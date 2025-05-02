(*----------------------------------------------------------------------
 * CPSC 326, Spring 2025
 * HW-8 OCaml Data Structure Implementations, Part A
 *
 * Arjuna Herbst
 *
 *----------------------------------------------------------------------*)

(* Simple algebraic type for representing expressions *)
type expr = Int of int
          | Plus of expr * expr
          | Minus of expr * expr
          | Times of expr * expr
          | Divide of expr * expr
          | Negate of expr
          | If of expr * expr * expr
          | Iterate of int * (int -> int) * expr


(* eval : expr -> int
 *
 * Evaluates a given expr.
 *)

      
