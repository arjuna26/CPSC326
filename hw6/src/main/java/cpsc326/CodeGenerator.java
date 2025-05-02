/**
 * CPSC 326, Spring 2025
 * 
 * @author Arjuna Herbst
 */

package cpsc326;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Generates MyPL VM code from an AST.
 */
public class CodeGenerator implements Visitor {

  /* vm to add frames to */
  private VM vm;

  /* current frame template being generated */
  private VMFrameTemplate currTemplate;

  /* variable -> index mappings with respect to environments */
  private VarTable varTable = new VarTable();

  /* struct defs for field names */
  private Map<String, StructDef> structs = new HashMap<>();

  /**
   * Create a new Code Generator given a virtual machine
   * 
   * @param vm the VM for storing generated frame templates
   */
  public CodeGenerator(VM vm) {
    this.vm = vm;
  }

  // ----------------------------------------------------------------------
  // Helper functions
  // ----------------------------------------------------------------------

  /**
   * Helper to add an instruction to the current frame.
   * 
   * @param instr the instruction to add
   */
  private void add(VMInstr instr) {
    currTemplate.add(instr);
  }

  /**
   * Helper to add an instruction to the current frame with a comment.
   * 
   * @param instr   the instruction to add
   * @param comment the comment to assign to the instruction
   */
  private void add(VMInstr instr, String comment) {
    instr.comment = comment;
    currTemplate.add(instr);
  }

  /**
   * Helper to execute body statements that cleans up the stack for
   * single function call statements (whose returned values aren't
   * used).
   */
  private void execBody(List<Stmt> stmts) {
    for (var stmt : stmts) {
      stmt.accept(this);
      if (stmt instanceof CallRValue)
        add(VMInstr.POP(), "clean up call rvalue statement");
    }
  }

  private boolean handleBuiltInFunction(CallRValue node) {
    String funcName = node.funName.lexeme;

    if (funcName.equals("print") || funcName.equals("println")) {
      if (node.args.size() > 0) {
        node.args.get(0).accept(this);
      } else {
        add(VMInstr.PUSH(""), "empty string for println");
      }
      add(VMInstr.WRITE(), funcName);
      if (funcName.equals("println")) {
        add(VMInstr.PUSH("\n"), "newline");
        add(VMInstr.WRITE(), "print newline");
      }
      add(VMInstr.PUSH(VM.NULL), "null return value for " + funcName);
      return true;
    } else if (funcName.equals("readln")) {
      add(VMInstr.READ(), "read input");
      return true;
    } else if (funcName.equals("int_val")) {
      node.args.get(0).accept(this);
      add(VMInstr.TOINT(), "convert to int");
      return true;
    } else if (funcName.equals("dbl_val")) {
      node.args.get(0).accept(this);
      add(VMInstr.TODBL(), "convert to double");
      return true;
    } else if (funcName.equals("str_val")) {
      node.args.get(0).accept(this);
      add(VMInstr.TOSTR(), "convert to string");
      return true;
    } else if (funcName.equals("size")) {
      node.args.get(0).accept(this);
      add(VMInstr.LEN(), "get length");
      return true;
    } else if (funcName.equals("get")) {
      node.args.get(1).accept(this);
      node.args.get(0).accept(this);
      add(VMInstr.GETC(), "get character");
      return true;
    }
    return false;
  }

  private void handleVarRValuePath(List<VarRef> path) {
    String baseName = path.get(0).varName.lexeme;
    int baseIndex = varTable.get(baseName);
    add(VMInstr.LOAD(baseIndex), "load base variable " + baseName);
    if (path.get(0).arrayExpr.isPresent()) {
      path.get(0).arrayExpr.get().accept(this);
      add(VMInstr.GETI(), "get array element");
    }
    for (int i = 1; i < path.size(); i++) {
      VarRef ref = path.get(i);
      add(VMInstr.GETF(ref.varName.lexeme), "get field " + ref.varName.lexeme);
      if (ref.arrayExpr.isPresent()) {
        ref.arrayExpr.get().accept(this);
        add(VMInstr.GETI(), "get array element");
      }
    }
  }

