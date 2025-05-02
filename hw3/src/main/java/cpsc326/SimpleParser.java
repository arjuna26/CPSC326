/**
 * CPSC 326, Spring 2025
 * The Simple Parser implementation.
 *
 * @author Arjuna Herbst
 */

package cpsc326;

import java.util.List;

/**
 * Simple recursive descent parser for checking program syntax.
 */
public class SimpleParser {

  private Lexer lexer; // the lexer
  private Token currToken; // the current token

  /**
   * Create a SimpleParser from the give lexer.
   * 
   * @param lexer The lexer for the program to parse.
   */
  public SimpleParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Run the parser.
   */
  public void parse() {
    advance();
    program();
    eat(TokenType.EOS, "expecting end of file");
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
   * @param targetTokenType The token type to check against.
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
   * CHECKS
   */

  /**
   * Checks for a valid program.
   */
  private void program() {
    // <program> ::= ( <struct_def> | <fun_def> )∗
    while (!match(TokenType.EOS)) {
      if (match(TokenType.STRUCT)) {
        structDef();
      } else {
        funDef();
      }
    }
    eat(TokenType.EOS, "expecting end-of-stream");
  }

  /**
   * Checks for a valid struct definition.
   */
  private void structDef() {
    // <struct_def> ::= STRUCT ID LBRACE <fields> RBRACE
    eat(TokenType.STRUCT, "struct tokentype error");
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LBRACE, "expecting '{'");
    if (!match(TokenType.RBRACE)) {
      fieldsDef();
    }
    eat(TokenType.RBRACE, "expecting '}'");

  }

  private void fieldsDef() {
    // <fields> ::= <field> ( COMMA <field> )∗|ϵ
    if (isFieldStart()) {
      fieldDef();
      while (match(TokenType.COMMA)) {
        advance(); // consume comma token
        fieldDef();
      }
    }
    // if ϵ, continue
  }

