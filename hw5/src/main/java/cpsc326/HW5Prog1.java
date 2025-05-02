/**
 * CPSC 326, Spring 2025
 * Example program 1 for HW-5.
 */

package cpsc326;

/**
 * Class for HW5 program 1.
 */
public class HW5Prog1 {

  // Implement the following MyPL program:
  //
  // bool is_prime(n: int) {
  // var m: int = n / 2
  // var v: int = 2
  // while v <= m {
  // var r: int = n / v
  // var p: int = r * v
  // if p == n {
  // return false
  // }
  // v = v + 1
  // }
  // return true
  // }
  //
  // void main() {
  // println("Please enter integer values to sum (prime to quit)")
  // var sum: int = 0
  // while true {
  // print("Enter an int: ")
  // var val: int = int_val(readln())
  // if is_prime(val) {
  // println("The sum is: " + str_val(sum))
  // println("Goodbye!")
  // return null
  // }
  // sum = sum + val
  // }
  // }

  public static void main(String[] args) {
    VMFrameTemplate m = new VMFrameTemplate("main");
    VMFrameTemplate p = new VMFrameTemplate("is_prime");

    // is_prime(n: int)
    // store function argument n in memory[0]
    p.add(VMInstr.STORE(0)); // param n

    // m = n / 2
    p.add(VMInstr.LOAD(0)); 
    p.add(VMInstr.PUSH(2));
    p.add(VMInstr.DIV());
    p.add(VMInstr.STORE(1)); // memory[1] holds m

    // v = 2
    p.add(VMInstr.PUSH(2));
    p.add(VMInstr.STORE(2)); // memory[2] holds v

    // while v <= m:
    int condIndex = p.instructions.size();

    // push condition (v <= m)
    p.add(VMInstr.LOAD(2)); // v
    p.add(VMInstr.LOAD(1)); // m
    p.add(VMInstr.CMPLE()); // v <= m?

    // jump out of loop if false
    int jmpOutIndex = p.instructions.size();
    p.add(VMInstr.JMPF(0)); // placeholder

    // r = n / v
    p.add(VMInstr.LOAD(0)); 
    p.add(VMInstr.LOAD(2)); 
    p.add(VMInstr.DIV());
    p.add(VMInstr.STORE(3)); // memory[3] holds r

    // pVal = r * v
    p.add(VMInstr.LOAD(3));
    p.add(VMInstr.LOAD(2));
    p.add(VMInstr.MUL());
    p.add(VMInstr.STORE(4)); // memory[4] holds pVal

    // if pVal == n => return false
    p.add(VMInstr.LOAD(4));
    p.add(VMInstr.LOAD(0));
    p.add(VMInstr.CMPEQ());
    int jmpNotEqIndex = p.instructions.size();
    p.add(VMInstr.JMPF(0));  // jump if not equal
    p.add(VMInstr.PUSH(false));
    p.add(VMInstr.RET());

    // fix up the "not equal" jump to continue
    p.instructions.get(jmpNotEqIndex).operand = p.instructions.size();

    // v = v + 1
    p.add(VMInstr.LOAD(2));
    p.add(VMInstr.PUSH(1));
    p.add(VMInstr.ADD());
    p.add(VMInstr.STORE(2));

    // jump back to condition
    p.add(VMInstr.JMP(condIndex));

    // fix up the while's JMPF to exit here
    int exitPoint = p.instructions.size();
    p.instructions.get(jmpOutIndex).operand = exitPoint;

    // if we exit the loop => return true
    p.add(VMInstr.PUSH(true));
    p.add(VMInstr.RET());

    // main()
    m.add(VMInstr.PUSH("Please enter integer values to sum (prime to quit)\n"));
    m.add(VMInstr.WRITE());

    // var sum = 0 => memory[0]
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.STORE(0));

    // while true
    int loopStart = m.instructions.size();
    m.add(VMInstr.PUSH("Enter an int: "));
    m.add(VMInstr.WRITE());

    // val = int_val(readln()) => memory[1]
    m.add(VMInstr.READ());
    m.add(VMInstr.TOINT());
    m.add(VMInstr.STORE(1));

    // if is_prime(val) ...
    m.add(VMInstr.LOAD(1));
    m.add(VMInstr.CALL("is_prime")); // result on stack

    int jmpfIndex = m.instructions.size();
    m.add(VMInstr.JMPF(0)); // placeholder

    // if prime => print sum, "Goodbye!", return
    m.add(VMInstr.PUSH("The sum is: "));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.TOSTR());
    m.add(VMInstr.WRITE());
    m.add(VMInstr.PUSH("\nGoodbye!\n"));
    m.add(VMInstr.WRITE());
    m.add(VMInstr.RET());

    // fix up the JMPF to skip the "prime" block
    int skipPrime = m.instructions.size();
    m.instructions.get(jmpfIndex).operand = skipPrime;

    // sum = sum + val
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.LOAD(1));
    m.add(VMInstr.ADD());
    m.add(VMInstr.STORE(0));

    // jump to loopStart
    m.add(VMInstr.JMP(loopStart));

    // create and run the VM
    VM vm = new VM();
    vm.add(p);
    vm.add(m);
    vm.run();
  }
}