  // ----------------------------------------------------------------------
  // Visitors for programs, functions, and structs
  // ----------------------------------------------------------------------

  /**
   * Generates the IR for the program
   */
  public void visit(Program node) {
    for (StructDef s : node.structs)
      s.accept(this);
    for (FunDef f : node.functions)
      f.accept(this);
  }

  /**
   * Generates a function definition
   */
  public void visit(FunDef node) {
    currTemplate = new VMFrameTemplate(node.funName.lexeme);
    varTable.pushEnvironment();
    for (VarDef param : node.params) {
      varTable.add(param.varName.lexeme);
    }
    if (node.params.size() > 0) {
      for (int i = 0; i < node.params.size(); i++) {
        add(VMInstr.PUSH(VM.NULL), "placeholder for " + node.params.get(i).varName.lexeme);
      }
      for (int i = 0; i < node.params.size(); i++) {
        int paramIndex = varTable.get(node.params.get(i).varName.lexeme);
        add(VMInstr.STORE(paramIndex), "reserve space for parameter " + node.params.get(i).varName.lexeme);
      }
      for (int i = node.params.size() - 1; i >= 0; i--) {
        int paramIndex = varTable.get(node.params.get(i).varName.lexeme);
        add(VMInstr.STORE(paramIndex), "initialize parameter " + node.params.get(i).varName.lexeme);
      }
    }
    execBody(node.stmts);
    boolean hasExplicitReturn = !node.stmts.isEmpty() &&
        (node.stmts.get(node.stmts.size() - 1) instanceof ReturnStmt);
    if (!hasExplicitReturn) {
      add(VMInstr.PUSH(VM.NULL), "default return value");
      add(VMInstr.RET(), "implicit return from function");
    }
    varTable.popEnvironment();
    vm.add(currTemplate);
  }

  /**
   * Adds the struct def to the list of structs.
   */
  public void visit(StructDef node) {
    structs.put(node.structName.lexeme, node);
  }

  /**
   * The visitor function for a variable definition, but this visitor
   * function is not used in code generation.
   */
  public void visit(VarDef node) {
    // nothing to do here
  }

  /**
   * The visitor function for data types, but not used in code generation.
   */
  public void visit(DataType node) {
    // nothing to do here
  }

  public void visit(ReturnStmt node) {
    node.expr.accept(this);
    add(VMInstr.RET(), "return from function ");
  }

  public void visit(VarStmt node) {
    varTable.add(node.varName.lexeme);
    int varIndex = varTable.get(node.varName.lexeme);
    if (node.expr.isPresent()) {
      node.expr.get().accept(this);
    } else {
      add(VMInstr.PUSH(VM.NULL), "push null for uninitialized variable");
    }
    add(VMInstr.STORE(varIndex), "store into variable " + node.varName.lexeme);
  }

