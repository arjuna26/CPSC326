/**
 * CPSC 326, Spring 2025
 * The virtual machine implementation.
 * author: Arjuna Herbst
 */

package cpsc326;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * MyPL virtual machine for running MyPL programs (as VM
 * instructions).
 */
public class VM {

  /* special NULL value */
  public static final Object NULL = new Object() {
    public String toString() {
      return "null";
    }
  };

  /* the array heap as an oid to list mapping */
  private Map<Integer, List<Object>> arrayHeap = new HashMap<>();

  /* the struct heap as an oid to object (field to value map) mapping */
  private Map<Integer, Map<String, Object>> structHeap = new HashMap<>();

  /* the operand stack */
  private Deque<Object> operandStack = new ArrayDeque<>();

  /* the function (frame) call stack */
  private Deque<VMFrame> callStack = new ArrayDeque<>();

  /* the set of program function definitions (frame templates) */
  private Map<String, VMFrameTemplate> templates = new HashMap<>();

  /* the next unused object id */
  private int nextObjectId = 2025;

  /* debug flag for output debug info during vm execution (run) */
  private boolean debug = false;

  // helper functions

  /**
   * Create and throw an error.
   * 
   * @param msg The error message.
   */
  private void error(String msg) {
    MyPLException.vmError(msg);
  }

  /**
   * Create and throw an error (for a specific frame).
   * 
   * @param msg   The error message.
   * @param frame The frame where the error occurred.
   */
  private void error(String msg, VMFrame frame) {
    String s = "%s in %s at %d: %s";
    String name = frame.template.functionName;
    int pc = frame.pc - 1;
    VMInstr instr = frame.template.instructions.get(pc);
    MyPLException.vmError(String.format(s, msg, name, pc, instr));
  }

  /**
   * Add a frame template to the VM.
   * 
   * @param template The template to add.
   */
  public void add(VMFrameTemplate template) {
    templates.put(template.functionName, template);
  }

  /**
   * For turning on debug mode to help with debugging the VM.
   * 
   * @param on Set to true to turn on debugging, false to turn it off.
   */
  public void debugMode(boolean on) {
    debug = on;
  }

  /**
   * Pretty-print the VM frames.
   */
  public String toString() {
    String s = "";
    for (var funName : templates.keySet()) {
      s += String.format("\nFrame '%s'\n", funName);
      VMFrameTemplate template = templates.get(funName);
      for (int i = 0; i < template.instructions.size(); ++i)
        s += String.format("  %d: %s\n", i, template.instructions.get(i));
    }
    return s;
  }

  // Additional helpers for implementing the VM instructions

  /**
   * Helper to ensure the given value isn't NULL
   * 
   * @param x     the value to check
   * @param frame the current stack frame
   */
  private void ensureNotNull(Object x, VMFrame frame) {
    if (x == NULL)
      error("null value error", frame);
  }

