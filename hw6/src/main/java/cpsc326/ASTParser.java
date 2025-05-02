/**
 * CPSC 326, Spring 2025
 * The AST Parser implementation.
 *
 * author: Arjuna Herbst
 */

package cpsc326;

import java.util.List;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Simple recursive descent parser for checking program syntax.
 */
public class ASTParser {

  private Lexer lexer; // the lexer
  private Token currToken; // the current token

  /**
   * Create a SimpleParser from the give lexer.
   * 
   * @param lexer The lexer for the program to parse.
   */
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Run the parser.
   */
  public Program parse() {
    advance();
    Program p = program();
    eat(TokenType.EOS, "expecting end of file");
    return p;
  }

  /**
   * Generate and throw a mypl parser exception.
   * 
   * @param msg The error message.
   */
  private void error(String msg) {
    String lexeme = currToken.lexeme;
    int line = currToken.line;
    int column = currToken.column;
    String s = "[%d,%d] %s found '%s'";
    MyPLException.parseError(String.format(s, line, column, msg, lexeme));
  }

  /**
   * Move to the next lexer token, skipping comments.
   */
  private void advance() {
    currToken = lexer.nextToken();
    while (match(TokenType.COMMENT))
      currToken = lexer.nextToken();
  }

  /**
   * Checks that the current token has the given token type.
   * 
   * @param targetTokenType The token type to check against.
   * @return True if the types match, false otherwise.
   */
  private boolean match(TokenType targetTokenType) {
    return currToken.tokenType == targetTokenType;
  }

  /**
   * Checks that the current token is contained in the given list of
   * token types.
   * 
   * @param targetTokenTypes The token types ot check against.
   * @return True if the current type is in the given list, false
   *         otherwise.
   */
  private boolean matchAny(List<TokenType> targetTokenTypes) {
    return targetTokenTypes.contains(currToken.tokenType);
  }

  /**
   * Advance to next token if current token matches the given token type.
   * 
   * @param targetType The token type to check against.
   */
  private void eat(TokenType targetTokenType, String msg) {
    if (!match(targetTokenType))
      error(msg);
    advance();
  }

  /**
   * Helper to check that the current token is a binary operator.
   */
  private boolean isBinOp() {
    return matchAny(List.of(TokenType.PLUS, TokenType.MINUS, TokenType.TIMES,
        TokenType.DIVIDE, TokenType.AND, TokenType.OR,
        TokenType.EQUAL, TokenType.LESS, TokenType.GREATER,
        TokenType.LESS_EQ, TokenType.GREATER_EQ,
        TokenType.NOT_EQUAL));
  }

  /**
   * Helper to check that the current token is a literal value.
   */
  private boolean isLiteral() {
    return matchAny(List.of(TokenType.INT_VAL, TokenType.DOUBLE_VAL,
        TokenType.STRING_VAL, TokenType.BOOL_VAL,
        TokenType.NULL_VAL));
  }