  public void visit(AssignStmt node) {
    if (node.lvalue.size() == 1 && !node.lvalue.get(0).arrayExpr.isPresent()) {
      node.expr.accept(this);
      String varName = node.lvalue.get(0).varName.lexeme;
      int varIndex = varTable.get(varName);
      add(VMInstr.STORE(varIndex), "assign to " + varName);
      return;
    }
    if (node.lvalue.size() == 1 && node.lvalue.get(0).arrayExpr.isPresent()) {
      String varName = node.lvalue.get(0).varName.lexeme;
      int varIndex = varTable.get(varName);
      add(VMInstr.LOAD(varIndex), "load array " + varName);
      node.lvalue.get(0).arrayExpr.get().accept(this);
      node.expr.accept(this);
      add(VMInstr.SETI(), "set array element");
      return;
    }
    varTable.pushEnvironment();
    varTable.add("__temp_expr__");
    int tempIndex = varTable.get("__temp_expr__");
    node.expr.accept(this);
    add(VMInstr.STORE(tempIndex), "store expression value temporarily");
    String baseName = node.lvalue.get(0).varName.lexeme;
    int baseIndex = varTable.get(baseName);
    add(VMInstr.LOAD(baseIndex), "load base variable " + baseName);
    if (node.lvalue.get(0).arrayExpr.isPresent()) {
      node.lvalue.get(0).arrayExpr.get().accept(this);
      add(VMInstr.GETI(), "get array element to access its field");
    }
    for (int i = 1; i < node.lvalue.size() - 1; i++) {
      VarRef ref = node.lvalue.get(i);
      add(VMInstr.GETF(ref.varName.lexeme), "get field " + ref.varName.lexeme);
      if (ref.arrayExpr.isPresent()) {
        ref.arrayExpr.get().accept(this);
        add(VMInstr.GETI(), "get array element");
      }
    }
    VarRef lastRef = node.lvalue.get(node.lvalue.size() - 1);
    if (lastRef.arrayExpr.isPresent()) {
      add(VMInstr.GETF(lastRef.varName.lexeme), "get last field " + lastRef.varName.lexeme);
      lastRef.arrayExpr.get().accept(this);
      add(VMInstr.LOAD(tempIndex), "load expression value");
      add(VMInstr.SETI(), "set array element");
    } else {
      add(VMInstr.LOAD(tempIndex), "load expression value");
      add(VMInstr.SETF(lastRef.varName.lexeme), "set field " + lastRef.varName.lexeme);
    }
    varTable.popEnvironment();
  }

  public void visit(WhileStmt node) {
    varTable.pushEnvironment();
    int conditionPos = currTemplate.instructions.size();
    node.condition.accept(this);
    int jumpInstrPos = currTemplate.instructions.size();
    add(VMInstr.JMPF(0), "jump to end of while if false");
    execBody(node.stmts);
    add(VMInstr.JMP(conditionPos), "jump back to condition");
    int endPos = currTemplate.instructions.size();
    currTemplate.instructions.get(jumpInstrPos).operand = endPos;
    varTable.popEnvironment();
  }

  public void visit(ForStmt node) {
    varTable.pushEnvironment();
    varTable.add(node.varName.lexeme);
    int varIndex = varTable.get(node.varName.lexeme);
    node.fromExpr.accept(this);
    add(VMInstr.STORE(varIndex), "initialize loop variable");
    int conditionPos = currTemplate.instructions.size();
    add(VMInstr.LOAD(varIndex), "load loop variable");
    node.toExpr.accept(this);
    add(VMInstr.CMPLE(), "check if loop variable <= upper bound");
    int jumpInstrPos = currTemplate.instructions.size();
    add(VMInstr.JMPF(0), "exit loop if done");
    execBody(node.stmts);
    add(VMInstr.LOAD(varIndex), "load loop variable");
    add(VMInstr.PUSH(1), "increment value");
    add(VMInstr.ADD(), "add 1 to loop variable");
    add(VMInstr.STORE(varIndex), "update loop variable");
    add(VMInstr.JMP(conditionPos), "jump back to loop condition");
    int endPos = currTemplate.instructions.size();
    currTemplate.instructions.get(jumpInstrPos).operand = endPos;
    varTable.popEnvironment();
  }