  /**
   * Helper to add two objects
   */
  private Object addHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x + (int) y;
    else if (x instanceof Double)
      return (double) x + (double) y;
    else
      return (String) x + (String) y;
  }

  /**
   * Helper to subtract two objects
   */
  private Object subHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x - (int) y;
    else
      return (double) x - (double) y;
  }

  /**
   * Helper to multiply two objects
   */
  private Object mulHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x * (int) y;
    else
      return (double) x * (double) y;
  }

  /**
   * Helper to divide two objects
   */
  private Object divHelper(Object x, Object y, VMFrame f) {
    if (x instanceof Integer && (int) y != 0)
      return (int) ((int) x / (int) y);
    else if (x instanceof Double && (double) y != 0.0)
      return (double) x / (double) y;
    else
      error("division by zero error", f);
    return null;
  }

  /**
   * Helper to compare if first object less than second
   */
  private Object cmpltHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x < (int) y;
    else if (x instanceof Double)
      return (double) x < (double) y;
    else
      return ((String) x).compareTo((String) y) < 0;
  }

  /**
   * Helper to compare if first object less than or equal second
   */
  private Object cmpleHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x <= (int) y;
    else if (x instanceof Double)
      return (double) x <= (double) y;
    else
      return ((String) x).compareTo((String) y) <= 0;
  }
  // the main run method

  /**
   * Execute the program
   */
  public void run() {
    // grab the main frame and "instantiate" it
    if (!templates.containsKey("main"))
      error("No 'main' function");
    VMFrame frame = new VMFrame(templates.get("main"));
    callStack.push(frame);

    // run loop until out of call frames or instructions in the frame
    while (!callStack.isEmpty() && frame.pc < frame.template.instructions.size()) {
      // get the next instruction
      VMInstr instr = frame.template.instructions.get(frame.pc);

      // for debugging:
      if (debug) {
        System.out.println();
        System.out.println("\t FRAME.........: " + frame.template.functionName);
        System.out.println("\t PC............: " + frame.pc);
        System.out.println("\t INSTRUCTION...: " + instr);
        Object val = operandStack.isEmpty() ? null : operandStack.peek();
        System.out.println("\t NEXT OPERAND..: " + val);
      }

      // increment the pc
      ++frame.pc;

      // ----------------------------------------------------------------------
      // Literals and Variables
      // ----------------------------------------------------------------------

      if (instr.opcode == OpCode.PUSH) {
        operandStack.push(instr.operand);
      }

      else if (instr.opcode == OpCode.POP) {
        operandStack.pop();
      }

      else if (instr.opcode == OpCode.LOAD) {
        operandStack.push(frame.memory.get((int) instr.operand));
      }

      else if (instr.opcode == OpCode.STORE) {
        int index = (int) instr.operand;
        Object value = operandStack.pop();
        // Check if the index is valid
        if (index < 0 || index > frame.memory.size()) {
          error("invalid memory address: " + index, frame);
        }

        // Ensure memory list has enough space
        while (frame.memory.size() <= index) {
          frame.memory.add(null);
        }

        // Now set the value
        frame.memory.set(index, value);
      }

      // ----------------------------------------------------------------------
      // Arithmetic Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.ADD)

      {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(addHelper(x, y));
      }

      else if (instr.opcode == OpCode.SUB) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(subHelper(x, y));
      }

      else if (instr.opcode == OpCode.MUL) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(mulHelper(x, y));
      }

      else if (instr.opcode == OpCode.DIV) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(divHelper(x, y, frame));
      }

      // ----------------------------------------------------------------------
      // Comparison Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.CMPLT) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(cmpltHelper(x, y));
      }

      else if (instr.opcode == OpCode.CMPLE) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(cmpleHelper(x, y));
      }

      else if (instr.opcode == OpCode.CMPEQ || instr.opcode == OpCode.CMPNE) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        boolean isEqual = (x == NULL && y == NULL) || (x != NULL && y != NULL && x.equals(y));
        operandStack.push(instr.opcode == OpCode.CMPEQ ? isEqual : !isEqual);
      }

      // ----------------------------------------------------------------------
      // Logical Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.AND || instr.opcode == OpCode.OR) {
        Object y = operandStack.pop();
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push(instr.opcode == OpCode.AND ? (boolean) x && (boolean) y : (boolean) x || (boolean) y);
      }

      else if (instr.opcode == OpCode.NOT) {
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        operandStack.push(!(boolean) x);
      }

      // ----------------------------------------------------------------------
      // Jump Instructions
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.JMP) {
        int offset = (int) instr.operand;
        frame.pc = offset;
      }

      else if (instr.opcode == OpCode.JMPF) {
        int offset = (int) instr.operand;
        Object x = operandStack.pop();
        ensureNotNull(x, frame);
        if (!(boolean) x) {
          frame.pc = offset;
        }
      }

      // ----------------------------------------------------------------------
      // Function Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.CALL) {
        String funcName = (String) instr.operand;
        if (!templates.containsKey(funcName)) {
          error("undefined function: " + funcName, frame);
        }
        VMFrame newFrame = new VMFrame(templates.get(funcName));
        callStack.push(newFrame);
        frame = newFrame;
      }

      else if (instr.opcode == OpCode.RET) {
        // The return value (if any) should already be on the operand stack
        // Pop the current frame
        callStack.pop();
        if (callStack.isEmpty()) {
          return;
        }
        frame = callStack.peek();
        // Return value remains on operand stack for caller to use
      }

      // ----------------------------------------------------------------------
      // I/O Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.WRITE) {
        Object x = operandStack.pop();
        if (x == NULL) {
          System.out.print("null");
        } else {
          System.out.print(x);
        }
      }

      else if (instr.opcode == OpCode.READ) {
        try {
          operandStack.push(new BufferedReader(new InputStreamReader(System.in)).readLine());
        } catch (Exception e) {
          error("read operation failed: " + e.getMessage(), frame);
        }
      }

      // ----------------------------------------------------------------------
      // String/Array Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.LEN) {
        Object value = operandStack.pop();
        ensureNotNull(value, frame);
        if (value instanceof String) {
          operandStack.push(((String) value).length());
        } else if (arrayHeap.containsKey((int) value)) {
          operandStack.push(arrayHeap.get((int) value).size());
        } else {
          error("len operation applied to invalid type", frame);
        }
      }

      else if (instr.opcode == OpCode.GETC) {
        Object index = operandStack.pop();
        Object str = operandStack.pop();
        ensureNotNull(index, frame);
        ensureNotNull(str, frame);
        int i = (int) index;
        String s = (String) str;
        if (i < 0 || i >= s.length()) {
          error("string index out of bounds: " + i, frame);
        }
        operandStack.push(String.valueOf(s.charAt(i)));
      }

      // ----------------------------------------------------------------------
      // Type Conversion
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.TOINT || instr.opcode == OpCode.TODBL) {
        Object value = operandStack.pop();
        ensureNotNull(value, frame);
        try {
          if (instr.opcode == OpCode.TOINT) {
            if (value instanceof Double)
              operandStack.push(((Double) value).intValue());
            else if (value instanceof String)
              operandStack.push(Integer.parseInt((String) value));
            else
              error("cannot convert to int: " + value, frame);
          } else { // TODBL
            if (value instanceof Integer)
              operandStack.push(((Integer) value).doubleValue());
            else if (value instanceof String)
              operandStack.push(Double.parseDouble((String) value));
            else
              error("cannot convert to double: " + value, frame);
          }
        } catch (NumberFormatException e) {
          error("cannot convert string to " + (instr.opcode == OpCode.TOINT ? "int" : "double") + ": " + value, frame);
        }
      }

      else if (instr.opcode == OpCode.TOSTR) {
        Object value = operandStack.pop();
        ensureNotNull(value, frame); // This will throw an exception if value is NULL
        operandStack.push(String.valueOf(value));
      }

      // ----------------------------------------------------------------------
      // Struct Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.ALLOCS) {
        Map<String, Object> struct = new HashMap<>();
        int oid = nextObjectId++;
        structHeap.put(oid, struct);
        operandStack.push(oid);
      }

      else if (instr.opcode == OpCode.SETF || instr.opcode == OpCode.GETF) {
        Object value = instr.opcode == OpCode.SETF ? operandStack.pop() : null;
        Object oid = operandStack.pop();
        String field = (String) instr.operand;
        ensureNotNull(oid, frame);
        if (!structHeap.containsKey((int) oid))
          error("invalid struct object id: " + oid, frame);

        if (instr.opcode == OpCode.SETF)
          structHeap.get((int) oid).put(field, value);
        else
          operandStack.push(structHeap.get((int) oid).get(field));
      }

      // ----------------------------------------------------------------------
      // Array Operations
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.ALLOCA) {
        Object length = operandStack.pop();
        ensureNotNull(length, frame);
        if (!(length instanceof Integer)) {
          error("array length must be an integer", frame);
        }
        int len = (int) length;
        if (len < 0) {
          error("array length cannot be negative: " + len, frame);
        }
        List<Object> array = new ArrayList<>(Collections.nCopies(len, NULL));
        int oid = nextObjectId++;
        arrayHeap.put(oid, array);
        operandStack.push(oid);
      }

      else if (instr.opcode == OpCode.SETI || instr.opcode == OpCode.GETI) {
        Object value = instr.opcode == OpCode.SETI ? operandStack.pop() : null;
        Object index = operandStack.pop();
        Object oid = operandStack.pop();
        ensureNotNull(oid, frame);
        ensureNotNull(index, frame);
        if (!arrayHeap.containsKey((int) oid))
          error("invalid array object id: " + oid, frame);

        int i = (int) index;
        List<Object> array = arrayHeap.get((int) oid);
        if (i < 0 || i >= array.size())
          error("array index out of bounds: " + i, frame);

        if (instr.opcode == OpCode.SETI)
          array.set(i, value);
        else
          operandStack.push(array.get(i));
      }

      // ----------------------------------------------------------------------
      // Special Instructions
      // ----------------------------------------------------------------------

      else if (instr.opcode == OpCode.DUP) {
        Object val = operandStack.peek();
        operandStack.push(val);
      } else if (instr.opcode == OpCode.NOP) {
        // do nothing
      } else

        error("Unsupported operation: " + instr);
    }
  }
}
