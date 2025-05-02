%======================================================================
% CPSC 326, Spring 2025
% HW-8 Extra Credit Part 2
%
% YOUR NAME HERE
%
%======================================================================

% TODO: Write a Clingo program to solve instances of Futoshiki
% (https://www.futoshiki.org/).
%
% Input:
%   -- size(n) where n is the size of the n x n board
%   -- lt(r1,c1,r2,c2) for r1, c1 a row column pair (each between 0
%      and n-1) and r2, c2 a row column pair, each representing cells
%      such that the first cell's value (r1,c1) must be less than the
%      second cell's value (r2,c2). 
%   -- cell(r,c,v) giving the initial value for a cell (as a row r,
%      column c). Empty cells, i.e., cells that still need to be
%      filled in as part of the game, are not given as input facts.
%
% Output:
%   -- a cell/3 fact for each cell in the solved game.
%
% To run: (gives one solution, to see all, add 0 to the end)
%   clingo ec_2.lp ec_2_input_game.lp 
%


% TODO: Put your answer here, before the #show directive


#show cell/3.
