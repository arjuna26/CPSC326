/**
 * CPSC 326, Spring 2025
 * Basic token tests.
 */ 

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


class TokenTests {

  @Test
  void basicTokenCreation() {
    Token t = new Token(TokenType.INT_TYPE, "int", 0, 0);
    assertEquals(TokenType.INT_TYPE, t.tokenType);
    assertEquals("int", t.lexeme);
    assertEquals(0, t.line);
    assertEquals(0, t.column);
    assertEquals("INT_TYPE \"int\" line 0 column 0", t.toString());
  }

  @Test
  void correctLineColumnTokenCreation() {
    Token t = new Token(TokenType.COMMA, ",", 10, 20);
    assertEquals(TokenType.COMMA, t.tokenType);
    assertEquals(",", t.lexeme);
    assertEquals(10, t.line);
    assertEquals(20, t.column);
    assertEquals("COMMA \",\" line 10 column 20", t.toString());
  }

}
