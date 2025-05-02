/**
 * CPSC 326, Spring 2025
 * MyPL Lexer Implementation.
 *
 * Arjuna Herbst
 */

package cpsc326;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * The Lexer class takes an input stream containing mypl source code
 * and transforms (tokenizes) it into a stream of tokens.
 */
public class Lexer {

  private BufferedReader buffer; // handle to the input stream
  private int line = 1; // current line number
  private int column = 0; // current column number
  private boolean empty_input = false;

  /**
   * Creates a new Lexer object out of an input stream.
   */
  public Lexer(InputStream input) {
    buffer = new BufferedReader(new InputStreamReader(input));
    // Check if the input is empty when Lexer object is created
    try {
      buffer.mark(1);
      if (buffer.read() == -1) {
        empty_input = true; // set flag
      }
      buffer.reset(); // reset to the marked position
    } catch (IOException e) {
      throw new RuntimeException("Error reading input stream", e);
    }
  }

  /**
   * Helper function to read a single character from the input stream.
   * 
   * @return A single character
   */
  private char read() {
    try {
      // I found it easier to track the column manually when calling read
      return (char) buffer.read();
    } catch (IOException e) {
      error("read error", line, column + 1);
    }
    return (char) -1;
  }

  /**
   * Helper function to look ahead one character in the input stream.
   * 
   * @return A single character
   */
  private char peek() {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = (char) buffer.read();
      buffer.reset();
      return (char) ch;
    } catch (IOException e) {
      error("read error", line, column + 1);
    }
    return (char) -1;
  }

  /**
   * Helper function to check if the given character is an end of line
   * symbol.
   * 
   * @return True if the character is an end of line character and
   *         false otherwise.
   */
  // private boolean isEOL(char ch) {
  //   if (ch == '\n')
  //     return true;
  //   if (ch == '\r' && peek() == '\n') {
  //     read();
  //     return true;
  //   } else if (ch == '\r')
  //     return true;
  //   return false;
  // }

  /**
   * Helper function to check if the given character is an end of file
   * symbol.
   * 
   * @return True if the character is an end of file character and
   *         false otherwise.
   */
  private boolean isEOF(char ch) {
    return ch == (char) -1;
  }

  /**
   * Print an error message and exit the program.
   */
  private void error(String msg, int line, int column) {
    String s = "[%d,%d] %s";
    MyPLException.lexerError(String.format(s, line, column, msg));
  }

  // here we will define all the helper functions we need for our Lexer
  // The point of this is so that we can use Character ops on peek()

  // check for whitespace char
  public static boolean isWhitespace(int ch) {
    return Character.isWhitespace((char) ch);
  }

  // check for digit
  private static boolean isDigit(int ch) {
    return Character.isDigit((char) ch);
  }

  // check for letter
  private static boolean isLetter(int ch) {
    return Character.isLetter((char) ch);
  }

  // checks for symbol
  private static boolean isSymbol(int ch, char symbol) {
    return (char) ch == symbol;
  }

  // check for leading 0's
  protected static boolean hasLeadingZeroes(String value) {
    if (value.length() < 2) {
      return false;
    }

    if (isSymbol(value.charAt(0), '0')) {
      char before = value.charAt(0);
      for (int i = 1; i < value.length(); i++) {
        char curr = value.charAt(i);
        if (isSymbol(curr, '.')) {
          return false;
        } else if (isDigit(curr) && before == '0') {
          return true;
        }
        before = curr;
      }
      return false;
    }
    return false;
  }

  /**
   * Obtains and returns the next token in the stream.
   * 
   * @return The next token in the stream.
   */
  public Token nextToken() { 
    
    // check Lexer's empty input flag
    if (empty_input) {
      return new Token(TokenType.EOS, "end-of-stream", line, column);
    }

    // read whitespace until next token
    while (isWhitespace(peek())) {
      char ch = read();

      if (ch == '\n') {
        line += 1;
        column = 0;
      } else {
        column += 1;
      }
    }

    // Check for comments
    if (isSymbol(peek(), '#')) {
      int startLine = line;
      int startColumn = column + 1;

      read();
      column++;

      StringBuilder commentContent = new StringBuilder();

      // Read the entire line as a comment until a newline or EOF is encountered
      while (!isSymbol(peek(), '\n') && !isEOF(peek())) {
        commentContent.append(read());
        column++;
      }

      // Move to the next line if newline is encountered
      if (isSymbol(peek(), '\n')) {
        read();
        line++;
        column = 0;
      }
      

      String lexeme = " " + commentContent.toString().trim();

      // Return a COMMENT token with the trimmed content
      return new Token(TokenType.COMMENT, lexeme, startLine, startColumn);
    }

    // clear once more for whitespace
    while (isWhitespace(peek())) {
      char ch = read();

      if (ch == '\n') {
        line += 1;
        column = 1;
      } else {
        column += 1;
      }
    }

    // check for end of file
    if (isEOF(peek())) {
      read();
      return new Token(TokenType.EOS, "end-of-stream", line, column + 1);
    } else {
      char curr = read();
      column += 1;

      // check for single char symbols
      if (isSymbol(curr, ',')) {
        return new Token(TokenType.COMMA, "" + curr, line, column);
      } else if (isSymbol(curr, '.')) {
        return new Token(TokenType.DOT, "" + curr, line, column);
      } else if (isSymbol(curr, '+')) {
        return new Token(TokenType.PLUS, "" + curr, line, column);
      } else if (isSymbol(curr, '-')) {
        return new Token(TokenType.MINUS, "" + curr, line, column);
      } else if (isSymbol(curr, ':')) {
        return new Token(TokenType.COLON, "" + curr, line, column);
      } else if (isSymbol(curr, '*')) {
        return new Token(TokenType.TIMES, "" + curr, line, column);
      } else if (isSymbol(curr, '/')) {
        return new Token(TokenType.DIVIDE, "" + curr, line, column);
      } else if (isSymbol(curr, '{')) {
        return new Token(TokenType.LBRACE, "" + curr, line, column);
      } else if (isSymbol(curr, '}')) {
        return new Token(TokenType.RBRACE, "" + curr, line, column);
      } else if (isSymbol(curr, '(')) {
        return new Token(TokenType.LPAREN, "" + curr, line, column);
      } else if (isSymbol(curr, ')')) {
        return new Token(TokenType.RPAREN, "" + curr, line, column);
      } else if (isSymbol(curr, '[')) {
        return new Token(TokenType.LBRACKET, "" + curr, line, column);
      } else if (isSymbol(curr, ']')) {
        return new Token(TokenType.RBRACKET, "" + curr, line, column);
      }

      // check for comparator symbols
      else if (isSymbol(curr, '!')) {
        int start_line = line;
        int start_column = column;

        // if symbol after ! is =, return NOT_EQUAL
        if (isSymbol(peek(), '=')) {
          char next = read();
          column += 1;
          return new Token(TokenType.NOT_EQUAL, "" + curr + next, start_line, start_column);

        } else {
          error("expecting !=", start_line, start_column);
        }
      } else if (isSymbol(curr, '=')) {
        int start_line = line;
        int start_column = column - 1;

        if (isSymbol(peek(), '=')) {
          char next = read();
          column += 1;
          return new Token(TokenType.EQUAL, "" + curr + next, start_line, start_column + 1);

        } else {
          return new Token(TokenType.ASSIGN, "" + curr, start_line, start_column + 1);
        }
      } else if (isSymbol(curr, '>')) {
        int start_line = line;
        int start_column = column - 1;

        if (isSymbol(peek(), '=')) {
          char next = read();
          column += 1;

          return new Token(TokenType.GREATER_EQ, "" + curr + next, start_line, start_column + 1);
        } else {
          return new Token(TokenType.GREATER, "" + curr, start_line, start_column + 1);
        }
      } else if (isSymbol(curr, '<')) {
        int start_line = line;
        int start_column = column - 1;

        if (isSymbol(peek(), '=')) {
          char next = read();
          column += 1;
          return new Token(TokenType.LESS_EQ, "" + curr + next, start_line, start_column + 1);
        } else {
          return new Token(TokenType.LESS, "" + curr, start_line, start_column + 1);
        }
      } else if (isSymbol(curr, '&')) {
        int start_line = line;
        int start_column = column - 1;

        if (isSymbol(peek(), '&')) {
          char next = read();
          column += 1;

          return new Token(TokenType.AND, "" + curr + next, start_line, start_column + 1);
        } else {
          error("expected &", start_line, start_column + 1);
        }
      }
      // check for primative types
      else if (isSymbol(curr, '\"')) {
        int start_line = line;
        int start_column = column - 1;

        // create empty string to read into
        String result = "";
        while (!isSymbol(peek(), '"')) {
          if (isSymbol(peek(), '\n')) {
            error("non-terminated string", line, column + 1);
          } else if (isEOF(peek())) {
            error("non-terminated string", line, column + 1);
          }
          result += (char) read();
          column += 1;
        }
        char close = (char) read();
        column += 1;

        if (isSymbol(close, '"')) {
          return new Token(TokenType.STRING_VAL, result.toString(), start_line, start_column + 1);
        }
      }

      // check for double/int vals
      else if (isDigit(curr)) {
        int start_line = line;
        int start_column = column;
        String result = "" + curr;
        boolean isDoubleVal = false;

        while (!isWhitespace(peek()) && !isEOF(peek())) {
          if(isSymbol(peek(), '.')){
            // if there is already a double, break and return below
            if(isDoubleVal) {
              break;
            } else {
              // else set the double val flag to true
              isDoubleVal = true;
            }
          } else if (!isDigit(peek())) {
            // if the next character is not a digit, break and return below
            break;
          }

          // read next char, increment column
          char next =  read();
          result += next;
          column += 1;
        }

        if (hasLeadingZeroes(result)) {
          error("leading zero in number", start_line, start_column);
        }

        if (isDoubleVal) {
          // first get the index of the decimal point
          int decimal_index = result.indexOf('.');
          // check if there is at least one digit after, throw error if there isn't
          if (decimal_index == result.length() - 1){
            error("missing digit after decimal", start_line, column + 1);
          }
          
          return new Token(TokenType.DOUBLE_VAL, result, start_line, start_column);
        } else {
          return new Token(TokenType.INT_VAL, result, start_line, start_column);
        }
      }

      // check for ids/reserved words
      else if (isLetter(curr)) {
        int start_line = line;
        int start_column = column;
        String result = "" + curr;

        while (!isWhitespace(peek()) && !isEOF(peek())) {
          if (isLetter(peek()) || isSymbol(peek(), '_')  || isDigit(peek())) {
            result += read();
            column += 1;
          } else {
            break;
          }
        }
        // switch statement to return keyword/ids
        switch(result) {
          case "and": 
            return new Token(TokenType.AND, result, start_line, start_column);
          case "or":
            return new Token(TokenType.OR, result, start_line, start_column);
          case "not":
            return new Token(TokenType.NOT, result, start_line, start_column);
          case "true":
            return new Token(TokenType.BOOL_VAL, result, start_line, start_column);
          case "false":
            return new Token(TokenType.BOOL_VAL, result, start_line, start_column);
          case "int":
            return new Token(TokenType.INT_TYPE, result, start_line, start_column);
          case "double":
            return new Token(TokenType.DOUBLE_TYPE, result, start_line, start_column);
          case "char":
            return new Token(TokenType.CHAR_TYPE, result, start_line, start_column);
          case "string":
            return new Token(TokenType.STRING_TYPE, result, start_line, start_column);
          case "bool":
            return new Token(TokenType.BOOL_TYPE, result, start_line, start_column);
          case "void":
            return new Token(TokenType.VOID_TYPE, result, start_line, start_column);
          case "var":
            return new Token(TokenType.VAR, result, start_line, start_column);
          case "while":
            return new Token(TokenType.WHILE, result, start_line, start_column);
          case "for":
            return new Token(TokenType.FOR, result, start_line, start_column);
          case "to":
            return new Token(TokenType.TO, result, start_line, start_column);
          case "from":
            return new Token(TokenType.FROM, result, start_line, start_column);
          case "if":
            return new Token(TokenType.IF, result, start_line, start_column);
          case "else":
            return new Token(TokenType.ELSE, result, start_line, start_column);
          case "new":
            return new Token(TokenType.NEW, result, start_line, start_column);
          case "return":
            return new Token(TokenType.RETURN, result, start_line, start_column);
          case "struct":
            return new Token(TokenType.STRUCT, result, start_line, start_column);
          case "null":
            return new Token(TokenType.NULL_VAL, result, start_line, start_column);
          default:
            return new Token(TokenType.ID, result, start_line, start_column);
        }
      }
      else {
        error("unrecognized symbol '" + curr + "'", line, column);
      }

    }

  return new Token(TokenType.EOS,"end-of-stream", line, column + 1);
}

}
