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



(* member: 'a -> 'a set -> bool
 *
 * Checks if the value is in the set.
 *)



(* subset: 'a set -> 'a set -> bool
 *
 * Checks if the first set is a proper subset of the second.
 *)



(* equal: 'a set -> 'a set -> bool
 *
 * Checks if the two sets have the same elements.
 *)



(* union: 'a set -> 'a set -> 'a set
 *
 * Unions two sets.
 * The resulting set is ordered and duplicate free.  
 *)



(* difference: 'a set -> 'a set -> 'a set
 *
 * Performs set difference.
 * The resulting set is ordered and duplicate free.  
 *)



(* intersect: 'a set -> 'a set -> 'a set
 *
 * Intersects the two sets.
 * The resulting set is ordered and duplicate free.  
 *)



(* set_map : ('a -> b') -> 'a set -> 'b set
 * 
 * A map function for the set type. 
 * The resulting set is ordered and duplicate free.  
 *)


