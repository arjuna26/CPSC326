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
 let rec eval e =
  match e with
  | Int n -> n
  | Plus (e1, e2) -> eval e1 + eval e2
  | Minus (e1, e2) -> eval e1 - eval e2
  | Times (e1, e2) -> eval e1 * eval e2
  | Divide (e1, e2) -> eval e1 / eval e2
  | Negate e1 -> -(eval e1)
  | If (cond, then_e, else_e) ->
      if eval cond <> 0 then eval then_e else eval else_e
  | Iterate (n, f, e1) ->
      let rec iter i acc =
        if i = 0 then acc
        else iter (i - 1) (f acc)
      in
      iter n (eval e1)

      
