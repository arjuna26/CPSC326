/**
 * CPSC 326, Spring 2025
 * Basic ast parser tests for HW-3.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 */
class ASTParserTests {

  /**
   * Helper to build an input string.
   */
  InputStream istream(String str) {
    try {
      return new ByteArrayInputStream(str.getBytes("UTF-8"));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  // ----------------------------------------------------------------------
  // Basic Function Definitions

  @Test
  void emptyInput() {
    var p = "";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(0, r.functions.size());
    assertEquals(0, r.structs.size());
  }

  @Test
  void emptyFunction() {
    var p = "int f() {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(0, r.structs.size());
    assertEquals(1, r.functions.size());
    assertEquals("int", r.functions.get(0).returnType.type.lexeme);
    assertFalse(r.functions.get(0).returnType.isArray);
    assertEquals("f", r.functions.get(0).funName.lexeme);
    assertEquals(0, r.functions.get(0).params.size());
    assertEquals(0, r.functions.get(0).stmts.size());
  }

  @Test
  void emptyFunctionArrayReturn() {
    var p = "[int] f() {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(0, r.structs.size());
    assertEquals(1, r.functions.size());
    assertEquals("int", r.functions.get(0).returnType.type.lexeme);
    assertTrue(r.functions.get(0).returnType.isArray);
    assertEquals("f", r.functions.get(0).funName.lexeme);
    assertEquals(0, r.functions.get(0).params.size());
    assertEquals(0, r.functions.get(0).stmts.size());
  }

  @Test
  void emptyFunctionOneBaseTypeParam() {
    var p = "int f(x: string) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(1, f.params.size());
    assertEquals("x", f.params.get(0).varName.lexeme);
    assertEquals("string", f.params.get(0).dataType.type.lexeme);
    assertFalse(f.params.get(0).dataType.isArray);
  }

  @Test
  void emptyFunctionOneBaseTypeArrayParam() {
    var p = "int f(x: [bool]) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(1, f.params.size());
    assertEquals("x", f.params.get(0).varName.lexeme);
    assertEquals("bool", f.params.get(0).dataType.type.lexeme);
    assertTrue(f.params.get(0).dataType.isArray);
  }

  @Test
  void emptyFunctionOneIdParam() {
    var p = "int f(s: my_struct) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(1, f.params.size());
    assertEquals("s", f.params.get(0).varName.lexeme);
    assertEquals("my_struct", f.params.get(0).dataType.type.lexeme);
    assertFalse(f.params.get(0).dataType.isArray);
  }

  @Test
  void emptyFunctionOneIdArrayParam() {
    var p = "int f(s: [my_struct]) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(1, f.params.size());
    assertEquals("s", f.params.get(0).varName.lexeme);
    assertEquals("my_struct", f.params.get(0).dataType.type.lexeme);
    assertTrue(f.params.get(0).dataType.isArray);
  }

  @Test
  void emptyFunctionTwoParams() {
    var p = "int f(x: bool, y: double) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(2, f.params.size());
    assertEquals("x", f.params.get(0).varName.lexeme);
    assertEquals("bool", f.params.get(0).dataType.type.lexeme);
    assertFalse(f.params.get(0).dataType.isArray);
    assertEquals("y", f.params.get(1).varName.lexeme);
    assertEquals("double", f.params.get(1).dataType.type.lexeme);
    assertFalse(f.params.get(1).dataType.isArray);
  }

  @Test
  void emptyFunctionThreeParams() {
    var p = "int f(x: bool, y: double, z: [int]) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(3, f.params.size());
    assertEquals("x", f.params.get(0).varName.lexeme);
    assertEquals("bool", f.params.get(0).dataType.type.lexeme);
    assertFalse(f.params.get(0).dataType.isArray);
    assertEquals("y", f.params.get(1).varName.lexeme);
    assertEquals("double", f.params.get(1).dataType.type.lexeme);
    assertFalse(f.params.get(1).dataType.isArray);
    assertEquals("z", f.params.get(2).varName.lexeme);
    assertEquals("int", f.params.get(2).dataType.type.lexeme);
    assertTrue(f.params.get(2).dataType.isArray);
  }

  @Test
  void twoEmptyFunctions() {
    var p = """
        int f() {}
        int g() {}
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    FunDef f = r.functions.get(0);
    assertEquals(2, r.functions.size());
    assertEquals(0, r.structs.size());
    assertEquals("f", r.functions.get(0).funName.lexeme);
    assertEquals(0, r.functions.get(0).params.size());
    assertEquals(0, r.functions.get(0).stmts.size());
    assertEquals("g", r.functions.get(1).funName.lexeme);
    assertEquals(0, r.functions.get(1).params.size());
    assertEquals(0, r.functions.get(1).stmts.size());
  }

  // ----------------------------------------------------------------------
  // Basic Function Definitions

  @Test
  void emptyStruct() {
    var p = "struct s {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(0, r.functions.size());
    assertEquals(1, r.structs.size());
    assertEquals("s", r.structs.get(0).structName.lexeme);
    assertEquals(0, r.structs.get(0).fields.size());
  }

  @Test
  void structWithOneBaseTypeField() {
    var p = "struct s {x: int}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.structs.size());
    assertEquals("s", r.structs.get(0).structName.lexeme);
    assertEquals(1, r.structs.get(0).fields.size());
    assertEquals("x", r.structs.get(0).fields.get(0).varName.lexeme);
    assertEquals("int", r.structs.get(0).fields.get(0).dataType.type.lexeme);
    assertFalse(r.structs.get(0).fields.get(0).dataType.isArray);
  }

  @Test
  void structWithOneIdField() {
    var p = "struct s {s1: s}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.structs.size());
    assertEquals("s", r.structs.get(0).structName.lexeme);
    assertEquals(1, r.structs.get(0).fields.size());
    assertEquals("s1", r.structs.get(0).fields.get(0).varName.lexeme);
    assertEquals("s", r.structs.get(0).fields.get(0).dataType.type.lexeme);
    assertFalse(r.structs.get(0).fields.get(0).dataType.isArray);
  }

  @Test
  void structWithOneArrayField() {
    var p = "struct s {x1: [int]}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.structs.size());
    assertEquals("s", r.structs.get(0).structName.lexeme);
    assertEquals(1, r.structs.get(0).fields.size());
    assertEquals("x1", r.structs.get(0).fields.get(0).varName.lexeme);
    assertEquals("int", r.structs.get(0).fields.get(0).dataType.type.lexeme);
    assertTrue(r.structs.get(0).fields.get(0).dataType.isArray);
  }

  @Test
  void structWithTwoBaseTypeFields() {
    var p = "struct s {x1: int, x2: bool}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.structs.size());
    assertEquals("s", r.structs.get(0).structName.lexeme);
    assertEquals(2, r.structs.get(0).fields.size());
    assertEquals("x1", r.structs.get(0).fields.get(0).varName.lexeme);
    assertEquals("int", r.structs.get(0).fields.get(0).dataType.type.lexeme);
    assertFalse(r.structs.get(0).fields.get(0).dataType.isArray);
    assertEquals("x2", r.structs.get(0).fields.get(1).varName.lexeme);
    assertEquals("bool", r.structs.get(0).fields.get(1).dataType.type.lexeme);
    assertFalse(r.structs.get(0).fields.get(1).dataType.isArray);
  }

  @Test
  void structWithThreeFields() {
    var p = "struct s {x1: int, x2: bool, x3: [s]}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.structs.size());
    assertEquals("s", r.structs.get(0).structName.lexeme);
    assertEquals(3, r.structs.get(0).fields.size());
    assertEquals("x1", r.structs.get(0).fields.get(0).varName.lexeme);
    assertEquals("int", r.structs.get(0).fields.get(0).dataType.type.lexeme);
    assertFalse(r.structs.get(0).fields.get(0).dataType.isArray);
    assertEquals("x2", r.structs.get(0).fields.get(1).varName.lexeme);
    assertEquals("bool", r.structs.get(0).fields.get(1).dataType.type.lexeme);
    assertFalse(r.structs.get(0).fields.get(1).dataType.isArray);
    assertEquals("x3", r.structs.get(0).fields.get(2).varName.lexeme);
    assertEquals("s", r.structs.get(0).fields.get(2).dataType.type.lexeme);
    assertTrue(r.structs.get(0).fields.get(2).dataType.isArray);
  }

  @Test
  void mixOfEmptyStructsAndFunctions() {
    var p = """
        struct s1 {}
        int f() {}
        struct s2 {}
        bool g() {}
        struct s3 {}
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(2, r.functions.size());
    assertEquals("f", r.functions.get(0).funName.lexeme);
    assertEquals(0, r.functions.get(0).params.size());
    assertEquals(0, r.functions.get(0).stmts.size());
    assertEquals("g", r.functions.get(1).funName.lexeme);
    assertEquals(0, r.functions.get(1).params.size());
    assertEquals(0, r.functions.get(1).stmts.size());
    assertEquals(3, r.structs.size());
    assertEquals("s1", r.structs.get(0).structName.lexeme);
    assertEquals(0, r.structs.get(0).fields.size());
    assertEquals("s2", r.structs.get(1).structName.lexeme);
    assertEquals(0, r.structs.get(1).fields.size());
    assertEquals("s3", r.structs.get(2).structName.lexeme);
    assertEquals(0, r.structs.get(2).fields.size());
  }

  // ----------------------------------------------------------------------
  // Variable Statements

  @Test
  void baseTypeVariableDeclarations() {
    var p = """
        void main() {
          var x1: int
          var x2: double
          var x3: bool
          var x4: string
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.size());
    assertEquals("main", r.functions.get(0).funName.lexeme);
    assertEquals(0, r.functions.get(0).params.size());
    assertEquals("void", r.functions.get(0).returnType.type.lexeme);
    assertFalse(r.functions.get(0).returnType.isArray);
    assertEquals(4, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("int", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
    s = (VarStmt) r.functions.get(0).stmts.get(1);
    assertEquals("x2", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("double", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
    s = (VarStmt) r.functions.get(0).stmts.get(2);
    assertEquals("x3", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("bool", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
    s = (VarStmt) r.functions.get(0).stmts.get(3);
    assertEquals("x4", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("string", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
  }

  @Test
  void arrayOfBaseTypeVariableDeclaration() {
    var p = """
        void main() {
          var x1: [int]
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("int", s.dataType.get().type.lexeme);
    assertTrue(s.dataType.get().isArray);
  }

  @Test
  void idVariableDeclaration() {
    var p = """
        void main() {
          var x1: s
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("s", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
  }

  @Test
  void baseTypeVariableDefinitionWithTypeAnnotation() {
    var p = """
        void main() {
          var x1: int = 0
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("int", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
    assertTrue(s.expr.isPresent());
    BasicExpr e = (BasicExpr) s.expr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("0", v.literal.lexeme);
  }

  @Test
  void idVariableDefinitionWithTypeAnnotation() {
    var p = """
        void main() {
          var x1: node = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("node", s.dataType.get().type.lexeme);
    assertFalse(s.dataType.get().isArray);
    assertTrue(s.expr.isPresent());
    BasicExpr e = (BasicExpr) s.expr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void baseTypeArrayVariableDefinitionWithTypeAnnotation() {
    var p = """
        void main() {
          var x1: [int] = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("int", s.dataType.get().type.lexeme);
    assertTrue(s.dataType.get().isArray);
    assertTrue(s.expr.isPresent());
    BasicExpr e = (BasicExpr) s.expr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void idArrayVariableDefinitionWithTypeAnnotation() {
    var p = """
        void main() {
          var x1: [node] = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertTrue(s.dataType.isPresent());
    assertEquals("node", s.dataType.get().type.lexeme);
    assertTrue(s.dataType.get().isArray);
    assertTrue(s.expr.isPresent());
    BasicExpr e = (BasicExpr) s.expr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void variableDefinitionWithoutTypeAnnotation() {
    var p = """
        void main() {
          var x1 = 0
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    VarStmt s = (VarStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x1", s.varName.lexeme);
    assertFalse(s.dataType.isPresent());
    assertTrue(s.expr.isPresent());
    BasicExpr e = (BasicExpr) s.expr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("0", v.literal.lexeme);
  }

  // ----------------------------------------------------------------------
  // Assignment statements

  @Test
  void simpleAssignment() {
    var p = """
        void main() {
          x = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x", s.lvalue.get(0).varName.lexeme);
    assertFalse(s.lvalue.get(0).arrayExpr.isPresent());
    BasicExpr e = (BasicExpr) s.expr;
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void simplePathAssignment() {
    var p = """
        void main() {
          x.y = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    assertEquals("x", s.lvalue.get(0).varName.lexeme);
    assertFalse(s.lvalue.get(0).arrayExpr.isPresent());
    BasicExpr e = (BasicExpr) s.expr;
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void simpleArrayPathAssignment() {
    var p = """
        void main() {
          x[0] = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    assertEquals(1, s.lvalue.size());
    assertEquals("x", s.lvalue.get(0).varName.lexeme);
    assertTrue(s.lvalue.get(0).arrayExpr.isPresent());
    BasicExpr e = (BasicExpr) s.lvalue.get(0).arrayExpr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("0", v.literal.lexeme);
    e = (BasicExpr) s.expr;
    v = (SimpleRValue) e.rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void multiplePathAssignment() {
    var p = """
        void main() {
          x1.x2[0].x3.x4[1] = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    assertEquals(4, s.lvalue.size());
    // x1
    assertEquals("x1", s.lvalue.get(0).varName.lexeme);
    assertFalse(s.lvalue.get(0).arrayExpr.isPresent());
    // x2[0]
    assertEquals("x2", s.lvalue.get(1).varName.lexeme);
    assertTrue(s.lvalue.get(1).arrayExpr.isPresent());
    BasicExpr e = (BasicExpr) s.lvalue.get(1).arrayExpr.get();
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("0", v.literal.lexeme);
    // x3
    assertEquals("x3", s.lvalue.get(2).varName.lexeme);
    assertFalse(s.lvalue.get(2).arrayExpr.isPresent());
    // x4[1]
    assertEquals("x4", s.lvalue.get(3).varName.lexeme);
    assertTrue(s.lvalue.get(3).arrayExpr.isPresent());
    e = (BasicExpr) s.lvalue.get(3).arrayExpr.get();
    v = (SimpleRValue) e.rvalue;
    assertEquals("1", v.literal.lexeme);
  }

  // ----------------------------------------------------------------------
  // If Statements

  @Test
  void singleIfStatement() {
    var p = """
        void main() {
          if true {}
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s = (IfStmt) r.functions.get(0).stmts.get(0);
    BasicExpr e = (BasicExpr) s.condition;
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("true", v.literal.lexeme);
    assertEquals(0, s.ifStmts.size());
    assertFalse(s.elseIf.isPresent());
    assertFalse(s.elseStmts.isPresent());
  }

  @Test
  void singleIfStatementWithBody() {
    var p = """
        void main() {
          if true {var x = 0}
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s = (IfStmt) r.functions.get(0).stmts.get(0);
    BasicExpr e = (BasicExpr) s.condition;
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("true", v.literal.lexeme);
    assertEquals(1, s.ifStmts.size());
    assertFalse(s.elseIf.isPresent());
    assertFalse(s.elseStmts.isPresent());
  }

  @Test
  void singleIfStatementWithOneElseIf() {
    var p = """
        void main() {
          if true {}
          else if false {}
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s1 = (IfStmt) r.functions.get(0).stmts.get(0);
    BasicExpr e1 = (BasicExpr) s1.condition;
    SimpleRValue v1 = (SimpleRValue) e1.rvalue;
    assertEquals("true", v1.literal.lexeme);
    assertEquals(0, s1.ifStmts.size());
    assertTrue(s1.elseIf.isPresent());
    assertFalse(s1.elseStmts.isPresent());
    IfStmt s2 = (IfStmt) s1.elseIf.get();
    BasicExpr e2 = (BasicExpr) s2.condition;
    SimpleRValue v2 = (SimpleRValue) e2.rvalue;
    assertEquals("false", v2.literal.lexeme);
    assertEquals(0, s2.ifStmts.size());
    assertFalse(s2.elseIf.isPresent());
    assertFalse(s2.elseStmts.isPresent());
  }

  @Test
  void singleIfStatementWithTwoElseIfs() {
    var p = """
        void main() {
          if true {}
          else if false {}
          else if true {}
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s1 = (IfStmt) r.functions.get(0).stmts.get(0);
    BasicExpr e1 = (BasicExpr) s1.condition;
    SimpleRValue v1 = (SimpleRValue) e1.rvalue;
    assertEquals("true", v1.literal.lexeme);
    assertEquals(0, s1.ifStmts.size());
    assertTrue(s1.elseIf.isPresent());
    assertFalse(s1.elseStmts.isPresent());
    IfStmt s2 = (IfStmt) s1.elseIf.get();
    BasicExpr e2 = (BasicExpr) s2.condition;
    SimpleRValue v2 = (SimpleRValue) e2.rvalue;
    assertEquals("false", v2.literal.lexeme);
    assertEquals(0, s2.ifStmts.size());
    assertTrue(s2.elseIf.isPresent());
    assertFalse(s2.elseStmts.isPresent());
    IfStmt s3 = (IfStmt) s2.elseIf.get();
    BasicExpr e3 = (BasicExpr) s3.condition;
    SimpleRValue v3 = (SimpleRValue) e3.rvalue;
    assertEquals("true", v3.literal.lexeme);
    assertEquals(0, s3.ifStmts.size());
    assertFalse(s3.elseIf.isPresent());
    assertFalse(s3.elseStmts.isPresent());
  }

  @Test
  void singleIfStatementWithEmptyElse() {
    var p = """
        void main() {
          if true {}
          else {}
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s = (IfStmt) r.functions.get(0).stmts.get(0);
    assertEquals(0, s.ifStmts.size());
    assertFalse(s.elseIf.isPresent());
    assertTrue(s.elseStmts.isPresent());
    assertEquals(0, s.elseStmts.get().size());
  }

  @Test
  void singleIfStatementWithNonEmptyElse() {
    var p = """
        void main() {
          if true {}
          else { var x = 0 }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s = (IfStmt) r.functions.get(0).stmts.get(0);
    assertEquals(0, s.ifStmts.size());
    assertFalse(s.elseIf.isPresent());
    assertTrue(s.elseStmts.isPresent());
    assertEquals(1, s.elseStmts.get().size());
  }

  @Test
  void fullIfStatement() {
    var p = """
        void main() {
          if true { x = 1 }
          else if false { x = 2 }
          else { x = 3 }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    IfStmt s1 = (IfStmt) r.functions.get(0).stmts.get(0);
    assertEquals(1, s1.ifStmts.size());
    assertTrue(s1.elseIf.isPresent());
    assertFalse(s1.elseStmts.isPresent());
    IfStmt s2 = (IfStmt) s1.elseIf.get();
    assertEquals(1, s2.ifStmts.size());
    assertFalse(s2.elseIf.isPresent());
    assertTrue(s2.elseStmts.isPresent());
    assertEquals(1, s2.elseStmts.get().size());
  }

  // ----------------------------------------------------------------------
  // While Statements

  @Test
  void emptyWhileStatement() {
    var p = """
        void main() {
          while true { }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    WhileStmt s = (WhileStmt) r.functions.get(0).stmts.get(0);
    BasicExpr e = (BasicExpr) s.condition;
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("true", v.literal.lexeme);
    assertEquals(0, s.stmts.size());
  }

  @Test
  void whileWithBodyStatement() {
    var p = """
        void main() {
          while false { x = 0 }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    WhileStmt s = (WhileStmt) r.functions.get(0).stmts.get(0);
    BasicExpr e = (BasicExpr) s.condition;
    SimpleRValue v = (SimpleRValue) e.rvalue;
    assertEquals("false", v.literal.lexeme);
    assertEquals(1, s.stmts.size());
  }

  // ----------------------------------------------------------------------
  // expressions

  @Test
  void simpleLiteralValues() {
    var p = """
        void main() {
          x = true
          x = false
          x = 0
          x = 0.0
          x = ""
          x = null
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(6, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    SimpleRValue v = (SimpleRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("true", v.literal.lexeme);
    s = (AssignStmt) r.functions.get(0).stmts.get(1);
    v = (SimpleRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("false", v.literal.lexeme);
    s = (AssignStmt) r.functions.get(0).stmts.get(2);
    v = (SimpleRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("0", v.literal.lexeme);
    s = (AssignStmt) r.functions.get(0).stmts.get(3);
    v = (SimpleRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("0.0", v.literal.lexeme);
    s = (AssignStmt) r.functions.get(0).stmts.get(4);
    v = (SimpleRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("", v.literal.lexeme);
    s = (AssignStmt) r.functions.get(0).stmts.get(5);
    v = (SimpleRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("null", v.literal.lexeme);
  }

  @Test
  void simpleBoolExpression() {
    var p = """
        void main() {
          x = true and false
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    BinaryExpr e = (BinaryExpr) s.expr;
    assertEquals("and", e.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e.lhs).rvalue;
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e.rhs).rvalue;
    assertEquals("true", v1.literal.lexeme);
    assertEquals("false", v2.literal.lexeme);
  }

  @Test
  void simpleNotExpression() {
    var p = """
        void main() {
          x = not true or false
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    UnaryExpr e1 = (UnaryExpr) s.expr;
    assertEquals("not", e1.unaryOp.lexeme);
    BinaryExpr e2 = (BinaryExpr) e1.expr;
    assertEquals("or", e2.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e2.lhs).rvalue;
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e2.rhs).rvalue;
    assertEquals("true", v1.literal.lexeme);
    assertEquals("false", v2.literal.lexeme);
  }

  @Test
  void doubleNotExpression() {
    var p = """
        void main() {
          x = not not true or false
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    UnaryExpr e1 = (UnaryExpr) s.expr;
    assertEquals("not", e1.unaryOp.lexeme);
    UnaryExpr e2 = (UnaryExpr) e1.expr;
    assertEquals("not", e2.unaryOp.lexeme);
    BinaryExpr e3 = (BinaryExpr) e2.expr;
    assertEquals("or", e3.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e3.lhs).rvalue;
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e3.rhs).rvalue;
    assertEquals("true", v1.literal.lexeme);
    assertEquals("false", v2.literal.lexeme);
  }

  @Test
  void simpleParenExpression() {
    var p = """
        void main() {
          x = (1 + 2)
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    BinaryExpr e1 = (BinaryExpr) s.expr;
    assertEquals("+", e1.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e1.lhs).rvalue;
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e1.rhs).rvalue;
    assertEquals("1", v1.literal.lexeme);
    assertEquals("2", v2.literal.lexeme);
  }

  @Test
  void parenExpressionFollowedByExpression() {
    var p = """
        void main() {
          x = (1 + 2) - 3
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    BinaryExpr e1 = (BinaryExpr) s.expr;
    assertEquals("-", e1.binaryOp.lexeme);
    BinaryExpr e2 = (BinaryExpr) e1.lhs;
    assertEquals("+", e2.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e2.lhs).rvalue;
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e2.rhs).rvalue;
    assertEquals("1", v1.literal.lexeme);
    assertEquals("2", v2.literal.lexeme);
    SimpleRValue v3 = (SimpleRValue) ((BasicExpr) e1.rhs).rvalue;
    assertEquals("3", v3.literal.lexeme);
  }

  @Test
  void parenExpressionBeforeExpression() {
    var p = """
        void main() {
          x = 3 * (1 + 2)
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    BinaryExpr e1 = (BinaryExpr) s.expr;
    assertEquals("*", e1.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e1.lhs).rvalue;
    assertEquals("3", v1.literal.lexeme);
    BinaryExpr e2 = (BinaryExpr) e1.rhs;
    assertEquals("+", e2.binaryOp.lexeme);
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e2.lhs).rvalue;
    SimpleRValue v3 = (SimpleRValue) ((BasicExpr) e2.rhs).rvalue;
    assertEquals("1", v2.literal.lexeme);
    assertEquals("2", v3.literal.lexeme);
  }

  @Test
  void twoBinaryOperatorsNoParens() {
    var p = """
        void main() {
          x = 1 / 2 + 3
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    BinaryExpr e1 = (BinaryExpr) s.expr;
    assertEquals("/", e1.binaryOp.lexeme);
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e1.lhs).rvalue;
    assertEquals("1", v1.literal.lexeme);
    BinaryExpr e2 = (BinaryExpr) e1.rhs;
    assertEquals("+", e2.binaryOp.lexeme);
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e2.lhs).rvalue;
    SimpleRValue v3 = (SimpleRValue) ((BasicExpr) e2.rhs).rvalue;
    assertEquals("2", v2.literal.lexeme);
    assertEquals("3", v3.literal.lexeme);
  }

  @Test
  void emptyCallExpression() {
    var p = """
        void main() {
          f()
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    CallRValue e = (CallRValue) r.functions.get(0).stmts.get(0);
    assertEquals("f", e.funName.lexeme);
    assertEquals(0, e.args.size());
  }

  @Test
  void oneArgCallExpression() {
    var p = """
        void main() {
          f(3)
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    CallRValue e = (CallRValue) r.functions.get(0).stmts.get(0);
    assertEquals("f", e.funName.lexeme);
    assertEquals(1, e.args.size());
    SimpleRValue v = (SimpleRValue) ((BasicExpr) e.args.get(0)).rvalue;
    assertEquals("3", v.literal.lexeme);
  }

  @Test
  void twoArgCallExpression() {
    var p = """
        void main() {
          f(3, 4)
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    CallRValue e = (CallRValue) r.functions.get(0).stmts.get(0);
    assertEquals("f", e.funName.lexeme);
    assertEquals(2, e.args.size());
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e.args.get(0)).rvalue;
    assertEquals("3", v1.literal.lexeme);
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e.args.get(1)).rvalue;
    assertEquals("4", v2.literal.lexeme);
  }

  @Test
  void simpleNewStructExpression() {
    var p = """
        void main() {
          x = new s()
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    NewStructRValue e = (NewStructRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("s", e.structName.lexeme);
    assertEquals(0, e.args.size());
  }

  @Test
  void newStructExpressionWithOneArg() {
    var p = """
        void main() {
          x = new s(true)
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    NewStructRValue e = (NewStructRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("s", e.structName.lexeme);
    assertEquals(1, e.args.size());
    SimpleRValue v = (SimpleRValue) ((BasicExpr) e.args.get(0)).rvalue;
    assertEquals("true", v.literal.lexeme);
  }

  @Test
  void newStructExpressionWithTwoArgs() {
    var p = """
        void main() {
          x = new s(1, "")
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    NewStructRValue e = (NewStructRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("s", e.structName.lexeme);
    assertEquals(2, e.args.size());
    SimpleRValue v1 = (SimpleRValue) ((BasicExpr) e.args.get(0)).rvalue;
    assertEquals("1", v1.literal.lexeme);
    SimpleRValue v2 = (SimpleRValue) ((BasicExpr) e.args.get(1)).rvalue;
    assertEquals("", v2.literal.lexeme);
  }

  @Test
  void newArrayExpressionWithBaseType() {
    var p = """
        void main() {
          x = new int[10]
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    NewArrayRValue e = (NewArrayRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals("int", e.type.lexeme);
    SimpleRValue v = (SimpleRValue) ((BasicExpr) e.arrayExpr).rvalue;
    assertEquals("10", v.literal.lexeme);
  }

  @Test
  void simpleVariableExpressionWithoutArrayAccess() {
    var p = """
        void main() {
          x = y
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    VarRValue e = (VarRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals(1, e.path.size());
    assertEquals("y", e.path.get(0).varName.lexeme);
    assertFalse(e.path.get(0).arrayExpr.isPresent());
  }

  @Test
  void simpleVariableExpressionWithArrayAccess() {
    var p = """
        void main() {
          x = y[0]
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    VarRValue e = (VarRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals(1, e.path.size());
    assertEquals("y", e.path.get(0).varName.lexeme);
    assertTrue(e.path.get(0).arrayExpr.isPresent());
    SimpleRValue v = (SimpleRValue) ((BasicExpr) e.path.get(0).arrayExpr.get()).rvalue;
    assertEquals("0", v.literal.lexeme);
  }

  @Test
  void twoVariablePathExpressionWithoutArrayAccess() {
    var p = """
        void main() {
          x = y.z
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    VarRValue e = (VarRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals(2, e.path.size());
    assertEquals("y", e.path.get(0).varName.lexeme);
    assertFalse(e.path.get(0).arrayExpr.isPresent());
    assertEquals("z", e.path.get(1).varName.lexeme);
    assertFalse(e.path.get(1).arrayExpr.isPresent());
  }

  @Test
  void mixedVariablePathExpression() {
    var p = """
        void main() {
          x = u[2].v.w[1].y
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.get(0).stmts.size());
    AssignStmt s = (AssignStmt) r.functions.get(0).stmts.get(0);
    VarRValue e = (VarRValue) ((BasicExpr) s.expr).rvalue;
    assertEquals(4, e.path.size());
    assertEquals("u", e.path.get(0).varName.lexeme);
    assertTrue(e.path.get(0).arrayExpr.isPresent());
    assertEquals("v", e.path.get(1).varName.lexeme);
    assertFalse(e.path.get(1).arrayExpr.isPresent());
    assertEquals("w", e.path.get(2).varName.lexeme);
    assertTrue(e.path.get(2).arrayExpr.isPresent());
    assertEquals("y", e.path.get(3).varName.lexeme);
    assertFalse(e.path.get(3).arrayExpr.isPresent());
  }

  // ----------------------------------------------------------------------
  // TODO: Design and implement the following unit tests and add them
  // below. Make sure your added unit tests pass.
  //
  // 1. Five new "positive" tests. Each test should involve an
  // "interesting" syntax case. Your tests must be similar to above
  // such that the AST objects returned are fully checked.
  //
  // ----------------------------------------------------------------------

  @Test
  void nestedIfElseWithinWhile() {
    var p = """
        void main() {
          while true {
            if false { x = 1 }
            else if true { x = 2 }
            else { x = 3 }
          }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.size());
    assertEquals("main", r.functions.get(0).funName.lexeme);
    assertEquals(1, r.functions.get(0).stmts.size());
    WhileStmt whileStmt = (WhileStmt) r.functions.get(0).stmts.get(0);
    // Check while condition literal
    BasicExpr whileCond = (BasicExpr) whileStmt.condition;
    assertEquals("true", ((SimpleRValue) whileCond.rvalue).literal.lexeme);
    // While body should have one if statement.
    assertEquals(1, whileStmt.stmts.size());
    IfStmt ifStmt = (IfStmt) whileStmt.stmts.get(0);
    BasicExpr ifCond = (BasicExpr) ifStmt.condition;
    assertEquals("false", ((SimpleRValue) ifCond.rvalue).literal.lexeme);
    // No inner if body statements
    assertEquals(1, ifStmt.ifStmts.size());
    // Check else-if branch for the if statement
    assertTrue(ifStmt.elseIf.isPresent());
    IfStmt elseIf = (IfStmt) ifStmt.elseIf.get();
    BasicExpr elseIfCond = (BasicExpr) elseIf.condition;
    assertEquals("true", ((SimpleRValue) elseIfCond.rvalue).literal.lexeme);
    // Else branch exists with one assignment
    assertTrue(elseIf.elseStmts.isPresent());
    assertEquals(1, elseIf.elseStmts.get().size());
    // Verify final assignment in else branch
    AssignStmt asgn = (AssignStmt) elseIf.elseStmts.get().get(0);
    assertEquals("x", asgn.lvalue.get(0).varName.lexeme);
    BasicExpr rhs = (BasicExpr) asgn.expr;
    assertEquals("3", ((SimpleRValue) rhs.rvalue).literal.lexeme);
  }

  @Test
  void complexArithmeticExpression() {
    var p = """
        void main() {
          x = ((1 + 2) * (3 - 4)) / 5
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.size());
    AssignStmt asgn = (AssignStmt) r.functions.get(0).stmts.get(0);
    // Entire expression is division
    BinaryExpr divExpr = (BinaryExpr) asgn.expr;
    assertEquals("/", divExpr.binaryOp.lexeme);
    // LHS: multiplication expression from (1+2) * (3-4)
    BinaryExpr multExpr = (BinaryExpr) divExpr.lhs;
    assertEquals("*", multExpr.binaryOp.lexeme);
    // Left factor of multiplication: (1+2)
    BinaryExpr addExpr = (BinaryExpr) multExpr.lhs;
    assertEquals("+", addExpr.binaryOp.lexeme);
    assertEquals("1", ((SimpleRValue) ((BasicExpr) addExpr.lhs).rvalue).literal.lexeme);
    assertEquals("2", ((SimpleRValue) ((BasicExpr) addExpr.rhs).rvalue).literal.lexeme);
    // Right factor of multiplication: (3-4)
    BinaryExpr subExpr = (BinaryExpr) multExpr.rhs;
    assertEquals("-", subExpr.binaryOp.lexeme);
    assertEquals("3", ((SimpleRValue) ((BasicExpr) subExpr.lhs).rvalue).literal.lexeme);
    assertEquals("4", ((SimpleRValue) ((BasicExpr) subExpr.rhs).rvalue).literal.lexeme);
    // Right operand of division is literal 5.
    assertEquals("5", ((SimpleRValue) ((BasicExpr) divExpr.rhs).rvalue).literal.lexeme);
  }

  @Test
  void nestedFunctionCallExpressions() {
    var p = """
        void main() {
          f(g(1, 2), h())
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.size());
    // The main function should have one statement: a call statement.
    CallRValue outerCall = (CallRValue) r.functions.get(0).stmts.get(0);
    assertEquals("f", outerCall.funName.lexeme);
    assertEquals(2, outerCall.args.size());
    // Check first argument: g(1, 2)
    CallRValue innerCallG = (CallRValue) ((BasicExpr) outerCall.args.get(0)).rvalue;
    assertEquals("g", innerCallG.funName.lexeme);
    assertEquals(2, innerCallG.args.size());
    assertEquals("1", ((SimpleRValue) ((BasicExpr) innerCallG.args.get(0)).rvalue).literal.lexeme);
    assertEquals("2", ((SimpleRValue) ((BasicExpr) innerCallG.args.get(1)).rvalue).literal.lexeme);
    // Check second argument: h()
    CallRValue innerCallH = (CallRValue) ((BasicExpr) outerCall.args.get(1)).rvalue;
    assertEquals("h", innerCallH.funName.lexeme);
    assertEquals(0, innerCallH.args.size());
  }

  @Test
  void complexNestedStructureWithMixedOperations() {
    var p = """
        void main() {
            ptr1.next[f(x) + g(y)].data[(a and b) or not (c)].value = new node(
                x.left[i + j].data,
                ptr2.values[k * 2].next[(x >= y) and (z <= w)],
                f(g(1, h(2, k())), m(n[0].x))
            )
            while ((x or y) and z) or (not (v) and not (((v == 0) or not (x)))) {
                arr[i + j].field.data[k - 1].value = (40 / (10 / 2))
            }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    assertEquals(1, r.functions.size());
    assertEquals(2, r.functions.get(0).stmts.size());
    // First statement should be a complex assignment
    AssignStmt assign = (AssignStmt) r.functions.get(0).stmts.get(0);
    assertEquals(4, assign.lvalue.size()); // ptr1.next[...].data[...].value
    // Second statement should be a while loop
    WhileStmt whileStmt = (WhileStmt) r.functions.get(0).stmts.get(1);
    assertTrue(whileStmt.condition instanceof BinaryExpr);
    assertEquals(1, whileStmt.stmts.size());
  }

  @Test
  void recursiveStructureAccess() {
    var p = """
        void main() {
            tree.left.right.data = new Node(
                left.value + right.value,
                null
            )
            if tree.height > maxHeight {
                tree.parent.balance = (left.depth - right.depth)
            }
        }
        """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    // Verify program structure
    assertEquals(1, r.functions.size());
    assertEquals(2, r.functions.get(0).stmts.size());
    // Verify first assignment with nested field access
    AssignStmt assign = (AssignStmt) r.functions.get(0).stmts.get(0);
    assertEquals(4, assign.lvalue.size()); // tree.left.right.data
    // Verify new struct creation
    NewStructRValue newNode = (NewStructRValue) ((BasicExpr) assign.expr).rvalue;
    assertEquals("Node", newNode.structName.lexeme);
    assertEquals(2, newNode.args.size());
    // Verify if statement with field access condition
    IfStmt ifStmt = (IfStmt) r.functions.get(0).stmts.get(1);
    assertTrue(ifStmt.condition instanceof BinaryExpr);
    assertEquals(1, ifStmt.ifStmts.size());
  }

}
