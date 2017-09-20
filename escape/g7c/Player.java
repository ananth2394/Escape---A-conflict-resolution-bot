package escape.g7c;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Player implements escape.sim.Player {
    private boolean DEBUG = false;

    private Random rand;
    private int turn;
    private int n;
    private int lastMove;
    private int nextLastMove;
    private int oddOwnedHandle = -1;
    private int evenOwnedHandle = -1;
    private ArrayList<Integer> moves;  // Represents the handles held in the previous turns. Zero-based.
    private ArrayList<List<Integer>> conflictsPerRound;

    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        this.conflictsPerRound = new ArrayList<List<Integer>>();
        this.moves = new ArrayList<Integer>();

        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        int move = this.getMove(conflicts);
        this.nextLastMove = this.lastMove;
        this.lastMove = move;
        this.moves.add(move);
        this.turn++;
        if (DEBUG) {
            System.out.printf("oddOwnedHandle: %d, evenOwnedHandle: %d, moves: %s\n",
                oddOwnedHandle, evenOwnedHandle, moves);
        }
        return move + 1;
    }

    public int getMove(List<Integer> conflicts) {
        boolean isFirstTurn = this.turn == 0;
        if (isFirstTurn) return 0;

        /*
         * If there are not conflicts, and the previous handle is not already
         * owned in the next turn, own it in this kind (even or odd) of turns.
         */
        boolean isOddTurn = (this.turn % 2) != 0;
        boolean hasConflicted = conflicts.size() != 0;
        if (!hasConflicted) {
            if (isOddTurn) {
                if (this.oddOwnedHandle != this.lastMove)
                    this.evenOwnedHandle = this.lastMove;
            } else {
                if (this.evenOwnedHandle != this.lastMove)
                    this.oddOwnedHandle = this.lastMove;
            }
        }

        boolean hasOddOwnedHandle = this.oddOwnedHandle != -1;
        boolean hasEvenOwnedHandle = this.evenOwnedHandle != -1;
        if (isOddTurn) {
            if (hasOddOwnedHandle) {
                return this.oddOwnedHandle;
            } else if (hasEvenOwnedHandle) {
                return this.chooseRandomExcluding(this.evenOwnedHandle, conflicts);
            }
        } else {
            if (hasEvenOwnedHandle) {
                return this.evenOwnedHandle;
            } else if (hasOddOwnedHandle) {
                return this.chooseRandomExcluding(this.oddOwnedHandle, conflicts);
            }
        }
        return this.chooseRandom(conflicts);
    }

    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts);
    }

    public int chooseRandomExcluding(int excluding, List<Integer> conflicts) {
        boolean avoidLast = conflicts.size() == 0;
        List<Integer> choices = new ArrayList<Integer>();
        for (int i = 0; i < this.n; i++) {
            choices.add(i);
        }
        if (conflicts.size() != 0) {
            choices.remove(new Integer(this.lastMove));
        }
        choices.remove(new Integer(excluding));
        int index = this.rand.nextInt(choices.size());
        return choices.get(index);
    }
}
