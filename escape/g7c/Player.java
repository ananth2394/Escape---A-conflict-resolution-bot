package escape.g7c;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Player implements escape.sim.Player {
    private Random rand;

    private int turn;
    private int n;
    private int lastMove;
    private int nextLastMove;
    private int ownedHandle = -1;
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
        return move + 1;
    }

    public int getMove(List<Integer> conflicts) {
        if (this.turn == 0) {
            return 0;
        } else if ((this.turn % 2) != 0) {
            if (conflicts.size() == 0) {
                this.ownedHandle = this.lastMove;
            }
            if (this.ownedHandle != -1) {
                return this.chooseRandomExcluding(this.ownedHandle, conflicts);
            } else {
                return this.chooseRandom(conflicts);
            }
        } else {
            if (this.ownedHandle != -1) {
                return this.ownedHandle;
            } else {
                return this.chooseRandomExcluding(this.nextLastMove, conflicts);
            }
        }
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
