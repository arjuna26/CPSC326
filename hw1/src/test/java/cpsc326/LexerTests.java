/**
 * CPSC 326, Spring 2025
 * Basic lexer tests.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 */
class LexerTests {

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
  // POSITIVE TEST CASES

  @Test
  void emptyInput() {
    var p = "";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.tokenType);
    assertEquals(1, t.line);
    assertEquals(0, t.column);
  }

  @Test
  void oneSymbol() {
    var p = ".";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.DOT, t.tokenType);
    assertEquals(".", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
  }

  @Test
  void oneSymbolThenEOS() {
    var p = "(";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.LPAREN, t.tokenType);
    assertEquals("(", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.tokenType);
    assertEquals("end-of-stream", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(2, t.column);
  }

  @Test
  void oneCommentThenEOS() {
    var p = "# a comment";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.COMMENT, t.tokenType);
    assertEquals(" a comment", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.tokenType);
    assertEquals("end-of-stream", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(12, t.column);
  }

  @Test
  void twoComments() {
    var p = """
        # a comment
        # another comment
        """;
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.COMMENT, t.tokenType);
    assertEquals(" a comment", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(" another comment", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(1, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void punctuationSymbols() {
    var p = ".:,()[]{}=";
    Lexer lexer = new Lexer(istream(p));
    var types = List.of(TokenType.DOT, TokenType.COLON, TokenType.COMMA,
        TokenType.LPAREN, TokenType.RPAREN, TokenType.LBRACKET,
        TokenType.RBRACKET, TokenType.LBRACE,
        TokenType.RBRACE, TokenType.ASSIGN);
    var lexemes = List.of(".", ":", ",", "(", ")", "[", "]", "{", "}", "=");
    for (int i = 0; i < types.size(); ++i) {
      Token t = lexer.nextToken();
      assertEquals(types.get(i), t.tokenType);
      assertEquals(lexemes.get(i), t.lexeme);
      assertEquals(1, t.line);
      assertEquals(i + 1, t.column);
    }
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void arithmeticSymbols() {
    var p = "+-*/";
    Lexer lexer = new Lexer(istream(p));
    var types = List.of(TokenType.PLUS, TokenType.MINUS,
        TokenType.TIMES, TokenType.DIVIDE);
    var lexemes = List.of("+", "-", "*", "/");
    for (int i = 0; i < types.size(); ++i) {
      Token t = lexer.nextToken();
      assertEquals(types.get(i), t.tokenType);
      assertEquals(lexemes.get(i), t.lexeme);
      assertEquals(1, t.line);
      assertEquals(i + 1, t.column);
    }
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void comparatorSymbols() {
    var p = "<><=>=!=";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.LESS, t.tokenType);
    assertEquals("<", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.GREATER, t.tokenType);
    assertEquals(">", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(2, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.LESS_EQ, t.tokenType);
    assertEquals("<=", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(3, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.GREATER_EQ, t.tokenType);
    assertEquals(">=", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.NOT_EQUAL, t.tokenType);
    assertEquals("!=", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(7, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void oneSymbolPerline() {
    var p = ",\n.\n:\n(";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.COMMA, t.tokenType);
    assertEquals(",", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOT, t.tokenType);
    assertEquals(".", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.COLON, t.tokenType);
    assertEquals(":", t.lexeme);
    assertEquals(3, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.LPAREN, t.tokenType);
    assertEquals("(", t.lexeme);
    assertEquals(4, t.line);
    assertEquals(1, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void oneCharacterStrings() {
    var p = "\"a\" \"?\" \"<\"";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("a", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("?", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("<", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(9, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void multiCharacterStrings() {
    var p = "\"abc\" \"><!=\" \"foo bar baz\"";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("abc", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("><!=", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(7, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("foo bar baz", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(14, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void basicIntLiterals() {
    var p = "0 42 10 1 9876543210";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("0", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("42", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(3, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("10", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("1", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(9, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("9876543210", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(11, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void basicDoubleLiterals() {
    var p = "0.0 0.00 3.14 321.1230";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("0.0", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("0.00", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("3.14", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(10, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("321.1230", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(15, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void specialLiterals() {
    var p = "true false null";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.BOOL_VAL, t.tokenType);
    assertEquals("true", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.BOOL_VAL, t.tokenType);
    assertEquals("false", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.NULL_VAL, t.tokenType);
    assertEquals("null", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(12, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void primiteTypeNames() {
    var p = "int double string bool void";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.INT_TYPE, t.tokenType);
    assertEquals("int", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_TYPE, t.tokenType);
    assertEquals("double", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_TYPE, t.tokenType);
    assertEquals("string", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(12, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.BOOL_TYPE, t.tokenType);
    assertEquals("bool", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(19, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.VOID_TYPE, t.tokenType);
    assertEquals("void", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(24, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void logicalOperators() {
    var p = "and or not";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.AND, t.tokenType);
    assertEquals("and", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.OR, t.tokenType);
    assertEquals("or", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.NOT, t.tokenType);
    assertEquals("not", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(8, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void ifStatementReservedWords() {
    var p = "if else";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.IF, t.tokenType);
    assertEquals("if", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ELSE, t.tokenType);
    assertEquals("else", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(4, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void loopStatementReservedWords() {
    var p = "while for from to";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.WHILE, t.tokenType);
    assertEquals("while", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.FOR, t.tokenType);
    assertEquals("for", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(7, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.FROM, t.tokenType);
    assertEquals("from", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(11, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.TO, t.tokenType);
    assertEquals("to", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(16, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void otherReservedWords() {
    var p = "return struct new";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.RETURN, t.tokenType);
    assertEquals("return", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRUCT, t.tokenType);
    assertEquals("struct", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(8, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.NEW, t.tokenType);
    assertEquals("new", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(15, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void basicIdentifiers() {
    var p = "x xs f0_0 foo_bar foo_bar_baz quix__";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("x", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("xs", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(3, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("f0_0", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("foo_bar", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(11, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("foo_bar_baz", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(19, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("quix__", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(31, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void tokensWithComments() {
    var p = "x < 1 # test 1\nif 3.14";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("x", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.LESS, t.tokenType);
    assertEquals("<", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(3, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("1", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.COMMENT, t.tokenType);
    assertEquals(" test 1", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(7, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.IF, t.tokenType);
    assertEquals("if", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("3.14", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(4, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void tokensWithNoSpaces() {
    var p = "for(int x)ify=4+";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.FOR, t.tokenType);
    assertEquals("for", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.LPAREN, t.tokenType);
    assertEquals("(", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(4, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_TYPE, t.tokenType);
    assertEquals("int", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("x", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(9, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.RPAREN, t.tokenType);
    assertEquals(")", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(10, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("ify", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(11, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ASSIGN, t.tokenType);
    assertEquals("=", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(14, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("4", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(15, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.PLUS, t.tokenType);
    assertEquals("+", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(16, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  @Test
  void numbersWithNoSpaces() {
    var p = "32.1.42 .0.0";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("32.1", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOT, t.tokenType);
    assertEquals(".", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("42", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOT, t.tokenType);
    assertEquals(".", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(9, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("0.0", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(10, t.column);
    assertEquals(TokenType.EOS, lexer.nextToken().tokenType);
  }

  // ----------------------------------------------------------------------
  // NEGATIVE TEST CASES

  @Test
  void nonTerminatedString() {
    var p = "\"hello \nworld\"";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,8] non-terminated string";
    assertEquals(m, e.getMessage());
  }

  @Test
  void invalidSymbolCombination() {
    var p = "!>";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,1] expecting !=";
    assertEquals(m, e.getMessage());
  }

  @Test
  void missingDoubleDigit() {
    var p = "32.a";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,4] missing digit after decimal";
    assertEquals(m, e.getMessage());
  }

  @Test
  void leadingZero() {
    var p = "02";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,1] leading zero in number";
    assertEquals(m, e.getMessage());
  }

  @Test
  void invalidSymbol() {
    var p = "?";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,1] unrecognized symbol '?'";
    assertEquals(m, e.getMessage());
  }

  @Test
  void invalidID() {
    var p = "_xs";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,1] unrecognized symbol '_'";
    assertEquals(m, e.getMessage());
  }

  // ----------------------------------------------------------------------
  // TODO: Design and implement the following unit tests and add them
  // below. Make sure your added unit tests pass.

  //
  // 1. Three new "positive" tests. Each test should involve an
  // "interesting" combination of tokens.

  @Test
  void caseSensitiveReserved() {
    var p = "Return struct new And";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("Return", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRUCT, t.tokenType);
    assertEquals("struct", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(8, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.NEW, t.tokenType);
    assertEquals("new", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(15, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("And", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(19, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.tokenType);
    assertEquals("end-of-stream", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(22, t.column);
  }

  @Test
  void moreTokensWithNoSpace() {
    var p = "7&&(}xy_0.2";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("7", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.AND, t.tokenType);
    assertEquals("&&", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(2, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.LPAREN, t.tokenType);
    assertEquals("(", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(4, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.RBRACE, t.tokenType);
    assertEquals("}", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(5, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("xy_0", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOT, t.tokenType);
    assertEquals(".", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(10, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.INT_VAL, t.tokenType);
    assertEquals("2", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(11, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.tokenType);
    assertEquals("end-of-stream", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(12, t.column);
  }

  @Test
  void noSpaceNewlineSpaceTokens() {
    var p = "var!=b_2\n\"hi\" 8.23.";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.VAR, t.tokenType);
    assertEquals("var", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.NOT_EQUAL, t.tokenType);
    assertEquals("!=", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(4, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("b_2", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.STRING_VAL, t.tokenType);
    assertEquals("hi", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(1, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOUBLE_VAL, t.tokenType);
    assertEquals("8.23", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(6, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.DOT, t.tokenType);
    assertEquals(".", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(10, t.column);
    t = lexer.nextToken();
    assertEquals(TokenType.EOS, t.tokenType);
    assertEquals("end-of-stream", t.lexeme);
    assertEquals(2, t.line);
    assertEquals(11, t.column);
  }

  //
  // 2. Two new "negative" tests. These should each be for
  // unrecognized symbols.

  @Test
  void whitespaceThenInvalidSymbol() {
    var p = " %";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,2] unrecognized symbol '%'";
    assertEquals(m, e.getMessage());
  }

  @Test
  void weirdIDThenInvalidSymbol() {
    var p = "a_0xy_ $";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.ID, t.tokenType);
    assertEquals("a_0xy_", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(1, t.column);

    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,8] unrecognized symbol '$'";
    assertEquals(m, e.getMessage());
  }
}