  /**
   * Helper to check that the current token is one of:
   * INT_TYPE | DOUBLE_TYPE | STRING_TYPE | BOOL_TYPE
   */
  private boolean isBaseType() {
    return matchAny(List.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE,
        TokenType.STRING_TYPE, TokenType.BOOL_TYPE));
  }

  private boolean isFieldStart() {
    return match(TokenType.ID) || isBaseType();
  }

  /**
   * Parse the program
   * 
   * @return the corresponding Program AST object
   */
  private Program program() {
    Program prog = new Program();

    while (!match(TokenType.EOS)) { // read until end of stream
      if (match(TokenType.STRUCT)) { // append struct definition or function definition to program object
        prog.structs.add(structDef());
      } else {
        prog.functions.add(funDef());
      }
    }
    eat(TokenType.EOS, "expecting end-of-stream");
    return prog;
  }

  private StructDef structDef() {
    StructDef sdecl = new StructDef();
    eat(TokenType.STRUCT, "expecting 'struct'");
    sdecl.structName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LBRACE, "expecting '{'");
    sdecl.fields = new ArrayList<>();
    while (isFieldStart()) {
      sdecl.fields.add(varDef());
      if (match(TokenType.COMMA)) {
        advance();
      } else {
        break;
      }
    }
    eat(TokenType.RBRACE, "expecting '}'");
    return sdecl;
  }

  private FunDef funDef() {
    FunDef fdecl = new FunDef();
    fdecl.returnType = returnTypeDef();
    fdecl.funName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LPAREN, "expecting '('");
    fdecl.params = paramsDef();
    eat(TokenType.RPAREN, "expecting ')'");
    fdecl.stmts = blockDef();
    return fdecl;
  }

  private DataType returnTypeDef() {
    DataType dtype = new DataType();
    if (match(TokenType.VOID_TYPE)) {
      dtype.type = currToken;
      dtype.isArray = false;
      advance();
    } else {
      dtype = dataTypeDef();
    }
    return dtype;
  }

  private List<VarDef> paramsDef() {
    List<VarDef> params = new ArrayList<>();
    if (match(TokenType.ID)) {
      params.add(varDef());
      while (match(TokenType.COMMA)) {
        advance();
        params.add(varDef());
      }
    }
    return params;
  }

  private VarDef varDef() {
    VarDef vdecl = new VarDef();
    vdecl.varName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.COLON, "expecting ':'");
    vdecl.dataType = dataTypeDef();
    return vdecl;
  }

  private DataType dataTypeDef() {
    DataType dtype = new DataType();
    if (match(TokenType.LBRACKET)) {
      dtype.isArray = true;
      advance();
      dtype.type = currToken;
      if (isBaseType() || match(TokenType.ID)) {
        advance();
      } else {
        error("expecting type");
      }
      eat(TokenType.RBRACKET, "expecting ']'");
    } else {
      dtype.isArray = false;
      dtype.type = currToken;
      if (isBaseType() || match(TokenType.ID)) {
        advance();
      } else {
        error("expecting type");
      }
    }
    return dtype;
  }

  private List<Stmt> blockDef() {
    List<Stmt> stmts = new ArrayList<>();
    eat(TokenType.LBRACE, "expecting '{'");
    while (!match(TokenType.RBRACE)) {
      stmts.add(stmtDef());
    }
    eat(TokenType.RBRACE, "expecting '}'");
    return stmts;
  }

  private Stmt stmtDef() {
    if (match(TokenType.VAR)) {
      return varStmtDef();
    } else if (match(TokenType.WHILE)) {
      return whileStmtDef();
    } else if (match(TokenType.IF)) {
      return ifStmtDef();
    } else if (match(TokenType.FOR)) {
      return forStmtDef();
    } else if (match(TokenType.RETURN)) {
      return returnStmtDef();
    } else if (match(TokenType.ID)) {
      Token idToken = currToken;
      advance();
      if (match(TokenType.LPAREN)) {
        return funCallDef(idToken);
      } else {
        return assignStmtDef(idToken);
      }
    } else {
      error("expecting statement");
      return null; // unreachable
    }
  }

  private VarStmt varStmtDef() {
    VarStmt vstmt = new VarStmt();
    eat(TokenType.VAR, "expecting 'var'");
    vstmt.varName = currToken;
    eat(TokenType.ID, "expecting identifier");
    if (match(TokenType.COLON)) {
      advance();
      vstmt.dataType = Optional.of(dataTypeDef());
    }
    if (match(TokenType.ASSIGN)) {
      advance();
      vstmt.expr = Optional.of(exprDef());
    }
    return vstmt;
  }

  private WhileStmt whileStmtDef() {
    WhileStmt wstmt = new WhileStmt();
    eat(TokenType.WHILE, "expecting 'while'");
    wstmt.condition = exprDef();
    wstmt.stmts = blockDef();
    return wstmt;
  }

  private IfStmt ifStmtDef() {
    IfStmt istmt = new IfStmt();
    eat(TokenType.IF, "expecting 'if'");
    istmt.condition = exprDef();
    istmt.ifStmts = blockDef();
    if (match(TokenType.ELSE)) {
      advance();
      if (match(TokenType.IF)) {
        istmt.elseIf = Optional.of(ifStmtDef());
      } else {
        istmt.elseStmts = Optional.of(blockDef());
      }
    }
    return istmt;
  }

  private ForStmt forStmtDef() {
    ForStmt fstmt = new ForStmt();
    eat(TokenType.FOR, "expecting 'for'");
    fstmt.varName = currToken;
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.FROM, "expecting 'from'");
    fstmt.fromExpr = exprDef();
    eat(TokenType.TO, "expecting 'to'");
    fstmt.toExpr = exprDef();
    fstmt.stmts = blockDef();
    return fstmt;
  }

  private ReturnStmt returnStmtDef() {
    ReturnStmt rstmt = new ReturnStmt();
    eat(TokenType.RETURN, "expecting 'return'");
    rstmt.expr = exprDef();
    return rstmt;
  }

  private AssignStmt assignStmtDef(Token idToken) {
    AssignStmt astmt = new AssignStmt();
    VarRef varRef = new VarRef();
    varRef.varName = idToken;
    astmt.lvalue.add(varRef);
    while (match(TokenType.DOT) || match(TokenType.LBRACKET)) {
      if (match(TokenType.DOT)) {
        advance();
        varRef = new VarRef();
        varRef.varName = currToken;
        eat(TokenType.ID, "expecting identifier");
        astmt.lvalue.add(varRef);
      } else if (match(TokenType.LBRACKET)) {
        advance();
        varRef.arrayExpr = Optional.of(exprDef());
        eat(TokenType.RBRACKET, "expecting ']'");
      }
    }
    eat(TokenType.ASSIGN, "expecting '='");
    astmt.expr = exprDef();
    return astmt;
  }

  private CallRValue funCallDef(Token idToken) {
    CallRValue cstmt = new CallRValue();
    cstmt.funName = idToken;
    eat(TokenType.LPAREN, "expecting '('");
    if (!match(TokenType.RPAREN)) {
      cstmt.args.add(exprDef());
      while (match(TokenType.COMMA)) {
        advance();
        cstmt.args.add(exprDef());
      }
    }
    eat(TokenType.RPAREN, "expecting ')'");
    return cstmt;
  }

  private Expr exprDef() {
    Expr expr;
    if (match(TokenType.NOT)) {
        UnaryExpr uexpr = new UnaryExpr();
        uexpr.unaryOp = currToken;
        advance();
        uexpr.expr = exprDef();
        expr = uexpr;
    } else if (match(TokenType.LPAREN)) {
        advance();
        expr = exprDef();
        eat(TokenType.RPAREN, "expecting ')'");
    } else {
        RValue rvalue = rvalueDef();
        BasicExpr bexpr = new BasicExpr();
        bexpr.rvalue = rvalue;
        expr = bexpr;
    }
    if (isBinOp()) {
        BinaryExpr bexpr = new BinaryExpr();
        bexpr.lhs = expr;
        bexpr.binaryOp = currToken;
        advance();
        bexpr.rhs = exprDef();
        expr = bexpr;
    }
    return expr;
}

  private RValue rvalueDef() {
    if (isLiteral()) {
        SimpleRValue svalue = new SimpleRValue();
        svalue.literal = currToken;
        advance();
        return svalue;
    } else if (match(TokenType.NEW)) {
        return newRvalueDef();
    } else if (match(TokenType.ID)) {
        Token idToken = currToken;
        advance();
        if (match(TokenType.LPAREN)) {
            return funCallDef(idToken);
        } else {
            VarRValue vvalue = new VarRValue();
            VarRef varRef = new VarRef();
            varRef.varName = idToken;
            vvalue.path.add(varRef);
            while (match(TokenType.DOT) || match(TokenType.LBRACKET)) {
                if (match(TokenType.DOT)) {
                    advance();
                    varRef = new VarRef();
                    varRef.varName = currToken;
                    eat(TokenType.ID, "expecting identifier");
                    vvalue.path.add(varRef);
                } else if (match(TokenType.LBRACKET)) {
                    advance();
                    varRef.arrayExpr = Optional.of(exprDef());
                    eat(TokenType.RBRACKET, "expecting ']'");
                }
            }
            return vvalue;
        }
    } else {
        error("expecting rvalue");
        return null; // unreachable
    }
  }

  private NewRValue newRvalueDef() {
    eat(TokenType.NEW, "expecting 'new'");
    if (match(TokenType.ID) || isBaseType()) {
      Token typeToken = currToken;
      advance();
      if (match(TokenType.LPAREN)) {
        NewStructRValue nvalue = new NewStructRValue();
        nvalue.structName = typeToken;
        advance();
        if (!match(TokenType.RPAREN)) {
          nvalue.args.add(exprDef());
          while (match(TokenType.COMMA)) {
            advance();
            nvalue.args.add(exprDef());
          }
        }
        eat(TokenType.RPAREN, "expecting ')'");
        return nvalue;
      } else if (match(TokenType.LBRACKET)) {
        NewArrayRValue nvalue = new NewArrayRValue();
        nvalue.type = typeToken;
        advance();
        nvalue.arrayExpr = exprDef();
        eat(TokenType.RBRACKET, "expecting ']'");
        return nvalue;
      } else {
        error("expecting '(' or '[' after type in new expression");
        return null; // unreachable
      }
    } else {
      error("expecting type");
      return null; // unreachable
    }
  }
}
