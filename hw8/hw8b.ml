(*----------------------------------------------------------------------
 * CPSC 326, Spring 2025
 * HW-8 OCaml Data Structure Implementations, Part B
 *
 * Arjuna Herbst
 *
 *----------------------------------------------------------------------*)


(* Simple linked-list based, ordered set data type *) 
type 'a set = Elem of 'a * 'a set 
            | EmptySet

(* add: 'a -> 'a set -> 'a set
 *
 * Adds a value to the set. 
 * The resulting set is ordered and duplicate free.  
 *)
 let rec add x s =
  match s with
  | EmptySet -> Elem (x, EmptySet)
  | Elem (y, ys) ->
      if x < y then Elem (x, s)
      else if x = y then s
      else Elem (y, add x ys)


(* member: 'a -> 'a set -> bool
 *
 * Checks if the value is in the set.
 *)
 let rec member x s =
  match s with
  | EmptySet -> false
  | Elem (y, ys) ->
      if x = y then true
      else if x < y then false
      else member x ys

(* subset: 'a set -> 'a set -> bool
 *
 * Checks if the first set is a proper subset of the second.
 *)
 let rec subset s1 s2 =
  match s1, s2 with
  | EmptySet, _ -> true
  | _, EmptySet -> false
  | Elem (x, xs), Elem (y, ys) ->
      if x = y then subset xs ys
      else if x < y then false
      else subset s1 ys

(* equal: 'a set -> 'a set -> bool
 *
 * Checks if the two sets have the same elements.
 *)
 let rec equal s1 s2 =
  match s1, s2 with
  | EmptySet, EmptySet -> true
  | Elem (x, xs), Elem (y, ys) -> x = y && equal xs ys
  | _, _ -> false

(* union: 'a set -> 'a set -> 'a set
 *
 * Unions two sets.
 * The resulting set is ordered and duplicate free.  
 *)
 let rec union s1 s2 =
  match s1, s2 with
  | EmptySet, s | s, EmptySet -> s
  | Elem (x, xs), Elem (y, ys) ->
      if x = y then Elem (x, union xs ys)
      else if x < y then Elem (x, union xs s2)
      else Elem (y, union s1 ys)

(* difference: 'a set -> 'a set -> 'a set
 *
 * Performs set difference.
 * The resulting set is ordered and duplicate free.  
 *)
 let rec difference s1 s2 =
  match s1, s2 with
  | EmptySet, _ -> EmptySet
  | s, EmptySet -> s
  | Elem (x, xs), Elem (y, ys) ->
      if x = y then difference xs ys
      else if x < y then Elem (x, difference xs s2)
      else difference s1 ys

(* intersect: 'a set -> 'a set -> 'a set
 *
 * Intersects the two sets.
 * The resulting set is ordered and duplicate free.  
 *)
 let rec intersect s1 s2 =
  match s1, s2 with
  | EmptySet, _ | _, EmptySet -> EmptySet
  | Elem (x, xs), Elem (y, ys) ->
      if x = y then Elem (x, intersect xs ys)
      else if x < y then intersect xs s2
      else intersect s1 ys

(* set_map : ('a -> b') -> 'a set -> 'b set
 * 
 * A map function for the set type. 
 * The resulting set is ordered and duplicate free.  
 *)
 let rec set_map f s =
  let rec aux s acc =
    match s with
    | EmptySet -> acc
    | Elem (x, xs) -> aux xs (add (f x) acc)
  in
  aux s EmptySet
