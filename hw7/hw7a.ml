(*----------------------------------------------------------------------
 * CPSC 326, Spring 2025
 * HW-7 (Part A) Function Implementations
 *
 * Arjuna Herbst
 *
 *----------------------------------------------------------------------*)

(* Helper functions to use in your function definitions *)

let head xs =
  match xs with
  | [] -> failwith "Empty List"
  | x::_ -> x

let tail xs =
  match xs with
  | [] -> failwith "Empty List"
  | _::t -> t

let rec length xs =
  match xs with
  | [] -> 0
  | _::t -> 1 + length t



(* TODO: write the following functions
 *
 * NOTES:
 *   -- read instructions carefully!
 *   -- you cannot use pattern matching for these
 *   -- only use constructs listed in HW-7
 *)


(* my_min : 'a list -> 'a
 *
 * Finds the minimum value in the given list.
 * Throws an exception if the list is empty. 
 *)
 let rec my_min xs =
  if length xs = 0 then
    failwith "Empty List"
  else if length xs = 1 then
    head xs
  else
    let tmin = my_min (tail xs) in
    let h = head xs in
    if h < tmin then h else tmin



(* my_reverse : 'a list -> 'a list
 *
 * Reverses the given list.
 *)
 let rec my_reverse xs =
  let rec rev_helper xs acc =
    if length xs = 0 then
      acc
    else
      rev_helper (tail xs) (head xs :: acc)
  in
  rev_helper xs []


(* my_take : int -> 'a list -> 'a list
 *
 * Keeps the first n elements of the list.
 *) 
 let rec my_take n xs =
  if n <= 0 then
    []
  else if length xs = 0 then
    []
  else
    head xs :: my_take (n - 1) (tail xs)



(* my_drop : int -> 'a list -> 'a list
 *
 * Removes the first n elements of the list.
 *)
 let rec my_drop n xs =
  if n <= 0 then
    xs
  else if length xs = 0 then
    []
  else
    my_drop (n - 1) (tail xs)


(* my_set : int -> 'a -> 'a list -> 'a list
 *
 * Sets the i-th element of xs to x.
 * Throws exception if the index is invalid.
 *)
 let rec my_set i x xs =
  if i < 0 then
    failwith "Invalid Index"
  else if length xs = 0 then
    failwith "Invalid Index"
  else if i = 0 then
    x :: tail xs
  else
    head xs :: my_set (i - 1) x (tail xs)


(* my_init : 'a list -> 'a list
 *
 * Removes the last element from the list.
 * Throws exception if list is empty.
 *)
 let rec my_init xs =
  if length xs = 0 then
    failwith "Empty List"
  else if length xs = 1 then
    []
  else
    head xs :: my_init (tail xs)


(* kv_build : 'a list -> 'b list -> ('a * 'b) list
 *
 * Creates a list of key-value pairs from list of keys and values.
 * The number of pairs is equal to the smallest of the two lists.
 *)
 let rec kv_build ks vs =
  if length ks = 0 then
    []
  else if length vs = 0 then
    []
  else
    (head ks, head vs) :: kv_build (tail ks) (tail vs)


(* kv_key : 'a -> ('a * 'b) list -> bool
 *
 * True if k is a key in the key-value pair.
 *)
 let rec kv_key k kvs =
  if length kvs = 0 then
    false
  else
    let pair = head kvs in
    if fst pair = k then
      true
    else
      kv_key k (tail kvs)


(* kv_remove : 'a -> ('a * 'b) list -> ('a * 'b) list
 *
 * Removes all pairs with key k.
 *)
 let rec kv_remove k kvs =
  if length kvs = 0 then
    []
  else
    let pair = head kvs in
    if fst pair = k then
      kv_remove k (tail kvs)
    else
      pair :: kv_remove k (tail kvs)


(* kv_collect ('a * 'b) list -> ('a * 'b list) list
 *
 * Gathers values for keys creating a unique set of keys,
 * where each key has a list of its corresponing values.
 *)
 let rec kv_collect kvs =
  if length kvs = 0 then
    []
  else
    let pair = head kvs in
    let k = fst pair in
    let rec collect_values k kvs =
      if length kvs = 0 then
        []
      else
        let p = head kvs in
        if fst p = k then
          snd p :: collect_values k (tail kvs)
        else
          collect_values k (tail kvs)
    in
    let values = collect_values k kvs in
    (k, values) :: kv_collect (kv_remove k kvs)
