(*----------------------------------------------------------------------
 * CPSC 326, Spring 2025
 * HW-7 (Part B) Function Implementations
 *
 * Arjuna Herbst
 *
 *----------------------------------------------------------------------*)


(* TODO: write the following functions
 *
 * NOTES:
 *   -- read instructions carefully!
 *   -- you cannot use if-then-else for these
 *   -- only use constructs listed in HW-7
 *)



(* my_min : 'a list -> 'a
 *
 * Finds the minimum value in the given list.
 * Throws an exception if the list is empty.
 *)
 let rec my_min xs =
  match xs with
  | [] -> failwith "Empty List"
  | [x] -> x
  | h :: t ->
      let tmin = my_min t in
      match h < tmin with
      | true -> h
      | false -> tmin



(* my_reverse : 'a list -> 'a list
 *
 * Reverses the given list.
 *)
 let rec my_reverse xs =
  let rec rev_helper xs acc =
    match xs with
    | [] -> acc
    | h :: t -> rev_helper t (h :: acc)
  in
  rev_helper xs []


(* my_take : int -> 'a list -> 'a list
 *
 * Keeps the first n elements of the list.
 *)
 let rec my_take n xs =
  match (n, xs) with
  | (n, _) when n <= 0 -> []
  | (_, []) -> []
  | (n, h :: t) -> h :: my_take (n - 1) t


(* my_drop : int -> 'a list -> 'a list
 *
 * Removes the first n elements of the list.
 *)
 let rec my_drop n xs =
  match (n, xs) with
  | (n, l) when n <= 0 -> l
  | (_, []) -> []
  | (n, _ :: t) -> my_drop (n - 1) t


(* my_set : int -> 'a -> 'a list -> 'a list
 *
 * Sets the i-th element of xs to x.
 * Throws exception if the index is invalid.
 *)
 let rec my_set i x xs =
  match (i, xs) with
  | (i, []) when i >= 0 -> failwith "Invalid Index"
  | (i, h :: t) when i = 0 -> x :: t
  | (i, h :: t) when i > 0 -> h :: my_set (i - 1) x t
  | (i, _) -> failwith "Invalid Index"


(* my_init : 'a list -> 'a list
 *
 * Removes the last element from the list.
 * Throws exception if list is empty.
 *)
 let rec my_init xs =
  match xs with
  | [] -> failwith "Empty List"
  | [_] -> []
  | h :: t -> h :: my_init t


(* kv_build : 'a list -> 'b list -> ('a * 'b) list
 *
 * Creates a list of key-value pairs from list of keys and values.
 * The number of pairs is equal to the smallest of the two lists.
 *)
 let rec kv_build ks vs =
  match (ks, vs) with
  | ([], _) -> []
  | (_, []) -> []
  | (k :: kt, v :: vt) -> (k, v) :: kv_build kt vt


(* kv_key : 'a -> ('a * 'b) list -> bool
 *
 * True if k is a key in the key-value pair.
 *)
 let rec kv_key k kvs =
  match kvs with
  | [] -> false
  | (k', _) :: t when k = k' -> true
  | _ :: t -> kv_key k t


(* kv_remove : 'a -> ('a * 'b) list -> ('a * 'b) list
 *
 * Removes all pairs with key k.
 *)
 let rec kv_remove k kvs =
  match kvs with
  | [] -> []
  | (k', v) :: t when k = k' -> kv_remove k t
  | pair :: t -> pair :: kv_remove k t


(* kv_collect ('a * 'b) list -> ('a * 'b list) list
 *
 * Gathers values for keys creating a unique set of keys,
 * where each key has a list of its corresponing values.
 *)
 let rec kv_collect kvs =
  match kvs with
  | [] -> []
  | (k, v) :: t ->
      let rec collect_values k lst =
        match lst with
        | [] -> []
        | (k', v') :: t' when k = k' -> v' :: collect_values k t'
        | _ :: t' -> collect_values k t'
      in
      let values = collect_values k kvs in
      (k, values) :: kv_collect (kv_remove k kvs)
