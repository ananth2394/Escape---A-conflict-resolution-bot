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
    private ArrayList<Integer>[] conflictsByPlayer;
    private boolean willStay = false;
    private int resolvingWith = -1;
    private int resolvingOn = -1;
    private ArrayList<Integer> avoid;

    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        this.conflictsByPlayer = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            this.conflictsByPlayer[i] = new ArrayList<Integer>();
        }
        this.conflictsPerRound = new ArrayList<List<Integer>>();
        this.moves = new ArrayList<Integer>();
        this.avoid = new ArrayList<Integer>();

        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        if (conflicts != null) {
            this.processLastRound(conflicts);
        }
        int move = this.getNextMove(conflicts);
        this.nextLastMove = this.lastMove;
        this.lastMove = move;
        this.moves.add(move);
        this.turn++;
        return move + 1;
    }
    
    // Log any information that will help us chose where to go next
    public void processLastRound(List<Integer> conflicts) {
        int move = this.lastMove;
        int turn = this.turn - 1;
        for (int i = 0; i < conflicts.size(); i++) {
            // Remember the history of where we saw each player
            this.conflictsByPlayer[conflicts.get(i) - 1].add(move);
        }
        if ((turn % 2) == 0) {
            if (conflicts.size() == 0) {
                this.ownedHandle = this.lastMove;
            } else if (conflicts.size() == 1) {
                this.resolvingWith = conflicts.get(0);
                this.resolvingOn = move;
                if (this.ownedHandle != -1) {
                    this.willStay = true;
                } else {
                    this.willStay = (rand.nextInt(2) == 0);
                }
            }
        } else if ((turn % 2) == 1) {
            if (resolvingWith != -1) {
                if (willStay == false && !conflicts.contains(resolvingWith)) {
                    avoid.add(resolvingOn);
                    resolvingWith = -1;
                    resolvingOn = -1;
                }
            }
        }
    }
    
    // Actually make the choice for the next move
    public int getNextMove(List<Integer> conflicts) {
        if (this.turn == 0) {
            return 0;
        } else if ((this.turn % 2) == 1) {
            if (this.resolvingWith != -1) {
                int resolutionHandle = conflictResolutionHandle(resolvingWith);
                if (resolutionHandle != -1) {
                    if (willStay == false) {
                        return resolutionHandle;
                    } else {
                        return this.chooseRandomExcluding(resolutionHandle, conflicts);
                    }
                }
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
    
    // When we conflict with one other player, we return to the last handle
    // we conflicted at to signal intent to leave, or any other handle to
    // signal intent to stay.
    public int conflictResolutionHandle(int player) {
        ArrayList<Integer> playerHist = conflictsByPlayer[player - 1];
        if (playerHist.size() == 0) {
            return -1;
        }
        for (int i = playerHist.size() - 1; i > 0; i--) {
            int pastHandle = playerHist.get(i);
            if (pastHandle != this.lastMove) {
                return pastHandle;
            }
        }
        return -1;
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
