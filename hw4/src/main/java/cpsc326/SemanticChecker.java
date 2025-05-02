/**
 * CPSC 326, Spring 2025
 * The Semantic Checker implementation.
 * 
 * Arjuna Herbst
 */

package cpsc326;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class SemanticChecker implements Visitor {

  // for tracking function and struct definitions:
  private Map<String, FunDef> functions = new HashMap<>();
  private Map<String, StructDef> structs = new HashMap<>();
  // for tracking variable types:
  private SymbolTable symbolTable = new SymbolTable();
  // for holding the last inferred type:
  private DataType currType;

  // ----------------------------------------------------------------------
  // Helper functions
  // ----------------------------------------------------------------------

  /**
   */
  private boolean isBaseType(String type) {
    return List.of("int", "double", "bool", "string", "void").contains(type);
  }

  /**
   */
  private boolean isBuiltInFunction(String name) {
    return List.of("print", "println", "readln", "size", "get", "int_val",
        "dbl_val", "str_val").contains(name);
  }

  /**
   * Create an error message
   */
  private void error(String msg) {
    MyPLException.staticError(msg);
  }

  /**
   * Creates an error
   */
  private void error(String msg, Token token) {
    String s = "[%d,%d] %s";
    MyPLException.staticError(String.format(s, token.line, token.column, msg));
  }

  /**
   * Checks if the field name is a field in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   * 
   * @param fieldName the field name to check for
   * @param structDef the struct definition to check
   * @returns true if a match and false otherwise
   */
  private boolean isStructField(String fieldName, StructDef structDef) {
    for (var field : structDef.fields)
      if (field.varName.lexeme.equals(fieldName))
        return true;
    return false;
  }

  /**
   * Obtains the data type for the field name in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   * 
   * @param fieldName the field name
   * @param structDef the struct definition
   * @returns the corresponding data type or null if no such field exists
   */
  private DataType getStructFieldType(String fieldName, StructDef structDef) {
    for (var field : structDef.fields)
      if (field.varName.lexeme.equals(fieldName))
        return field.dataType;
    return null;
  }

  // ----------------------------------------------------------------------
  // Visit Functions
  // ----------------------------------------------------------------------

  /**
   * Checks the program
   */
  public void visit(Program node) {
    // collect all struct definitions first
    for (StructDef s : node.structs) {
      if (structs.containsKey(s.structName.lexeme)) {
        error("Duplicate struct name: " + s.structName.lexeme, s.structName);
      } else {
        structs.put(s.structName.lexeme, s);
      }
    }

    // now check each struct
    for (StructDef s : node.structs) {
      s.accept(this);
    }

    // check each function
    for (FunDef f : node.functions) {
      if (functions.containsKey(f.funName.lexeme)) {
        error("Duplicate function name: " + f.funName.lexeme, f.funName);
      } else {
        functions.put(f.funName.lexeme, f);
      }

      // Check if this is the main function
      if (f.funName.lexeme.equals("main")) {
        // Check if main has void return type
        if (!f.returnType.type.lexeme.equals("void")) {
          error("Main function must have void return type", f.funName);
        }

        // Check if main has no parameters
        if (!f.params.isEmpty()) {
          error("Main function cannot have parameters", f.funName);
        }
      }
      f.accept(this);
    }

    // check for main
    if (!functions.containsKey("main")) {
      error("No main function found");
    }
  }

  /**
   * Checks a function definition signature and body
   */
  public void visit(FunDef node) {
    String funName = node.funName.lexeme; // set current function name
    if (isBuiltInFunction(funName)) { // check if built-in function
      error(funName + " is a built-in function and cannot be redefined", node.funName);
    }
    // check if function already defined
    if (symbolTable.exists(funName)) {
      error(funName + " has already been defined and cannot be redefined", node.funName);
    }

    // Enter a new scope for this function
    symbolTable.pushEnvironment();

    // check return type
    node.returnType.accept(this);

    // Store declared return type
    DataType declaredReturnType = new DataType();
    declaredReturnType.isArray = node.returnType.isArray;
    declaredReturnType.type = node.returnType.type;

    // Initialize currType to the function's return type
    // This allows functions without return statements to pass type checking
    currType = declaredReturnType;

    // check parameters
    for (VarDef p : node.params) {
      if (symbolTable.existsInCurrEnv(p.varName.lexeme)) {
        error("Duplicate parameter name: " + p.varName.lexeme, p.varName);
      }
      p.accept(this);
      symbolTable.add(p.varName.lexeme, p.dataType);
    }

    // check body
    for (Stmt s : node.stmts) {
      s.accept(this);
    }

    // Exit the function scope
    symbolTable.popEnvironment();
  }

  /**
   * Checks structs for duplicate fields and valid data types
   */
  public void visit(StructDef node) {
    Set<String> fieldNames = new HashSet<>();
    for (VarDef field : node.fields) {
      // Check for duplicate field names
      if (fieldNames.contains(field.varName.lexeme)) {
        error("Duplicate field name: " + field.varName.lexeme, field.varName);
      }
      fieldNames.add(field.varName.lexeme);

      // Check field type
      field.accept(this);
    }
  }

  /**
   * Checks variable declarations
   */
  public void visit(VarDef node) {
    node.dataType.accept(this);
  }

  /**
   * Checks data types
   */
  public void visit(DataType node) {
    String typeName = node.type.lexeme;

    // Check if it's a base type or a defined struct
    if (!isBaseType(typeName) && !structs.containsKey(typeName)) {
      error("Undefined type: " + typeName, node.type);
    }

    // Store current type for checking later
    currType = node;
  }

  /**
   * Helper method to check if types are compatible
   */
  private boolean typesCompatible(DataType t1, DataType t2) {
    if (t1 == null || t2 == null)
      return true; // null can be assigned to any type

    if (t1.isArray != t2.isArray)
      return false;

    return t1.type.lexeme.equals(t2.type.lexeme);
  }

  /**
   * Checks assignment statements
   */
  public void visit(AssignStmt node) {
    // First check the expression on the right side
    node.expr.accept(this);
    DataType exprType = currType;

    // Check the lvalue path
    if (node.lvalue.isEmpty()) {
      error("Empty lvalue in assignment");
      return;
    }

    VarRef firstVar = node.lvalue.get(0);
    String varName = firstVar.varName.lexeme;

    // Check if variable exists
    if (!symbolTable.exists(varName)) {
      error("Undefined variable: " + varName, firstVar.varName);
      return;
    }

    DataType currentType = symbolTable.get(varName);

    // Check array access if present for first var
    if (firstVar.arrayExpr.isPresent()) {
      if (!currentType.isArray) {
        error("Cannot index non-array variable", firstVar.varName);
        return;
      }

      Expr arrayIndex = firstVar.arrayExpr.get();
      arrayIndex.accept(this);

      // Array index must be an integer
      if (currType == null || !currType.type.lexeme.equals("int")) {
        error("Array index must be an integer", firstVar.varName);
        return;
      }

      // Adjust the type to the element type
      DataType elementType = new DataType();
      elementType.isArray = false;
      elementType.type = currentType.type;
      currentType = elementType;
    }

    // Process the rest of the path (if any) starting at index 1
    for (int i = 1; i < node.lvalue.size(); i++) {
      VarRef ref = node.lvalue.get(i);
      String fieldName = ref.varName.lexeme;

      // Current type must be a struct
      if (!structs.containsKey(currentType.type.lexeme)) {
        error("Cannot access field of non-struct type", ref.varName);
        return;
      }

      StructDef structDef = structs.get(currentType.type.lexeme);

      // Check if field exists
      if (!isStructField(fieldName, structDef)) {
        error("Undefined field: " + fieldName, ref.varName);
        return;
      }

      // Update current type to field type
      currentType = getStructFieldType(fieldName, structDef);

      // Check if we're trying to access a field without array indexing, but the field
      // is an array
      if (i < node.lvalue.size() - 1 && currentType.isArray && !ref.arrayExpr.isPresent()) {
        error("Cannot access field of array without indexing", node.lvalue.get(i + 1).varName);
        return;
      }

      // AFTER getting the field type, check array access if present
      if (ref.arrayExpr.isPresent()) {
        if (!currentType.isArray) {
          error("Cannot index non-array field", ref.varName);
          return;
        }

        Expr arrayIndex = ref.arrayExpr.get();
        arrayIndex.accept(this);

        // When validating array indices, combine these checks
        boolean validIndex = currType != null && currType.type.lexeme.equals("int");
        if (!validIndex) {
          error("Array index must be an integer", ref.varName);
          return;
        }

        // Adjust the type to the element type
        DataType elementType = new DataType();
        elementType.isArray = false;
        elementType.type = currentType.type;
        currentType = elementType;
      }
    }

    // Check type compatibility
    if (!typesCompatible(currentType, exprType)) {
      error("Type mismatch in assignment", node.lvalue.get(0).varName);
    }
  }

  /**
   * Checks return statements
   */
  public void visit(ReturnStmt node) {
    FunDef currentFunction = null;
    // Find the current function
    for (Map.Entry<String, FunDef> entry : functions.entrySet()) {
      if (entry.getValue().stmts.contains(node)) {
        currentFunction = entry.getValue();
        break;
      }
    }

    if (currentFunction == null) {
      // This should not happen, but handle it just in case
      error("Return statement not inside a function");
      return;
    }

    DataType expectedReturnType = currentFunction.returnType;

    if (expectedReturnType.type.lexeme.equals("void")) {
      // If the function is void, the return expression should be null
      if (node.expr != null) {
        node.expr.accept(this);
        if (currType != null) {
          error("Cannot return a value from a void function");
          return;
        }
      }
    } else {
      // If the function is not void, the return expression should not be null
      if (node.expr == null) {
        error("Must return a value from a non-void function");
        return;
      }

      // Process the return expression to set currType
      node.expr.accept(this);

      // Check type compatibility
      if (!typesCompatible(expectedReturnType, currType)) {
        error("Type mismatch in return statement");
        return;
      }
    }
  }

  /**
   * Checks var statements
   */
  public void visit(VarStmt node) {
    String varName = node.varName.lexeme;

    // Check if variable already exists in current environment
    if (symbolTable.existsInCurrEnv(varName)) {
      error("Variable " + varName + " already defined in this scope", node.varName);
    }

    // Check optional type
    if (node.dataType.isPresent()) {
      DataType declaredType = node.dataType.get();
      declaredType.accept(this);

      // Check initialization expression if present
      if (node.expr.isPresent()) {
        Expr initExpr = node.expr.get();
        initExpr.accept(this);

        // Check for void type mismatch
        if (currType != null && currType.type.lexeme.equals("void")) {
          error("Cannot assign void to a non-void variable", node.varName);
        }

        // Check type compatibility
        if (!typesCompatible(declaredType, currType)) {
          error("Type mismatch in variable declaration", node.varName);
        }
      }

      // Add variable with declared type to symbol table
      symbolTable.add(varName, declaredType);
    } else if (node.expr.isPresent()) {
      // Infer type from initialization expression
      Expr initExpr = node.expr.get();
      initExpr.accept(this);

      // Check for void type
      if (currType != null && currType.type.lexeme.equals("void")) {
        error("Cannot assign void to a non-void variable", node.varName);
      }

      // Can't infer type from null
      if (currType == null) {
        error("Cannot infer type without an initialization expression", node.varName);
      }

      // Add variable with inferred type to symbol table
      symbolTable.add(varName, currType);
    } else {
      error("Variable declaration requires either a type or an initialization expression", node.varName);
    }
  }

  /**
   * Checks while statements
   */
  public void visit(WhileStmt node) {
    // Check condition
    node.condition.accept(this);

    // Condition must be a boolean
    if (currType == null) {
      error("While condition cannot be null");
      return;
    }

    if (currType.isArray) {
      error("While condition cannot be a boolean array");
      return;
    }

    if (!currType.type.lexeme.equals("bool")) {
      error("While condition must be a boolean expression");
    }

    // Enter a new scope for the loop body
    symbolTable.pushEnvironment();

    // Check body statements
    for (Stmt s : node.stmts) {
      s.accept(this);
    }

    // Exit the loop body scope
    symbolTable.popEnvironment();
  }

  /**
   * Checks for statements
   */
  public void visit(ForStmt node) {
    String varName = node.varName.lexeme;

    // Enter a new scope for the loop body
    symbolTable.pushEnvironment();

    // Add loop variable to scope (shadowing possible)
    DataType intType = new DataType();
    intType.isArray = false;
    intType.type = new Token(TokenType.INT_TYPE, "int", 0, 0);
    symbolTable.add(varName, intType);

    // Check from expression
    node.fromExpr.accept(this);

    // Check if currType is null after accepting fromExpr
    if (currType == null) {
      error("For loop 'from' expression cannot be null", node.varName);
      symbolTable.popEnvironment(); // Clean up scope before returning
      return;
    }

    // From expression must be an integer. Check the type of the *expression*.
    if (!currType.type.lexeme.equals("int")) {

      // If the from expression is a variable, check its type in the symbol table
      if (node.fromExpr instanceof BasicExpr && ((BasicExpr) node.fromExpr).rvalue instanceof VarRValue) {
        VarRValue varRValue = (VarRValue) ((BasicExpr) node.fromExpr).rvalue;
        if (varRValue.path.size() == 1) {
          Token fromVarToken = varRValue.path.get(0).varName;
          DataType fromVarType = symbolTable.get(fromVarToken.lexeme);
          if (fromVarType == null || !fromVarType.type.lexeme.equals("int")) {
            error("For loop 'from' expression must be an integer", node.varName);
            symbolTable.popEnvironment(); // Clean up scope before returning
            return;
          }
        } else {
          error("For loop 'from' expression must be an integer", node.varName);
          symbolTable.popEnvironment(); // Clean up scope before returning
          return;
        }

      } else {
        error("For loop 'from' expression must be an integer", node.varName);
        symbolTable.popEnvironment(); // Clean up scope before returning
        return;
      }
    }

    // Check to expression
    node.toExpr.accept(this);

    // To expression must be an integer
    if (currType == null) {
      error("For loop 'to' expression cannot be null", node.varName);
      symbolTable.popEnvironment(); // Clean up scope before returning
      return;
    }

    // To expression must be an integer. Check the type of the *expression*.
    if (!currType.type.lexeme.equals("int")) {
      // If the to expression is a variable, check its type in the symbol table
      if (node.toExpr instanceof BasicExpr && ((BasicExpr) node.toExpr).rvalue instanceof VarRValue) {
        VarRValue varRValue = (VarRValue) ((BasicExpr) node.toExpr).rvalue;
        if (varRValue.path.size() == 1) {
          Token toVarToken = varRValue.path.get(0).varName;
          DataType toVarType = symbolTable.get(toVarToken.lexeme);
          if (toVarType == null || !toVarType.type.lexeme.equals("int")) {
            error("For loop 'to' expression must be an integer", node.varName);
            symbolTable.popEnvironment(); // Clean up scope before returning
            return;
          }
        } else {
          error("For loop 'to' expression must be an integer", node.varName);
          symbolTable.popEnvironment(); // Clean up scope before returning
          return;
        }

      } else {
        error("For loop 'to' expression must be an integer", node.varName);
        symbolTable.popEnvironment(); // Clean up scope before returning
        return;
      }
    }

    // Check body statements
    for (Stmt s : node.stmts) {
      s.accept(this);
    }

    // Exit the loop body scope
    symbolTable.popEnvironment();
  }

  /**
   * Checks if statements
   */
  public void visit(IfStmt node) {
    // Check condition
    node.condition.accept(this);

    // Condition must be a boolean
    if (currType == null) {
      error("If condition cannot be null");
      return;
    }

    if (currType.isArray) {
      error("If condition cannot be a boolean array");
      return;
    }

    if (!currType.type.lexeme.equals("bool")) {
      error("If condition must be a boolean expression");
    }

    // Enter a new scope for if body
    symbolTable.pushEnvironment();

    // Check if body statements
    for (Stmt s : node.ifStmts) {
      s.accept(this);
    }

    // Exit if body scope
    symbolTable.popEnvironment();

    // Check else-if if present
    if (node.elseIf.isPresent()) {
      node.elseIf.get().accept(this);
    }

    // Check else if present
    if (node.elseStmts.isPresent()) {
      symbolTable.pushEnvironment();

      for (Stmt s : node.elseStmts.get()) {
        s.accept(this);
      }

      symbolTable.popEnvironment();
    }
  }

  /**
   * Checks simple rvalues
   */
  public void visit(SimpleRValue node) {
    TokenType type = node.literal.tokenType;

    // Create a new data type based on the literal type
    currType = new DataType();
    currType.isArray = false;

    if (type == TokenType.INT_VAL) {
      currType.type = new Token(TokenType.INT_TYPE, "int", 0, 0);
    } else if (type == TokenType.DOUBLE_VAL) {
      currType.type = new Token(TokenType.DOUBLE_TYPE, "double", 0, 0);
    } else if (type == TokenType.BOOL_VAL) {
      currType.type = new Token(TokenType.BOOL_TYPE, "bool", 0, 0);
    } else if (type == TokenType.STRING_VAL) {
      currType.type = new Token(TokenType.STRING_TYPE, "string", 0, 0);
    } else if (type == TokenType.NULL_VAL) {
      currType = null;
    }
  }

  /**
   * Checks new array rvalues
   */
  public void visit(NewArrayRValue node) {
    String typeName = node.type.lexeme;

    // Check if array type is valid (base type or defined struct)
    if (!isBaseType(typeName) && !structs.containsKey(typeName)) {
      error("Invalid array type: " + typeName, node.type);
    }

    // Check size expression
    node.arrayExpr.accept(this);

    // Size expression must be an integer
    if (!currType.type.lexeme.equals("int")) {
      error("Array size must be an integer expression");
    }

    // Set current type to array type
    currType = new DataType();
    currType.isArray = true;
    currType.type = node.type;
  }

  /**
   * Checks new struct rvalues
   */
  public void visit(NewStructRValue node) {
    String structName = node.structName.lexeme;

    // Check if struct is defined
    if (!structs.containsKey(structName)) {
      error("Undefined struct: " + structName, node.structName);
      return;
    }

    StructDef structDef = structs.get(structName);

    // Check if number of arguments matches number of fields
    if (node.args.size() != structDef.fields.size()) {
      error("Wrong number of arguments for struct constructor", node.structName);
      return;
    }

    // Check each argument type matches corresponding field type
    for (int i = 0; i < node.args.size(); i++) {
      Expr arg = node.args.get(i);
      DataType fieldType = structDef.fields.get(i).dataType;

      arg.accept(this);

      if (!typesCompatible(fieldType, currType)) {
        error("Type mismatch in struct constructor argument", node.structName);
      }
    }

    // Set current type to struct type
    currType = new DataType();
    currType.isArray = false;
    currType.type = node.structName;
  }

  /**
   * Checks call rvalues
   */
  public void visit(CallRValue node) {
    String funName = node.funName.lexeme;

    if (isBuiltInFunction(funName)) {
      handleBuiltInFunction(node);
    } else {
      // Check if function exists
      FunDef funDef = functions.get(funName);
      if (funDef == null) {
        error("Function not defined", node.funName);
        return;
      }

      // Check number of arguments
      if (node.args.size() != funDef.params.size()) {
        error("Incorrect number of arguments for function call", node.funName);
        return;
      }

      // Check each argument type matches corresponding parameter type
      for (int i = 0; i < node.args.size(); i++) {
        Expr arg = node.args.get(i);
        DataType paramType = funDef.params.get(i).dataType;

        arg.accept(this);

        if (!typesCompatible(paramType, currType)) {
          error("Type mismatch in function call argument", node.funName);
        }
      }

      // Special case: if the function has a void return type,
      // assume it returns null which can be assigned to any type
      if (funDef.returnType != null &&
          funDef.returnType.type.tokenType == TokenType.VOID_TYPE) {
        currType = null; // null can be assigned to any type
      } else {
        // Set current type to function return type
        currType = funDef.returnType;
      }
    }
  }

  /**
   * Handle built-in function calls
   */
  private void handleBuiltInFunction(CallRValue node) {
    String funName = node.funName.lexeme;

    if (funName.equals("print") || funName.equals("println")) {
      // print and println take exactly one argument of any type
      if (node.args.size() != 1) {
        error(funName + " requires exactly one argument", node.funName);
        return;
      }

      // Check argument
      node.args.get(0).accept(this);

      // Check if argument is a struct or array
      if (currType != null) {
        if (currType.isArray) {
          error("Cannot print array objects", node.funName);
          return;
        } else if (structs.containsKey(currType.type.lexeme)) {
          error("Cannot print struct objects", node.funName);
          return;
        }
      }

      // Set return type to null (can be assigned to any type)
      currType = null;
    } else if (funName.equals("readln")) {
      // readln takes no arguments and returns a string
      if (node.args.size() != 0) {
        error("readln takes no arguments", node.funName);
        return;
      }

      currType = new DataType();
      currType.isArray = false;
      currType.type = new Token(TokenType.STRING_TYPE, "string", 0, 0);
    } else if (funName.equals("size")) {
      // size takes one argument (string or array) and returns an int
      if (node.args.size() != 1) {
        error("size requires exactly one argument", node.funName);
        return;
      }

      node.args.get(0).accept(this);

      if (currType == null || (!currType.isArray && !currType.type.lexeme.equals("string"))) {
        error("size requires a string or array argument", node.funName);
        return;
      }

      currType = new DataType();
      currType.isArray = false;
      currType.type = new Token(TokenType.INT_TYPE, "int", 0, 0);
    } else if (funName.equals("get")) {
      // get takes two arguments (int, string or array) and returns a value
      if (node.args.size() != 2) {
        error("get requires exactly two arguments", node.funName);
        return;
      }

      node.args.get(0).accept(this);
      DataType indexType = currType;

      node.args.get(1).accept(this);
      DataType containerType = currType;

      if (indexType == null || !indexType.type.lexeme.equals("int")) {
        error("get requires an integer as its first argument", node.funName);
        return;
      }

      if (containerType == null || (!containerType.isArray && !containerType.type.lexeme.equals("string"))) {
        error("get requires a string or array as its second argument", node.funName);
        return;
      }

      currType = new DataType();
      currType.isArray = false;
      currType.type = containerType.type.lexeme.equals("string")
          ? new Token(TokenType.STRING_TYPE, "string", 0, 0)
          : containerType.type;
    } else if (funName.equals("int_val") || funName.equals("dbl_val") || funName.equals("str_val")) {
      // casting functions take one argument and return a value
      if (node.args.size() != 1) {
        error(funName + " requires exactly one argument", node.funName);
        return;
      }

      node.args.get(0).accept(this);

      if (funName.equals("int_val")) {
        // Check if argument is valid for int_val (must be string or double)
        boolean validInput = currType != null &&
            (currType.type.lexeme.equals("string") ||
                currType.type.lexeme.equals("double"));

        if (!validInput) {
          error("int_val requires a string or double argument", node.funName);
          return;
        }

        currType = new DataType();
        currType.isArray = false;
        currType.type = new Token(TokenType.INT_TYPE, "int", 0, 0);
      } else if (funName.equals("dbl_val")) {
        // Check if argument is valid for dbl_val (must be string or int)
        boolean validInput = currType != null &&
            (currType.type.lexeme.equals("string") ||
                currType.type.lexeme.equals("int"));

        if (!validInput) {
          error("dbl_val requires a string or int argument", node.funName);
          return;
        }

        currType = new DataType();
        currType.isArray = false;
        currType.type = new Token(TokenType.DOUBLE_TYPE, "double", 0, 0);
      } else { // str_val
        // Check if argument is valid for str_val (must be int or double)
        boolean validInput = currType != null &&
            (currType.type.lexeme.equals("int") ||
                currType.type.lexeme.equals("double"));

        if (!validInput) {
          error("str_val requires an int or double argument", node.funName);
          return;
        }

        currType = new DataType();
        currType.isArray = false;
        currType.type = new Token(TokenType.STRING_TYPE, "string", 0, 0);
      }
    }
  }

  /**
   * Checks variable rvalues
   */
  public void visit(VarRValue node) {
    if (node.path.isEmpty()) {
      error("Empty variable path");
      return;
    }

    VarRef firstVar = node.path.get(0);
    String varName = firstVar.varName.lexeme;

    // Check if variable exists
    if (!symbolTable.exists(varName)) {
      error("Undefined variable: " + varName, firstVar.varName);
      return;
    }

    DataType currentType = symbolTable.get(varName);

    // Check array access if present for first var
    if (firstVar.arrayExpr.isPresent()) {
      if (!currentType.isArray) {
        error("Cannot index non-array variable", firstVar.varName);
        return;
      }

      Expr arrayIndex = firstVar.arrayExpr.get();
      arrayIndex.accept(this);

      // Array index must be an integer
      if (!currType.type.lexeme.equals("int")) {
        error("Array index must be an integer", firstVar.varName);
        return;
      }

      // Adjust the type to the element type
      DataType elementType = new DataType();
      elementType.isArray = false;
      elementType.type = currentType.type;
      currentType = elementType;
    }

    // Process the rest of the path (if any)
    for (int i = 1; i < node.path.size(); i++) {
      VarRef ref = node.path.get(i);
      String fieldName = ref.varName.lexeme;

      // Current type must be a struct
      if (!structs.containsKey(currentType.type.lexeme)) {
        error("Cannot access field of non-struct type", ref.varName);
        return;
      }

      StructDef structDef = structs.get(currentType.type.lexeme);

      // Check if field exists
      if (!isStructField(fieldName, structDef)) {
        error("Undefined field: " + fieldName, ref.varName);
        return;
      }

      // Update current type to field type
      currentType = getStructFieldType(fieldName, structDef);

      // Check if we're trying to access a field without array indexing, but the field
      // is an array
      if (i < node.path.size() - 1 && currentType.isArray && !ref.arrayExpr.isPresent()) {
        error("Cannot access field of array without indexing", node.path.get(i + 1).varName);
        return;
      }

      // AFTER getting the field type, check array access if present
      if (ref.arrayExpr.isPresent()) {
        if (!currentType.isArray) {
          error("Cannot index non-array field", ref.varName);
          return;
        }

        Expr arrayIndex = ref.arrayExpr.get();
        arrayIndex.accept(this);

        // When validating array indices, combine these checks
        boolean validIndex = currType != null && currType.type.lexeme.equals("int");
        if (!validIndex) {
          error("Array index must be an integer", ref.varName);
          return;
        }

        // Adjust the type to the element type
        DataType elementType = new DataType();
        elementType.isArray = false;
        elementType.type = currentType.type;
        currentType = elementType;
      }
    }

    // Set current type to the determined type
    currType = currentType;
  }

  /**
   * Checks binary expressions
   */
  public void visit(BinaryExpr node) {
    // Check left hand side
    node.lhs.accept(this);
    DataType lhsType = currType;

    // Check right hand side
    node.rhs.accept(this);
    DataType rhsType = currType;

    String op = node.binaryOp.lexeme;

    // Handle different operator types
    if (op.equals("+")) {
      // Addition works for int, double, and string
      if (lhsType != null && rhsType != null &&
          !lhsType.isArray && !rhsType.isArray) {

        String lhsTypeName = lhsType.type.lexeme;
        String rhsTypeName = rhsType.type.lexeme;

        // Check if both operands have the same type from our allowed list
        if (lhsTypeName.equals(rhsTypeName) && List.of("int", "double", "string").contains(lhsTypeName)) {
          currType = lhsType; // Result has same type as operands
        } else {
          error("Invalid types for + operator", node.binaryOp);
        }
      } else {
        error("Cannot use + operator with arrays or null", node.binaryOp);
      }
    } else if (op.equals("-") || op.equals("*") || op.equals("/")) {
      // Arithmetic operators work for int and double
      if (lhsType != null && rhsType != null &&
          !lhsType.isArray && !rhsType.isArray) {

        String lhsTypeName = lhsType.type.lexeme;
        String rhsTypeName = rhsType.type.lexeme;

        if ((lhsTypeName.equals("int") && rhsTypeName.equals("int")) ||
            (lhsTypeName.equals("double") && rhsTypeName.equals("double"))) {
          currType = lhsType; // Result has same type as operands
        } else {
          error("Invalid types for arithmetic operator", node.binaryOp);
        }
      } else {
        error("Cannot use arithmetic operators with arrays or null", node.binaryOp);
      }
    } else if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=")) {
      // Comparison operators work for int, double, and string
      if (lhsType != null && rhsType != null &&
          !lhsType.isArray && !rhsType.isArray) {

        String lhsTypeName = lhsType.type.lexeme;
        String rhsTypeName = rhsType.type.lexeme;

        if ((lhsTypeName.equals("int") && rhsTypeName.equals("int")) ||
            (lhsTypeName.equals("double") && rhsTypeName.equals("double")) ||
            (lhsTypeName.equals("string") && rhsTypeName.equals("string"))) {
          // Result is bool
          currType = new DataType();
          currType.isArray = false;
          currType.type = new Token(TokenType.BOOL_TYPE, "bool", 0, 0);
        } else {
          error("Invalid types for comparison operator", node.binaryOp);
        }
      } else {
        error("Cannot use comparison operators with arrays or null", node.binaryOp);
      }
    } else if (op.equals("==") || op.equals("!=")) {
      // Equality operators work for any two types that are the same
      if (lhsType == null || rhsType == null) {
        // null can be compared with any type
        currType = new DataType();
        currType.isArray = false;
        currType.type = new Token(TokenType.BOOL_TYPE, "bool", 0, 0);
      } else if (typesCompatible(lhsType, rhsType)) {
        // Result is bool
        currType = new DataType();
        currType.isArray = false;
        currType.type = new Token(TokenType.BOOL_TYPE, "bool", 0, 0);
      } else {
        error("Cannot compare incompatible types", node.binaryOp);
      }
    } else if (op.equals("and") || op.equals("or")) {
      // Logical operators work only for bool
      if (lhsType != null && rhsType != null &&
          !lhsType.isArray && !rhsType.isArray &&
          lhsType.type.lexeme.equals("bool") && rhsType.type.lexeme.equals("bool")) {
        // Result is bool
        currType = lhsType;
      } else {
        error("Logical operators require boolean operands", node.binaryOp);
      }
    }
  }

  /**
   * Checks unary expressions
   */
  public void visit(UnaryExpr node) {
    // Check the expression
    node.expr.accept(this);

    String op = node.unaryOp.lexeme;

    if (op.equals("not")) {
      // 'not' only works with boolean
      if (currType != null && !currType.isArray && currType.type.lexeme.equals("bool")) {
        // Result is bool
        // currType is already bool, so no need to change
      } else {
        error("'not' operator requires a boolean operand", node.unaryOp);
      }
    } else if (op.equals("-")) {
      // Unary minus works with int and double
      if (currType != null && !currType.isArray &&
          (currType.type.lexeme.equals("int") || currType.type.lexeme.equals("double"))) {
        // Result has same type as operand
        // currType is already correct, so no need to change
      } else {
        error("Unary minus requires an integer or double operand", node.unaryOp);
      }
    }
  }

  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }
}