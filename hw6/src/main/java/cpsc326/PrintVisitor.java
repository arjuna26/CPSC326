/**
 * CPSC 326, Spring 2025
 * Pretty print visitor.
 *
 * Arjuna Herbst 
 */

package cpsc326;

public class PrintVisitor implements Visitor {

  private int indent = 0;

  /**
   * Prints message without ending newline
   */
  private void write(String s) {
    System.out.print(s);
  }

  /**
   * Increase the indent level by one
   */
  private void incIndent() {
    indent++;
  }

  /**
   * Decrease the indent level by one
   */
  private void decIndent() {
    indent--;
  }

  /**
   * Print an initial indent string
   */
  private String indent() {
    return "  ".repeat(indent);
  }

  /**
   * Prints a newline
   */
  private void newline() {
    System.out.println();
  }

  /**
   * Prints the program
   */
  public void visit(Program node) {
    // always one blank line at the "top"
    newline();
    for (StructDef s : node.structs)
      s.accept(this);
    for (FunDef f : node.functions)
      f.accept(this);
  }

  @Override
  public void visit(StructDef node) {
    write("struct " + node.structName.lexeme + " {");
    newline();
    incIndent();
    for (int i = 0; i < node.fields.size(); i++) {
      write(indent());
      node.fields.get(i).accept(this);
      if (i < node.fields.size() - 1) {
        write(", ");
      }
      newline();
    }
    decIndent();
    write("}");
    newline();
    newline();
  }

  @Override
  public void visit(FunDef node) {
    write(node.returnType.type.lexeme + " " + node.funName.lexeme + "(");
    for (int i = 0; i < node.params.size(); i++) {
      node.params.get(i).accept(this);
      if (i < node.params.size() - 1) {
        write(", ");
      }
    }
    write(") {");
    newline();
    incIndent();
    for (Stmt stmt : node.stmts) {
      write(indent());
      stmt.accept(this);
      newline();
    }
    decIndent();
    write("}");
    newline();
    newline();
  }

  @Override
  public void visit(VarDef node) {
    write(node.varName.lexeme + ": " + node.dataType.type.lexeme);
  }

  @Override
  public void visit(DataType node) {
    if (node.isArray) {
      write("[" + node.type.lexeme + "]");
    } else {
      write(node.type.lexeme);
    }
  }

  @Override
  public void visit(VarStmt node) {
    write("var " + node.varName.lexeme);
    if (node.dataType.isPresent()) {
      write(": ");
      node.dataType.get().accept(this);
    }
    if (node.expr.isPresent()) {
      write(" = ");
      node.expr.get().accept(this);
    }
  }

  @Override
  public void visit(AssignStmt node) {
    for (int i = 0; i < node.lvalue.size(); i++) {
      VarRef varRef = node.lvalue.get(i);
      write(varRef.varName.lexeme);
      if (varRef.arrayExpr.isPresent()) {
        write("[");
        varRef.arrayExpr.get().accept(this);
        write("]");
      }
      if (i < node.lvalue.size() - 1) {
        write(".");
      }
    }
    write(" = ");
    node.expr.accept(this);
  }

  @Override
  public void visit(WhileStmt node) {
    write("while ");
    node.condition.accept(this);
    write(" {");
    newline();
    incIndent();
    for (Stmt stmt : node.stmts) {
      write(indent());
      stmt.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");
  }

  @Override
  public void visit(ForStmt node) {
    write("for " + node.varName.lexeme + " from ");
    node.fromExpr.accept(this);
    write(" to ");
    node.toExpr.accept(this);
    write(" {");
    newline();
    incIndent();
    for (Stmt stmt : node.stmts) {
      write(indent());
      stmt.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");
  }

  @Override
  public void visit(IfStmt node) {
    write("if ");
    node.condition.accept(this);
    write(" {");
    newline();
    incIndent();
    for (Stmt stmt : node.ifStmts) {
      write(indent());
      stmt.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");

    if (node.elseIf.isPresent()) {
      write("\n" + indent() + "else ");
      node.elseIf.get().accept(this);
    } else if (node.elseStmts.isPresent()) {
      write("\n" + indent() + "else {");
      newline();
      incIndent();
      for (Stmt stmt : node.elseStmts.get()) {
        write(indent());
        stmt.accept(this);
        newline();
      }
      decIndent();
      write(indent() + "}");
    }
  }

  @Override
  public void visit(ReturnStmt node) {
    write("return ");
    node.expr.accept(this);
  }

  @Override
  public void visit(VarRValue node) {
    for (int i = 0; i < node.path.size(); i++) {
      VarRef varRef = node.path.get(i);
      write(varRef.varName.lexeme);
      if (varRef.arrayExpr.isPresent()) {
        write("[");
        varRef.arrayExpr.get().accept(this);
        write("]");
      }
      if (i < node.path.size() - 1) {
        write(".");
      }
    }
  }

  @Override
  public void visit(SimpleRValue node) {
    if (node.literal.tokenType == TokenType.STRING_VAL) {
      write("\"" + node.literal.lexeme + "\"");
    } else {
      write(node.literal.lexeme);
    }
  }

  @Override
  public void visit(NewStructRValue node) {
    write("new " + node.structName.lexeme + "(");
    for (int i = 0; i < node.args.size(); i++) {
      node.args.get(i).accept(this);
      if (i < node.args.size() - 1) {
        write(", ");
      }
    }
    write(")");
  }

  @Override
  public void visit(NewArrayRValue node) {
    write("new " + node.type.lexeme + "[");
    node.arrayExpr.accept(this);
    write("]");
  }

  @Override
  public void visit(CallRValue node) {
    write(node.funName.lexeme + "(");
    for (int i = 0; i < node.args.size(); i++) {
      node.args.get(i).accept(this);
      if (i < node.args.size() - 1) {
        write(", ");
      }
    }
    write(")");
  }

  @Override
  public void visit(BinaryExpr node) {
    write("(");
    node.lhs.accept(this);
    write(" " + node.binaryOp.lexeme + " ");
    node.rhs.accept(this);
    write(")");
  }

  @Override
  public void visit(UnaryExpr node) {
    write(node.unaryOp.lexeme + " ");
    if (node.unaryOp.lexeme.equals("not")) {
      write("(");
    }
    node.expr.accept(this);
    if (node.unaryOp.lexeme.equals("not")) {
      write(")");
    }
  }

  @Override
  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }
}
