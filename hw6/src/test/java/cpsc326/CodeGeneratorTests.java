/**
 * CPSC 326, Spring 2025
 * Unit tests for he Code Generator.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;


class CodeGeneratorTests {

  /** For dealing with program output **/
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
  
  /**
   * Helper to generate a VM to run
   */
  VM build(String program) {
    Lexer lexer = new Lexer(istream(program));
    ASTParser parser = new ASTParser(lexer);
    Program p = parser.parse();
    p.accept(new SemanticChecker());
    VM vm = new VM();
    p.accept(new CodeGenerator(vm));
    return vm;
  }
  
  //----------------------------------------------------------------------
  // Getting Started
  
  @Test
  void emptyProgram() {
    String p = "void main() {}";
    build(p).run();
    assertEquals("", output.toString());    
  }

  @Test
  void simplePrint() {
    String p = """
      void main() {print("blue")}
    """;
    build(p).run();
    assertEquals("blue", output.toString());    
  }

  //----------------------------------------------------------------------
  // BASIC VARIABLES AND ASSIGNMENT

  @Test
  void testSimpleVarDecls() {
    String p = """
      void main() { 
        var x1: int = 3
        var x2: double = 2.7
        var x3: bool = true
        var x4: string = "abc"
        print(x1)
        print(x2) 
        print(x3) 
        print(x4)
      }
    """;
    build(p).run();
    assertEquals("32.7trueabc", output.toString());
  }

  @Test
  void testSimpleVarDeclNoExpr() {
    String p = """
      void main() { 
      var x: int  
      print(x)
    } 
    """;
    build(p).run();
    assertEquals("null", output.toString());
  }
  
  @Test
  void testSimpleVarAssignments() {
    String p = """
      void main() { 
        var x = 3 
        print(x) 
        x = 4 
        print(x)
      } 
    """;
    build(p).run();
    assertEquals("34", output.toString());
  }

  //----------------------------------------------------------------------
  // ARITHMETIC EXPRESSIONS

  @Test
  void testSimpleAdd() {
    String p = """
      void main() { 
        var x = 4 + 5
        var y = 3.25 + 4.5
        var z = "ab" + "cd"
        print(x)
        print(" ")
        print(y)
        print(" ")
        print(z)
      } 
    """;
    build(p).run();
    assertEquals("9 7.75 abcd", output.toString());
  }
  
  @Test
  void testSimpleSub() {
    String p = """
      void main() { 
        var x = 6 - 5
        var y = 4.5 - 3.25
        print(x)
        print(" ")
        print(y)
      } 
    """;
    build(p).run();
    assertEquals("1 1.25", output.toString());
  }

  @Test
  void testSimpleMult() {
    String p = """
      void main() { 
        var x = 4 * 3
        var y = 4.5 * 3.25
        print(x)
        print(" ")
        print(y)
      } 
    """;
    build(p).run();
    assertEquals("12 14.625", output.toString());
  }
  
  @Test
  void testSimpleDiv() {
    String p = """
      void main() { 
        var x = 9 / 2
        var y = 4.5 / 1.25
        print(x)
        print(" ")
        print(y)
      } 
    """;
    build(p).run();
    assertEquals("4 3.6", output.toString());
  }

  @Test
  void testLongerArithmeticExpr() {
    String p = """
      void main() { 
        var x = 3 + (6 - 5) + (5 * 2) + (2 / 2)
        print(x)
      } 
    """;
    build(p).run();
    assertEquals("15", output.toString());
  }

  //----------------------------------------------------------------------
  // BOOLEAN EXPRESSIONS

  @Test
  void testSimpleAnd() {
    String p = """
      void main() {
        var x1: bool = true and true
        var x2: bool = true and false
        var x3: bool = false and true
        var x4: bool = false and false
        print(x1)
        print(" ")
        print(x2)
        print(" ")
        print(x3)
        print(" ")
        print(x4)
      }
    """;
    build(p).run();
    assertEquals("true false false false", output.toString());
  }

  @Test
  void testSimpleOr() {
    String p = """
      void main() {
        var x1: bool = true or true
        var x2: bool = true or false
        var x3: bool = false or true
        var x4: bool = false or false
        print(x1)
        print(" ")
        print(x2)
        print(" ")
        print(x3)
        print(" ")
        print(x4)
      }
    """;
    build(p).run();
    assertEquals("true true true false", output.toString());
  }

  @Test
  void testSimpleNot() {
    String p = """
      void main() {
        var x1: bool = not true
        var x2: bool = not false
        print(x1)
        print(" ")
        print(x2)
      }
    """;
    build(p).run();
    assertEquals("false true", output.toString());
  }

  @Test
  void testMoreInvolvedLogicalExpression() {
    String p = """
      void main() {
        var x: bool = true or (true and false) or (false or (true and true))
        var y: bool = not ((not false) and (false or (true or false)) and true)
        print(x)
        print(" ")
        print(y)
      }
    """;
    build(p).run();
    assertEquals("true false", output.toString());
  }

  //----------------------------------------------------------------------
  // COMPARISON OPERATORS

  @Test
  void testTrueNumericalComparisons() {
    String p = """
      void main() {
        var x1: bool = 3 < 4
        var x2: bool = 3 <= 4
        var x3: bool = 3 <= 3
        var x4: bool = 4 > 3
        var x5: bool = 4 >= 3
        var x6: bool = 3 >= 3
        var x7: bool = 3 == 3
        var x8: bool = 3 != 4
        print(x1 and x2 and x3 and x4 and x5 and x6 and x7 and x8)
        var y1: bool = 3.25 < 4.5
        var y2: bool = 3.25 <= 4.5
        var y3: bool = 3.25 <= 3.25
        var y4: bool = 4.5 > 3.25
        var y5: bool = 4.5 >= 3.25
        var y6: bool = 3.25 >= 3.25
        var y7: bool = 3.25 == 3.25
        var y8: bool = 3.25 != 4.5
        print(y1 and y2 and y3 and y4 and y5 and y6 and y7 and y8)
      }
    """;
    build(p).run();
    assertEquals("truetrue", output.toString());
  }

  @Test
  void testFalseNumericalComparisons() {
    String p = """
      void main() {
        var x1: bool = 4 < 3
        var x2: bool = 4 <= 3
        var x3: bool = 3 > 4
        var x4: bool = 3 >= 4
        var x5: bool = 3 == 4
        var x6: bool = 3 != 3
        print(x1 or x2 or x3 or x4 or x5 or x6)
        var y1: bool = 4.5 < 3.25
        var y2: bool = 4.5 <= 3.25
        var y3: bool = 3.25 > 4.5
        var y4: bool = 3.25 >= 4.5
        var y5: bool = 3.25 == 4.5
        var y6: bool = 3.25 != 3.25
        print(y1 or y2 or y3 or y4 or y5 or y6)
      }
    """;
    build(p).run();
    assertEquals("falsefalse", output.toString());
  }

  @Test
  void testTrueAlphabeticComparisons() {
    String p = """
      void main() {
        var x1: bool = "a" < "b"
        var x2: bool = "a" <= "b"
        var x3: bool = "a" <= "a"
        var x4: bool = "b" > "a"
        var x5: bool = "b" >= "a"
        var x6: bool = "a" >= "a"
        var x7: bool = "a" == "a"
        var x8: bool = "a" != "b"
        print(x1 and x2 and x3 and x4 and x5 and x6 and x7 and x8)
        var y1: bool = "aa" < "ab"
        var y2: bool = "aa" <= "ab"
        var y3: bool = "aa" <= "aa"
        var y4: bool = "ab" > "aa"
        var y5: bool = "ab" >= "aa"
        var y6: bool = "aa" >= "aa"
        var y7: bool = "aa" == "aa"
        var y8: bool = "aa" != "ab"
        print(y1 and y2 and y3 and y4 and y5 and y6 and y7 and y8)
      }
    """;
    build(p).run();
    assertEquals("truetrue", output.toString());
  }

  @Test
  void testFalseAlphabeticComparisons() {
    String p = """
      void main() {
        var x1: bool = "b" < "a"
        var x2: bool = "b" <= "a"
        var x3: bool = "a" > "b"
        var x4: bool = "a" >= "b"
        var x5: bool = "a" == "b"
        var x6: bool = "a" != "a"
        print(x1 or x2 or x3 or x4 or x5 or x6)
        var y1: bool = "ab" < "aa"
        var y2: bool = "ab" <= "aa"
        var y3: bool = "aa" > "ab"
        var y4: bool = "aa" >= "ab"
        var y5: bool = "aa" == "ab"
        var y6: bool = "aa" != "aa"
        print(y1 or y2 or y3 or y4 or y5 or y6)
      }
    """;
    build(p).run();
    assertEquals("falsefalse", output.toString());
  }

  @Test
  void testNullComparisons() {
    String p = """
      void main() {
        var a: int = 3
        var b: double = 2.75
        var c: string = "abc"
        var d: bool = false
        print(null != null)
        print((a == null) or (b == null) or (c == null) or (d == null))
        print((null == a) or (null == b) or (null == c) or (null == d))
        print(" ")
        print(null == null)
        print((a != null) and (b != null) and (c != null) and (d != null))
        print((null != a) and (null != b) and (null != c) and (null != d))
      }
    """;
    build(p).run();
    assertEquals("falsefalsefalse truetruetrue", output.toString());
  }

  //----------------------------------------------------------------------
  // WHILE LOOPS

  @Test
  void basicWhile() {
    String p = """
      void main() {
        var i: int = 0
        while i < 5 {
          i = i + 1
        }
        print(i)
      }
    """;
    build(p).run();
    assertEquals("5", output.toString());
  }

  @Test
  void moreInvolvedWhile() {
    String p = """
      void main() {
        var i: int = 0
        while i < 7 {
          var j: int = i * 2
          print(j)
          print(" ")
          i = i + 1
        }
        print(i)
      }
    """;
    build(p).run();
    assertEquals("0 2 4 6 8 10 12 7", output.toString());
  }

  @Test
  void nestedWhile() {
    String p = """
      void main() {
        var i: int = 0
        while i < 5 {
          print(i)
          print(" ")
          var j: int = 0
          while j < i {
            print(j)
            print(" ")
            j = j + 1
          }
          i = i + 1
        }
      }
    """;
    build(p).run();
    assertEquals("0 1 0 2 0 1 3 0 1 2 4 0 1 2 3 ", output.toString());
  }

  //----------------------------------------------------------------------
  // FOR LOOPS

  @Test
  void basicFor() {
    String p = """
      void main() {
        for i from 0 to 4 {
          print(i)
          print(" ")
        }
      }
    """;
    build(p).run();
    // Output: 0 1 2 3 4 
    assertEquals("0 1 2 3 4 ", output.toString());
  }

  @Test
  void nestedFor() {
    String p = """
      void main() {
        var x: int = 0
        for i from 1 to 5 {
          for j from 1 to 4 {
            x = x + (i * j)
          }
        }
        print(x)
      }
    """;
    build(p).run();
    assertEquals("150", output.toString());
  }

  @Test
  void forOuterNonBadShadowing() {
    String p = """
      void main() {
        var i: int = 32
        for i from 0 to 4 {
          print(i)
          print(" ")
        }
        print(i)
      }
    """;
    build(p).run();
    assertEquals("0 1 2 3 4 32", output.toString());
  }

  //----------------------------------------------------------------------
  // IF STATEMENTS

  @Test
  void justAnIf() {
    String p = """
      void main() {
        print("-")
        if true {
          print(1)
        }
        print("-")
      }
    """;
    build(p).run();
    assertEquals("-1-", output.toString());
  }

  @Test
  void consecutiveIfs() {
    String p = """
      void main() {
        print("-")
        if 3 < 4 {
          print(1)
        }
        if true {
          print(2)
        }
        if 3 > 4 {
          print(3)
        }
        print("-")
      }
    """;
    build(p).run();
    assertEquals("-12-", output.toString());
  }

  @Test
  void simpleElseIfs() {
    String p = """
      void main() {
        print("-")
        if 3 < 4 {
          print(1)
        }
        else if 4 > 3 {
          print(2)
        }
        else {
          print(3)
        }
        if 4 < 3 {
          print(1)
        }
        else if 3 < 4 {
          print(2)
        }
        else {
          print(3)
        }
        if 4 < 3 {
          print(1)
        }
        else if 3 != 3 {
          print(2)
        }
        else {
          print(3)
        }
        print("-")
      }
    """;
    build(p).run();
    assertEquals("-123-", output.toString());
  }

  @Test
  void manyElseIfs() {
    String p = """
      void main() {
        print("-")
        if false {
          print(1)
        }
        else if false {
          print(2)
        }
        else if true {
          print(3)
        }
        else if true {
          print(4)
        }
        else {
          print(5)
        }
        print("-")
      }
    """;
    build(p).run();
    assertEquals("-3-", output.toString());
  }

  //----------------------------------------------------------------------
  // FUNCTION CALLS

  @Test
  void noArgCall() {
    String p = """
      void f() {
      }
      void main() {
        print(f())
      }
    """;
    build(p).run();
    assertEquals("null", output.toString());
  }

  @Test
  void oneArgCall() {
    String p = """
      int f(x: int) {
        return x
      }
      void main() {
        print(f(3))
        print(f(4))
      }
    """;
    build(p).run();
    assertEquals("34", output.toString());
  }

  @Test
  void twoArgCall() {
    String p = """
      int f(x: int, y: int) {
        return x * y
      }
      void main() {
        print(f(3, 2))
        print(f(5, 6))
      }
    """;
    build(p).run();
    assertEquals("630", output.toString());
  }

  @Test
  void threeArgCall() {
    String p = """
      int f(x: int, y: int, z: int) {
        return (x * y) - z
      }
      void main() {
        print(f(3, 2, 4))
        print(f(5, 6, 10))
      }
    """;
    build(p).run();
    assertEquals("220", output.toString());
  }

  @Test
  void multiLevelCall() {
    String p = """
      string f(s: string) {
        return s + "!"
      }
      string g(s1: string, s2: string) {
        return f(s1 + s2)
      }
      string h(s1: string, s2: string, s3: string) {
        return g(s1, s2) + f(s3)
      }
      void main() {
        print(h("red", "blue", "green"))
      }
    """;
    build(p).run();
    assertEquals("redblue!green!", output.toString());
  }

  @Test
  void basicRecursion() {
    String p = """
      int non_negative_sum(x: int) {
        if x <= 0 {
          return 0
        }
        return x + non_negative_sum(x - 1)
      }
      void main() {
        print(non_negative_sum(0))
        print(" ")
        print(non_negative_sum(1))
        print(" ")
        print(non_negative_sum(10))
      }
    """;
    build(p).run();
    assertEquals("0 1 55", output.toString());
  }

  @Test
  void fibRecursion() {
    String p = """
      int fib(n: int) {
        if (n < 0) {
          return null
        }
        if n == 0 {
          return 0
        }
        if n == 1 {
          return 1
        }
        return fib(n - 1) + fib(n - 2)
      }
      void main() {
        print(fib(8))
      }
    """;
    build(p).run();
    assertEquals("21", output.toString());
  }
  
  //----------------------------------------------------------------------
  // STRUCTS

  @Test
  void emptyStruct() {
    String p = """
      struct T {
      }
      void main() {
        var t: T = new T()
        print("")
      }
    """;
    build(p).run();
    assertEquals("", output.toString());
  }

  @Test
  void simpleOneFieldStruct() {
    String p = """
      struct T {
        x: int
      }
      void main() {
        var t: T = new T(3)
        print(t.x)
      }
    """;
    build(p).run();
    assertEquals("3", output.toString());
  }

  @Test
  void simpleTwoFieldStruct() {
    String p = """
      struct T {
        x: int,
        y: bool
      }
      void main() {
        var t: T = new T(3, true)
        print(t.x)
        print(" ")
        print(t.y)
      }
    """;
    build(p).run();
    assertEquals("3 true", output.toString());
  }

  @Test
  void simpleAssignField() {
    String p = """
      struct T {
        x: int,
        y: bool
      }
      void main() {
        var t: T = new T(3, true)
        t.x = t.x + 1
        t.y = not t.y
        print(t.x)
        print(" ")
        print(t.y)
      }
    """;
    build(p).run();
    assertEquals("4 false", output.toString());
  }

  @Test
  void simpleStructAssign() {
    String p = """
      struct T {
        x: int,
        y: bool
      }
      void main() {
        var t1: T = new T(3, true)
        var t2: T = t1
        var t3: T
        t3 = t2
        t1.x = t1.x + 1
        print(t1.x)
        print(" ")
        print(t1.y)
        print(" ")
        print(t2.x)
        print(" ")
        print(t2.y)
        print(" ")
        print(t3.x)
        print(" ")
        print(t3.y)
      }
    """;
    build(p).run();
    assertEquals("4 true 4 true 4 true", output.toString());
  }

  @Test
  void simpleTwoStructs() {
    String p = """
      struct T1 {
        val: int,
        t2: T2
      }
      struct T2 {
        val: int,
        t1: T1
      }
      void main() {
        var x: T1 = new T1(3, null)
        var y: T2 = new T2(4, x)
        x.t2 = y
        print(x.val)
        print(" ")
        print(x.t2.val)
        print(" ")
        print(y.val)
        print(" ")
        print(y.t1.val)
      }
    """;
    build(p).run();
    assertEquals("3 4 4 3", output.toString());
  }

  @Test
  void recursiveStruct() {
    String p = """
      struct Node {
        val: int,
        next: Node
      }
      void main() {
        var r: Node = new Node(10, null)
        r.next = new Node(20, null)
        r.next.next = new Node(30, null)
        print(r.val)
        print(" ")
        print(r.next.val)
        print(" ")
        print(r.next.next.val)
     }
    """;
    build(p).run();
    assertEquals("10 20 30", output.toString());
  }

  @Test
  void structAsFunParam() {
    String p = """
      struct Node {
        val: int, 
        next: Node
      }
      int val(n: Node) {
        return n.val
      }
      void main() {
        var r: Node = new Node(24, null)
        print(val(r))
      }
    """;
    build(p).run();
    assertEquals("24", output.toString());
  }

  //----------------------------------------------------------------------
  // ARRAYS

  @Test
  void simpleArrayCreation() {
    String p = """
      void main() {
        var xs: [int] = new int[5]
        print("")
      }
    """;
    build(p).run();
    assertEquals("", output.toString());
  }

  @Test
  void simpleArrayAccess() {
    String p = """
      void main() {
        var xs: [int] = new int[2]
        print(xs[0])
        print(" ")
        print(xs[1])
      }
    """;
    build(p).run();
    assertEquals("null null", output.toString());
  }

  @Test
  void arrayInit() {
    String p = """
      void main() {
        var xs: [bool] = new bool[3]
        xs[0] = false
        xs[1] = true
        print(xs[0])
        print(" ")
        print(xs[1])
        print(" ")
        print(xs[2])
     }
   """;
   build(p).run();
   assertEquals("false true null", output.toString());
  }

  @Test
  void arrayOfStruct() {
    String p = """
      struct T {
        x: bool,
        y: int
      }
      void main() {
        var xs: [T] = new T[3]
        xs[0] = new T(true, 24)
        xs[1] = new T(false, 48)
        print(xs[0].x)
        print(" ")
        print(xs[0].y)
        print(" ")
        print(xs[1].x)
        print(" ")
        print(xs[1].y)
      }
    """;
    build(p).run();
    assertEquals("true 24 false 48", output.toString());
  }

  @Test
  void updateArrayOfStruct() {
    String p = """
      struct T {
        x: bool,
        y: int
      }
      void main() {
        var xs: [T] = new T[2]
        xs[0] = new T(true, 24)
        xs[1] = new T(false, 48)
        xs[0].x = not xs[0].x
        xs[0].y = xs[0].y + 1
        xs[1].x = not xs[1].x
        xs[1].y = xs[1].y + 1
        print(xs[0].y)
        print(" ")
        print(xs[0].x)
        print(" ")
        print(xs[1].y)
        print(" ")
        print(xs[1].x)
      }
    """;
    build(p).run();
    assertEquals("25 false 49 true", output.toString());
  }

  @Test
  void updatePathEndingInArray() {
    String p = """
      struct Node {
        val: int,
        next: [Node]
      }
      void main() {
        var n: Node = new Node(20, new Node[2])
        n.next[0] = new Node(10, new Node[1])
        n.next[1] = new Node(30, null)
        n.next[0].next[0] = new Node(5, null)
        print(n.val)
        print(" ")
        print(n.next[0].val)
        print(" ")
        print(n.next[1].val)
        print(" ")
        print(n.next[0].next[0].val)
      }
    """;
    build(p).run();
    assertEquals("20 10 30 5", output.toString());
  }

  @Test
  void arrayAsParam() {
    String p = """
      bool val(xs: [bool], index: int) {
        return xs[index]
      }
      void main() {
        var xs: [bool] = new bool[5]
        xs[0] = true
        print(val(xs, 0))
        print(" ")
        xs[1] = false
        print(val(xs, 1))
      }
    """;
    build(p).run();
    assertEquals("true false", output.toString());
  }

  //----------------------------------------------------------------------
  // BUILT-IN FUNCTIONS

  @Test
  void simpleToStr() {
    String p = """
      void main() {
        print(str_val(24))
        print(" ")
        print(str_val(3.14))
      }
    """;
    build(p).run();
    assertEquals("24 3.14", output.toString());
  }

  @Test
  void simpleToInt() {
    String p = """
      void main() {
        print(int_val("24"))
        print(" ")
        print(int_val(3.14))
      }
    """;
    build(p).run();
    assertEquals("24 3", output.toString());
  }

  @Test
  void simpleToDouble() {
    String p = """
      void main() {
        print(dbl_val("3.14"))
        print(" ")
        print(dbl_val(3))
      }
    """;
    build(p).run();
    assertEquals("3.14 3.0", output.toString());
  }

  @Test
  void stringLength() {
    String p = """
      void main() {
        print(size(""))
        print(" ")
        print(size("abcdefg"))
      }
    """;
    build(p).run();
    assertEquals("0 7", output.toString());
  }

  @Test
  void arrayLength() {
    String p = """
      void main() {
        print(size(new int[0]))
        print(" ")
        print(size(new int[7]))
        print(" ")
        print(size(new string[1000]))
      }
    """;
    build(p).run();
    assertEquals("0 7 1000", output.toString());
  }

  @Test
  void stringGet() {
    String p = """
      void main() {
        var s: string = "bluegreen"
        for i from 0 to size(s) -1 {
          print(get(i, s))
          print(" ")
        }
      }
    """;
    build(p).run();
    assertEquals("b l u e g r e e n ", output.toString());
  }

  //----------------------------------------------------------------------
  // RUN-TIME ERRORS

  @Test
  void intDivideByZero() {
    String p = """
      void main() {
        var x = 1 / 0
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }
  
  @Test
  void doubleDivideByZero() {
    String p = """
      void main() {
        var x = 1.23 / 0.0
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }
  
  @Test
  void rvalueNullArrayAccess() {
    String p = """
      void main() {
        var xs: [int] = null
        var x = xs[0]
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void lvalueNullArrayAccess() {
    String p = """
      void main() {
        var xs: [int] = null
        xs[1] = 5
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }
  
  @Test
  void rvalueSimpleNullStructDereference() {
    String p = """
      struct T {x: int, y: bool}
      void main() {
        var t: T = null
        var v = t.x
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void rvalueMoreInvoledNullStructDereference() {
    String p = """
      struct Node {val: int, next: Node}
      void main() {
        var n: Node = new Node(3, null)
        var x = n.next.val
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }
  
  @Test
  void lvalueNullStructDereference() {
    String p = """
      struct Node {val: int, next: Node}
      void main() {
        var n: Node = new Node(3, null)
        n.next.val = 4
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void indexTooBig() {
    String p = """
      void main() {
        var xs = new int[5]
        var x = xs[5]
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void indexTooSmall() {
    String p = """
      void main() {
        var xs = new int[5]
        var x = xs[0-1]
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void badIntVal() {
    String p = """
      void main() {
        var x = int_val("123a")
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }
  
  @Test
  void badDoubleVal() {
    String p = """
      void main() {
        var x = dbl_val("1.23a")
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void stringGetIndexTooBig() {
    String p = """
      void main() {
        var c = get(3, "abc")
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

  @Test
  void stringGetIndexTooSmall() {
    String p = """
      void main() {
        var c = get(0-1, "abc")
      }
    """;
    VM vm = build(p);
    Exception e = assertThrows(MyPLException.class, () -> vm.run());
    assertTrue(e.getMessage().startsWith("VM_ERROR: "));
  }

}

