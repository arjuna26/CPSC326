/**
 * CPSC 326, Spring 2025
 * Example program 2 for HW-5.
 */

package cpsc326;

/**
 * Class for HW5 program 2.
 */
public class HW5Prog2 {

  // Implement the following MyPL program:
  //
  // struct Team {
  // name: string,
  // wins: int,
  // games: int
  // }
  //
  // void main() {
  // var teams: Team = new Team[2]
  // teams[0] = new Team("a", 10, 20);
  // teams[1] = new Team("b", 18, 20);
  // double sum = 0.0;
  // sum = sum + (dbl_val(teams[0].wins) / dbl_val(teams[0].games))
  // sum = sum + (dbl_val(teams[1].wins) / dbl_val(teams[1].games))
  // print("The average win percentage is: ")
  // print(sum / 2)
  // println("")
  // }

  public static void main(String[] args) {
    VMFrameTemplate m = new VMFrameTemplate("main");

    // Create an array of size 2
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.STORE(0)); 

    // teams[0] = new Team("a", 10, 20)
    m.add(VMInstr.LOAD(0)); 
    m.add(VMInstr.PUSH(0)); 
    m.add(VMInstr.ALLOCS()); 

    // Set name field to "a"
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH("a"));
    m.add(VMInstr.SETF("name"));

    // Set wins field to 10
    m.add(VMInstr.DUP()); 
    m.add(VMInstr.PUSH(10)); 
    m.add(VMInstr.SETF("wins")); 

    // Set games field to 20
    m.add(VMInstr.DUP()); 
    m.add(VMInstr.PUSH(20));
    m.add(VMInstr.SETF("games"));

    m.add(VMInstr.SETI()); 

    // teams[1] = new Team("b", 18, 20)
    m.add(VMInstr.LOAD(0)); 
    m.add(VMInstr.PUSH(1)); 
    m.add(VMInstr.ALLOCS()); 

    // Set name field to "b"
    m.add(VMInstr.DUP()); 
    m.add(VMInstr.PUSH("b")); 
    m.add(VMInstr.SETF("name")); 

    // Set wins field to 18
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH(18)); 
    m.add(VMInstr.SETF("wins")); 

    // Set games field to 20
    m.add(VMInstr.DUP()); 
    m.add(VMInstr.PUSH(20)); 
    m.add(VMInstr.SETF("games")); 

    m.add(VMInstr.SETI()); // Set teams[1] to the Team struct

    // double sum = 0.0
    m.add(VMInstr.PUSH(0.0)); 
    m.add(VMInstr.STORE(1)); 

    // sum = sum + (dbl_val(teams[0].wins) / dbl_val(teams[0].games))
    m.add(VMInstr.LOAD(0)); 
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.GETI());
    m.add(VMInstr.GETF("wins")); 
    m.add(VMInstr.TODBL());

    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0)); 
    m.add(VMInstr.GETI()); 
    m.add(VMInstr.GETF("games")); 
    m.add(VMInstr.TODBL());

    m.add(VMInstr.DIV());
    m.add(VMInstr.LOAD(1)); 
    m.add(VMInstr.ADD()); 
    m.add(VMInstr.STORE(1)); 

    // sum = sum + (dbl_val(teams[1].wins) / dbl_val(teams[1].games))
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(1)); 
    m.add(VMInstr.GETI()); 
    m.add(VMInstr.GETF("wins")); 
    m.add(VMInstr.TODBL()); 

    m.add(VMInstr.LOAD(0)); 
    m.add(VMInstr.PUSH(1)); 
    m.add(VMInstr.GETI());
    m.add(VMInstr.GETF("games"));
    m.add(VMInstr.TODBL()); 

    m.add(VMInstr.DIV()); // Division: wins/games
    m.add(VMInstr.LOAD(1)); 
    m.add(VMInstr.ADD()); // Add to sum
    m.add(VMInstr.STORE(1)); 

    // print("The average win percentage is: ")
    m.add(VMInstr.PUSH("The average win percentage is: "));
    m.add(VMInstr.WRITE()); 

    // print(sum / 2)
    m.add(VMInstr.LOAD(1)); 
    m.add(VMInstr.PUSH(2.0)); 
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());

    // println("")
    m.add(VMInstr.PUSH("\n")); 
    m.add(VMInstr.WRITE()); 

    // create the vm:
    VM vm = new VM();
    // add the frame:
    vm.add(m);
    // run the vm:
    vm.run();
  }
}