  public void visit(IfStmt node) {
    node.condition.accept(this);
    int falseJumpPos = currTemplate.instructions.size();
    add(VMInstr.JMPF(0), "jump to else if condition false");
    varTable.pushEnvironment();
    execBody(node.ifStmts);
    varTable.popEnvironment();
    int endJumpPos = currTemplate.instructions.size();
    add(VMInstr.JMP(0), "jump past else branch");
    int elsePos = currTemplate.instructions.size();
    currTemplate.instructions.get(falseJumpPos).operand = elsePos;
    if (node.elseIf.isPresent()) {
      node.elseIf.get().accept(this);
    } else if (node.elseStmts.isPresent()) {
      varTable.pushEnvironment();
      execBody(node.elseStmts.get());
      varTable.popEnvironment();
    }
    int endPos = currTemplate.instructions.size();
    currTemplate.instructions.get(endJumpPos).operand = endPos;
  }

  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }

  public void visit(UnaryExpr node) {
    node.expr.accept(this);

    if (node.unaryOp.lexeme.equals("not")) {
      add(VMInstr.NOT(), "logical not");
    }
  }

  public void visit(BinaryExpr node) {
    String op = node.binaryOp.lexeme;
    if (op.equals(">") || op.equals(">=")) {
      node.rhs.accept(this);
      node.lhs.accept(this);
    } else {
      node.lhs.accept(this);
      node.rhs.accept(this);
    }
    if (op.equals("+")) {
      add(VMInstr.ADD(), "addition");
    } else if (op.equals("-")) {
      add(VMInstr.SUB(), "subtraction");
    } else if (op.equals("*")) {
      add(VMInstr.MUL(), "multiplication");
    } else if (op.equals("/")) {
      add(VMInstr.DIV(), "division");
    } else if (op.equals("<")) {
      add(VMInstr.CMPLT(), "less than");
    } else if (op.equals("<=")) {
      add(VMInstr.CMPLE(), "less than or equal");
    } else if (op.equals(">")) {
      add(VMInstr.CMPLT(), "greater than");
    } else if (op.equals(">=")) {
      add(VMInstr.CMPLE(), "greater than or equal");
    } else if (op.equals("==")) {
      add(VMInstr.CMPEQ(), "equal to");
    } else if (op.equals("!=")) {
      add(VMInstr.CMPNE(), "not equal to");
    } else if (op.equals("and")) {
      add(VMInstr.AND(), "logical and");
    } else if (op.equals("or")) {
      add(VMInstr.OR(), "logical or");
    }
  }

  public void visit(CallRValue node) {
    if (handleBuiltInFunction(node)) {
      return;
    }
    for (Expr arg : node.args) {
      arg.accept(this);
    }
    add(VMInstr.CALL(node.funName.lexeme), "call function " + node.funName.lexeme);
  }

  public void visit(SimpleRValue node) {
    TokenType type = node.literal.tokenType;
    String lexeme = node.literal.lexeme;
    if (type == TokenType.INT_VAL) {
      add(VMInstr.PUSH(Integer.parseInt(lexeme)), "int literal: " + lexeme);
    } else if (type == TokenType.DOUBLE_VAL) {
      add(VMInstr.PUSH(Double.parseDouble(lexeme)), "double literal: " + lexeme);
    } else if (type == TokenType.BOOL_VAL) {
      add(VMInstr.PUSH(Boolean.parseBoolean(lexeme)), "bool literal: " + lexeme);
    } else if (type == TokenType.STRING_VAL) {
      lexeme = lexeme.replace("\\n", "\n");
      lexeme = lexeme.replace("\\t", "\t");
      lexeme = lexeme.replace("\\r", "\r");
      add(VMInstr.PUSH(lexeme), "string literal: " + lexeme);
    } else if (type == TokenType.NULL_VAL) {
      add(VMInstr.PUSH(VM.NULL), "null literal");
    }
  }

  public void visit(NewStructRValue node) {
    add(VMInstr.ALLOCS(), "new struct " + node.structName.lexeme);
    for (int i = 0; i < node.args.size(); i++) {
      add(VMInstr.DUP(), "duplicate struct reference");
      node.args.get(i).accept(this);
      String fieldName = structs.get(node.structName.lexeme).fields.get(i).varName.lexeme;
      add(VMInstr.SETF(fieldName), "set field " + fieldName);
    }
  }

  public void visit(NewArrayRValue node) {
    node.arrayExpr.accept(this);
    add(VMInstr.ALLOCA(), "new array of " + node.type.lexeme);
  }

  public void visit(VarRValue node) {
    if (node.path.size() == 1 && !node.path.get(0).arrayExpr.isPresent()) {
      String varName = node.path.get(0).varName.lexeme;
      int varIndex = varTable.get(varName);
      add(VMInstr.LOAD(varIndex), "load variable " + varName);
    } else {
      handleVarRValuePath(node.path);
    }
  }
}
