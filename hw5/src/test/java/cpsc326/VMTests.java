/**
 * CPSC 326, Spring 2025
 * Basic VM Tests.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

class VMTests {

  private PrintStream stdout = System.out;
  private ByteArrayOutputStream output = new ByteArrayOutputStream();

  @BeforeEach
  public void changeSystemOut() {
    // redirect System.out to output
    System.setOut(new PrintStream(output));
  }

  @AfterEach
  public void restoreSystemOut() {
    // reset System.out to standard out
    System.setOut(stdout);
  }

  // ----------------------------------------------------------------------
  // Getting Started

  @Test
  void singleNop() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.NOP());
    VM vm = new VM();
    vm.add(m);
    vm.run();
  }

  @Test
  void singleWrite() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  void singleDup() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(42));
    m.add(VMInstr.DUP());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("4242", output.toString());
  }

  // ----------------------------------------------------------------------
  // Basic literals and variables

  @Test
  void singlePop() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.POP());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  void writeNull() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("null", output.toString());
  }

  @Test
  void storeAndLoad() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.STORE(0));
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  // ***TODO: store in incorrect address ...
  @Test
  void badStoreIndex() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.STORE(0));
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.STORE(2));
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // ----------------------------------------------------------------------
  // Operations

  // add

  @Test
  void intAdd() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(12));
    m.add(VMInstr.PUSH(24));
    m.add(VMInstr.ADD());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("36", output.toString());
  }

  @Test
  void dblAdd() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3.50));
    m.add(VMInstr.PUSH(2.25));
    m.add(VMInstr.ADD());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("5.75", output.toString());
  }

  @Test
  void strAdd() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.ADD());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("abcdef", output.toString());
  }

  @Test
  void nullAddFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.ADD());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void nullAddSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.ADD());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // sub

  @Test
  void intSub() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(15));
    m.add(VMInstr.PUSH(9));
    m.add(VMInstr.SUB());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("6", output.toString());
  }

  @Test
  void dblSub() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3.75));
    m.add(VMInstr.PUSH(2.50));
    m.add(VMInstr.SUB());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("1.25", output.toString());
  }

  @Test
  void nullSubFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.SUB());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void nullSubSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.SUB());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // mul

  @Test
  void intMul() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(15));
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.MUL());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("45", output.toString());
  }

  @Test
  void dblMul() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1.25));
    m.add(VMInstr.PUSH(3.00));
    m.add(VMInstr.MUL());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("3.75", output.toString());
  }

  @Test
  void nullMulFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.MUL());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void nullMulSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.MUL());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // div

  @Test
  void intDiv() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(16));
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("5", output.toString());
  }

  @Test
  void dblDiv() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3.75));
    m.add(VMInstr.PUSH(3.00));
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("1.25", output.toString());
  }

  @Test
  void nullDivFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void nullDivSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void intDivByZero() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void dblDivByZero() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1.5));
    m.add(VMInstr.PUSH(0.0));
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // and

  @Test
  void and() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.AND());
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.AND());
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.AND());
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.AND());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsefalsefalsetrue", output.toString());
  }

  @Test
  void andNullFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.AND());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void andNullSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.AND());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // or

  @Test
  void or() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.OR());
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.OR());
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.OR());
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.OR());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsetruetruetrue", output.toString());
  }

  @Test
  void orNullFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.OR());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void orNullSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.OR());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // not

  @Test
  void not() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.NOT());
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.NOT());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalse", output.toString());
  }

  @Test
  void notNullOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.NOT());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // cmplt

  @Test
  void intLessThan() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsefalsetrue", output.toString());
  }

  @Test
  void dblLessThan() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsefalsetrue", output.toString());
  }

  @Test
  void strLessThan() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsefalsetrue", output.toString());
  }

  @Test
  void lessThanNullFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void lessThanNullSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // cmple

  @Test
  void intLessThanEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalsetrue", output.toString());
  }

  @Test
  void dblLessThanEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalsetrue", output.toString());
  }

  @Test
  void strLessThanEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalsetrue", output.toString());
  }

  @Test
  void lessEqualNullFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void lessThanEqualNullSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.CMPLE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // cmpeq

  @Test
  void intEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalsefalse", output.toString());
  }

  @Test
  void dblEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalsefalse", output.toString());
  }

  @Test
  void strEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("truefalsefalse", output.toString());
  }

  @Test
  void equalNullFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("false", output.toString());
  }

  @Test
  void equalNullSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("false", output.toString());
  }

  @Test
  void equalNullBothOperands() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.CMPEQ());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("true", output.toString());
  }

  // cmpne

  @Test
  void intNotEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsetruetrue", output.toString());
  }

  @Test
  void dblNotEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(1.0));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.PUSH(2.0));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsetruetrue", output.toString());
  }

  @Test
  void strNotEqual() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("abc"));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.PUSH("def"));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("falsetruetrue", output.toString());
  }

  @Test
  void notEqualNullFirstOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("true", output.toString());
  }

  @Test
  void notEqualNullSecondOperand() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("true", output.toString());
  }

  @Test
  void notEqualNullBothOperands() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.CMPNE());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("false", output.toString());
  }

  // ----------------------------------------------------------------------
  // Jumps

  @Test
  void jumpForward() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.JMP(3));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("green", output.toString());
  }

  @Test
  void jumpFalseForward() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(false));
    m.add(VMInstr.JMPF(4));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("green", output.toString());
  }

  @Test
  void jumpFalseNoJump() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(true));
    m.add(VMInstr.JMPF(4));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  void jumpBackwards() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.STORE(0));
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.CMPLT());
    m.add(VMInstr.JMPF(13));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.ADD());
    m.add(VMInstr.STORE(0));
    m.add(VMInstr.JMP(2));
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("bluebluegreen", output.toString());
  }

  // ----------------------------------------------------------------------
  // User-Defined Functions

  @Test
  void mainReturnsNull() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.RET());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("", output.toString());
  }

  @Test
  void functionReturnsLiteral() {
    // string f() { return "blue" }
    VMFrameTemplate f = new VMFrameTemplate("f");
    f.add(VMInstr.PUSH("blue"));
    f.add(VMInstr.RET());
    // void main() { print(f()) }
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.CALL("f"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(f);
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  void functionReturnsModifiedParameter() {
    // string f(x: int) { return x + 4 }
    VMFrameTemplate f = new VMFrameTemplate("f");
    f.add(VMInstr.PUSH(4));
    f.add(VMInstr.ADD());
    f.add(VMInstr.RET());
    // void main() { print(f()) }
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.CALL("f"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(f);
    vm.add(m);
    vm.run();
    assertEquals("7", output.toString());
  }

  @Test
  void functionSubtractsItsTwoParameters() {
    // string f(x: int, y: int) { return x - y }
    VMFrameTemplate f = new VMFrameTemplate("f");
    f.add(VMInstr.STORE(0)); // x
    f.add(VMInstr.STORE(1)); // y
    f.add(VMInstr.LOAD(0));
    f.add(VMInstr.LOAD(1));
    f.add(VMInstr.SUB());
    f.add(VMInstr.RET());
    // void main() { print(f(4, 3)) }
    VMFrameTemplate m = new VMFrameTemplate("main");
    // push args in reverse order
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.PUSH(4));
    m.add(VMInstr.CALL("f"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(f);
    vm.add(m);
    vm.run();
    assertEquals("1", output.toString());
  }

  @Test
  void functionPrintsTwoParameters() {
    // string f(x: int, y: int) { print(x) print(y) }
    VMFrameTemplate f = new VMFrameTemplate("f");
    f.add(VMInstr.STORE(0)); // x
    f.add(VMInstr.STORE(1)); // y
    f.add(VMInstr.LOAD(0));
    f.add(VMInstr.WRITE());
    f.add(VMInstr.LOAD(1));
    f.add(VMInstr.WRITE());
    f.add(VMInstr.RET());
    // void main() { print(f(4, 3)) }
    VMFrameTemplate m = new VMFrameTemplate("main");
    // push args in reverse order
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.CALL("f"));
    VM vm = new VM();
    vm.add(f);
    vm.add(m);
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  void sumUsingRecursion() {
    // string sum(x: int) { if x < 0 {return 0} return sum(x-1) + x }
    VMFrameTemplate f = new VMFrameTemplate("sum");
    f.add(VMInstr.STORE(0)); // x -> var[0]
    f.add(VMInstr.LOAD(0)); // push x
    f.add(VMInstr.PUSH(0));
    f.add(VMInstr.CMPLT()); // x < 0
    f.add(VMInstr.JMPF(7));
    f.add(VMInstr.PUSH(0));
    f.add(VMInstr.RET()); // return 0
    f.add(VMInstr.LOAD(0));
    f.add(VMInstr.PUSH(1));
    f.add(VMInstr.SUB()); // x - 1
    f.add(VMInstr.CALL("sum")); // sum(x-1)
    f.add(VMInstr.LOAD(0)); // push x
    f.add(VMInstr.ADD()); // sum(x-1) + x
    f.add(VMInstr.RET()); // return sum(x-1) + x
    // void main() { print(sum(4)) }
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(4));
    m.add(VMInstr.CALL("sum"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(f);
    vm.add(m);
    vm.run();
    assertEquals("10", output.toString());
  }

  @Test
  void callMultipleFunctions() {
    // int f(x: int) { return g(x+1) + x }
    VMFrameTemplate f = new VMFrameTemplate("f");
    f.add(VMInstr.STORE(0)); // x -> var[0]
    f.add(VMInstr.LOAD(0)); // push x
    f.add(VMInstr.PUSH(1));
    f.add(VMInstr.ADD()); // x + 1
    f.add(VMInstr.CALL("g")); // g(x+1)
    f.add(VMInstr.LOAD(0));
    f.add(VMInstr.ADD()); // g(x+1) + x
    f.add(VMInstr.RET()); // return g(x+1) + x
    // int g(x: int) { return x + 2 }
    VMFrameTemplate g = new VMFrameTemplate("g");
    g.add(VMInstr.STORE(0)); // x -> var[0]
    g.add(VMInstr.LOAD(0)); // push x
    g.add(VMInstr.PUSH(2));
    g.add(VMInstr.ADD()); // x + 2
    g.add(VMInstr.RET()); // return x + 2
    // void main() { print(f(10) }
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(10));
    m.add(VMInstr.CALL("f"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(f);
    vm.add(g);
    vm.add(m);
    vm.run();
    assertEquals("23", output.toString());
  }

  // ----------------------------------------------------------------------
  // Structs

  @Test
  void createTwoNoFieldStructs() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("20252026", output.toString());
  }

  @Test
  void createOneFieldStruct() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.SETF("field1"));
    m.add(VMInstr.DUP());
    m.add(VMInstr.GETF("field1"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  void createTwoOneFieldStructs() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.STORE(0)); // obj1 -> var[0]
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.STORE(1)); // obj2 -> var[1]
    // set obj1 field
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.SETF("field1"));
    // set obj2 field
    m.add(VMInstr.LOAD(1));
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.SETF("field1"));
    // write obj1 field
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.GETF("field1"));
    m.add(VMInstr.WRITE());
    // write obj1 field
    m.add(VMInstr.LOAD(1));
    m.add(VMInstr.GETF("field1"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  void createOneTwoFieldStruct() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.STORE(0)); // obj1 -> var[0]
    // set field1
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.SETF("field1"));
    // set field2
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.SETF("field2"));
    // write field1
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.GETF("field1"));
    m.add(VMInstr.WRITE());
    // write field2
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.GETF("field2"));
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("bluegreen", output.toString());
  }

  @Test
  void getFieldOnNullObject() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.GETF("field1"));
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void setFieldOnNullObject() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.SETF("field1"));
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // ----------------------------------------------------------------------
  // Arrays

  @Test
  void createTwoArrays() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(10)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH(5));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("20252026", output.toString());
  }

  @Test
  void invalidArrayCreationIntLength() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(-1)); // array length
    m.add(VMInstr.ALLOCA());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void invalidArrayCreationNullLength() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL)); // array length
    m.add(VMInstr.ALLOCA());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void simpleArrayAccess() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.STORE(0)); // oid -> var[0]
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.GETI());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.GETI());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.GETI());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("nullnullnull", output.toString());
  }

  @Test
  void invalidArrayAccessWithIndexTooLarge() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.GETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void invalidArrayAccessWithIndexTooSmall() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.PUSH(-1));
    m.add(VMInstr.GETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void invalidArrayAccessWithNullIndex() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.GETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void invalidNullArrayAccess() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL)); // null oid
    m.add(VMInstr.PUSH(0)); // array index
    m.add(VMInstr.GETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void arrayFieldUpdate() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.STORE(0)); // oid -> var[0]
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0)); // index
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.SETI());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.GETI());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  void arrayLoopWithUpdates() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3)); // array length
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.STORE(0));
    for (int i = 0; i < 3; ++i) {
      m.add(VMInstr.LOAD(0));
      m.add(VMInstr.PUSH(i));
      m.add(VMInstr.PUSH(10 + i));
      m.add(VMInstr.SETI());
    }
    for (int i = 0; i < 3; ++i) {
      m.add(VMInstr.LOAD(0));
      m.add(VMInstr.PUSH(i));
      m.add(VMInstr.GETI());
      m.add(VMInstr.WRITE());
    }
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("101112", output.toString());
  }

  @Test
  void invalidNullArraySetField() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL)); // oid
    m.add(VMInstr.PUSH(0)); // index
    m.add(VMInstr.PUSH(1)); // value
    m.add(VMInstr.SETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void invalidNullArrayIndexSetField() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(10));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.PUSH(VM.NULL)); // index
    m.add(VMInstr.PUSH(1)); // value
    m.add(VMInstr.SETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void arraySetFieldWithIndexTooSmall() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(10));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.PUSH(-1)); // index
    m.add(VMInstr.PUSH(1)); // value
    m.add(VMInstr.SETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void arraySetFieldWithIndexTooLarge() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(10));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.PUSH(10)); // index
    m.add(VMInstr.PUSH(1)); // value
    m.add(VMInstr.SETI());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // ----------------------------------------------------------------------
  // Built-In Functions

  // size

  @Test
  void stringSizeCalls() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(""));
    m.add(VMInstr.LEN());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.LEN());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("green"));
    m.add(VMInstr.LEN());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("045", output.toString());
  }

  @Test
  void nullObjectInCallToSize() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.LEN());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void arraySizeCalls() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.LEN());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.LEN());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH(10000));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.LEN());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("0310000", output.toString());
  }

  // get

  @Test
  void stringGetCalls() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue")); // second arg first
    m.add(VMInstr.PUSH(0)); // first arg second
    m.add(VMInstr.GETC());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.GETC());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.GETC());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.GETC());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("blue", output.toString());
  }

  @Test
  void stringGetIndexTooSmall() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH(-1));
    m.add(VMInstr.GETC());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void stringGetIndexTooBig() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH(4));
    m.add(VMInstr.GETC());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void stringGetWithNullString() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.PUSH(4));
    m.add(VMInstr.GETC());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void stringGetWithNullIndex() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("blue"));
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.GETC());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // int_val

  @Test
  void intValCalls() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3.14));
    m.add(VMInstr.TOINT());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("5"));
    m.add(VMInstr.TOINT());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("35", output.toString());
  }

  @Test
  void intValCalledOnBadString() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("bad int"));
    m.add(VMInstr.TOINT());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void intValCalledOnNull() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.TOINT());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // dbl_val

  @Test
  void dblValCalls() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("2.7"));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("4"));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("3.02.74.0", output.toString());
  }

  @Test
  void dblValCalledOnBadString() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH("bad double"));
    m.add(VMInstr.TODBL());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void dblValCalledOnNull() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.TODBL());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // str_val

  @Test
  void strValCalls() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.TOSTR());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH(5.1));
    m.add(VMInstr.TOSTR());
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("35.1", output.toString());
  }

  @Test
  void strValCalledOnNull() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(VM.NULL));
    m.add(VMInstr.TOSTR());
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  // additional tests as required by the assignment

  // Positive Tests

  @Test
  void complexCalculationWithMultipleOperations() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    // Calculate (3 + 4) * 2 - 5
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.PUSH(4));
    m.add(VMInstr.ADD()); // 3 + 4 = 7
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.MUL()); // 7 * 2 = 14
    m.add(VMInstr.PUSH(5));
    m.add(VMInstr.SUB()); // 14 - 5 = 9
    m.add(VMInstr.WRITE());
    VM vm = new VM();
    vm.add(m);
    vm.run();
    assertEquals("9", output.toString());
  }

  @Test
  void nestedFunctionCalls() {
    // Function that returns double the input
    VMFrameTemplate double_func = new VMFrameTemplate("double");
    double_func.add(VMInstr.PUSH(2));
    double_func.add(VMInstr.MUL());
    double_func.add(VMInstr.RET());

    // Function that adds 5 to the input
    VMFrameTemplate add5_func = new VMFrameTemplate("add5");
    add5_func.add(VMInstr.PUSH(5));
    add5_func.add(VMInstr.ADD());
    add5_func.add(VMInstr.RET());

    // Main function that calls double(add5(3))
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.PUSH(3));
    m.add(VMInstr.CALL("add5")); // add5(3) = 8
    m.add(VMInstr.CALL("double")); // double(8) = 16
    m.add(VMInstr.WRITE());

    VM vm = new VM();
    vm.add(double_func);
    vm.add(add5_func);
    vm.add(m);
    vm.run();
    assertEquals("16", output.toString());
  }

  // Negative Tests

  @Test
  void callUndefinedFunction() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    m.add(VMInstr.CALL("undefined_function"));
    VM vm = new VM();
    vm.add(m);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void divideStringByNumber() {
    VMFrameTemplate m = new VMFrameTemplate("main");
    // Push a string and a number
    m.add(VMInstr.PUSH("hello"));
    m.add(VMInstr.PUSH(5));
    // Try to divide a string by a number (invalid operation)
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());

    VM vm = new VM();
    vm.add(m);

    // Should throw a MyPLException
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR:"));
  }

}