  private void fieldDef() {
    // <field> ::= ID : <data_type>
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.COLON, "expecting ':'");
    dataTypeDef();
  }

  private void dataTypeDef() {
    // <data_type> ::= <base_type> | ID | LBRACKET ( <base_type> | ID ) RBRACKET
    if (match(TokenType.LBRACKET)) {
      advance();
      if (isBaseType()) {
        baseTypeDef();
      } else if (match(TokenType.ID)) {
        advance();
      } else {
        error("expecting type");
      }
      eat(TokenType.RBRACKET, "expecting '['");
    } else if (isBaseType()) {
      baseTypeDef();
    } else if (match(TokenType.ID)) {
      advance();
    } else {
      error("expecting type");
    }
  }

  private void baseTypeDef() {
    // <base_type> ::= INT_TYPE | DOUBLE_TYPE | STRING_TYPE | BOOL_TYPE | CHAR_TYPE
    if (isBaseType()) {
      advance();
    } else {
      error("expecting DTYPE");
    }
  }

  /**
   * checks for a valid function definition.
   */
  private void funDef() {
    // <fun_def> ::= <return_type> ID LPAREN <params> RPAREN <block>
    returnTypeDef();
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.LPAREN, "expecting '('");
    paramsDef();
    eat(TokenType.RPAREN, "expecting ')'");
    blockDef();
    // advance();
  }

  private void returnTypeDef() {
    // <return_type> ::= <data_type> | VOID_TYPE
    if (match(TokenType.VOID_TYPE)) {
      advance();
    } else {
      dataTypeDef();
    }

  }

  private void paramsDef() {
    // <params> ::= <param> ( COMMA <param> )∗ | ε
    if (match(TokenType.ID)) {
      paramDef();
      while (match(TokenType.COMMA)) {
        advance();
        paramDef();
      }
    }
  }

  private void paramDef() {
    // <param> ::= ID : <data_type>
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.COLON, "expecting ':'");
    dataTypeDef();
  }

  private void blockDef() {
    // <block> ::= LBRACE ( <stmt> )∗ RBRACE
    eat(TokenType.LBRACE, "expecting '{'");
    // check for stmt*
    while (matchAny(List.of(TokenType.VAR, TokenType.COLON, TokenType.ASSIGN, TokenType.WHILE, TokenType.IF,
        TokenType.FOR, TokenType.RETURN, TokenType.ID))) {
      stmtDef();
    }

    eat(TokenType.RBRACE, "expecting statement");

  }

  private void stmtDef() {
    // <stmt> ::= <var_stmt> | <while_stmt> | <if_stmt> | <for_stmt> | <return_stmt> | <assign_stmt> | <fun_call>
    if (match(TokenType.VAR)) {
      varStmtDef();
    } else if (match(TokenType.WHILE)) {
      whileStmtDef();
    } else if (match(TokenType.IF)) {
      ifStmtDef();
    } else if (match(TokenType.FOR)) {
      forStmtDef();
    } else if (match(TokenType.RETURN)) {
      returnStmtDef();
    } else if (match(TokenType.ID)) {
      advance(); // advance past the ID
      if (match(TokenType.LPAREN)) {
        funCallDef();
      } else {
        varRvalueDef();
        eat(TokenType.ASSIGN, "expecting '='");
        exprDef();
      }
    } else {
      error("missing stmt");
    }
  }

  private void varStmtDef() {
    // VAR ID ( <var_init> | <var_type> ( <var_init> | ε ) )
    eat(TokenType.VAR, "expecting 'var'");
    eat(TokenType.ID, "expecting identifier");
    if (match(TokenType.ASSIGN)) {
      varInitDef();
    } else if (match(TokenType.COLON)) {
      varTypeDef();
      if (match(TokenType.ASSIGN)) {
        varInitDef();
      }
    } else {
      error("expecting ':'");
    }
  }

  private void varInitDef() {
    // <var_init> ::= ASSIGN <expr>
    eat(TokenType.ASSIGN, "expecting '='");
    exprDef();
  }

  private void exprDef() {
    // <expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <bin_op> <expr> | ε )
    if (match(TokenType.NOT)) {
      advance(); // advance past NOT
      exprDef();
      if (isBinOp()) {
        advance();
        exprDef();
      }
    } else if (match(TokenType.LPAREN)) {
      advance(); // advance past LPAREN
      exprDef();
      eat(TokenType.RPAREN, "expecting ')'");
      if (isBinOp()) {
        advance();
        exprDef();
      }
    } else {
      rvalueDef();
      if (isBinOp()) {
        advance();
        exprDef();
      }
    }
  }

  private void rvalueDef() {
    // <rvalue> ::= <literal> | <new_rvalue> | <var_rvalue> | <fun_call>
    if (isLiteral()) {
      advance();
    } else if (match(TokenType.NEW)) {
      newRvalueDef();
    } else if (match(TokenType.ID)) {
      advance();
      if (match(TokenType.LPAREN)) {
        funCallDef();
      } else {
        varRvalueDef();
      }
    } else {
      error("expecting identifier");
    }
  }

  private void varRvalueDef() {
    // <var_rvalue> ::= ID ( LBRACKET <expr> RBRACKET | ε ) ( DOT ID ( LBRACKET <expr> RBRACKET | ε ) )∗
    if (match(TokenType.LBRACKET)) {
      advance();
      exprDef();
      eat(TokenType.RBRACKET, "expecting ']'");
    }
    while (match(TokenType.DOT)) {
      advance();
      eat(TokenType.ID, "expecting identifier");
      if (match(TokenType.LBRACKET)) {
        advance();
        exprDef();
        eat(TokenType.RBRACKET, "expecting ']'");
      }
    }
  }

  private void newRvalueDef() {
    // <new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
    eat(TokenType.NEW, "expecting 'new'");
    if (match(TokenType.ID)) {
      advance();
      if (match(TokenType.LPAREN)) {
        advance();
        argsDef();
        eat(TokenType.RPAREN, "missing closing RPAREN");
      } else if (match(TokenType.LBRACKET)) {
        advance();
        exprDef();
        eat(TokenType.RBRACKET, "expecting ']'");
      }
    } else if (isBaseType()) {
      advance();
      eat(TokenType.LBRACKET, "expecting '['");
      exprDef();
      eat(TokenType.RBRACKET, "expecting ']'");
    }
  }

  private void argsDef() {
    // <args> ::= <expr> ( COMMA <expr> )* | ε
    if (!match(TokenType.RPAREN)) { // Check for ε case
      exprDef();
      while (match(TokenType.COMMA)) {
        advance();
        exprDef();
      }
    }
  }

  private void varTypeDef() {
    // <var_type> ::= COLON <data_type>
    eat(TokenType.COLON, "expecting COLON");
    dataTypeDef();
  }

  private void whileStmtDef() {
    // <while_stmt> ::= WHILE <expr> <block>
    advance();
    exprDef();
    blockDef();
  }

  private void ifStmtDef() {
    // <if_stmt> ::= IF <expr> <block> ( ELSE ( <if_stmt> | <block> ) | ε )
    advance();
    exprDef();
    blockDef();
    if (match(TokenType.ELSE)) {
      advance();
      if (match(TokenType.IF)) {
        ifStmtDef();
      } else {
        blockDef();
      }
    }
  }

  private void forStmtDef() {
    // <for_stmt> ::= FOR ID FROM <expr> TO <expr> <block>
    advance();
    eat(TokenType.ID, "expecting identifier");
    eat(TokenType.FROM, "expecting 'from'");
    exprDef();
    eat(TokenType.TO, "expecting 'to'");
    exprDef();
    blockDef();
  }

  private void returnStmtDef() {
    // <return_stmt> ::= RETURN <expr>
    advance();
    exprDef();
  }

  private void funCallDef() {
    // <fun_call> ::= ID LPAREN <args> RPAREN
    eat(TokenType.LPAREN, "expecting LPAREN");
    argsDef();
    eat(TokenType.RPAREN, "missing closing RPAREN");
  }

}