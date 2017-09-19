package escape.g7c;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

public class Player implements escape.sim.Player {
    private Random rand;
	
    private int turn;
    private int n;
    private int lastMove;
    private int nextLastMove;
    private int ownedHandle = -1;
    private ArrayList<Integer> moves;  // Represents the handles held in the previous turns. Zero-based.
    private ArrayList<List<Integer>> conflictsPerRound;
    private double[] weightsEven;
    private double[] weightsOdd;
    private double totalWeight = 0.0;
    private int ownedEven = -1;
    private int ownedOdd = -1;
	
    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        this.conflictsPerRound = new ArrayList<List<Integer>>();
        this.moves = new ArrayList<Integer>();
        weightsEven = new double[n];
        weightsOdd = new double[n];
        for (int i=0; i<n; ++i){
            totalWeight += 100;
            weightsEven[i] = 100;
            weightsOdd[i] = 100;
        }
        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        int move = this.getMove(conflicts);
        //System.out.println(conflicts);
        //System.out.println("hihihi");
        this.nextLastMove = this.lastMove;
        this.lastMove = move;
        this.moves.add(move);
        this.turn++;
        return move + 1;
    }
    
    public int getMove(List<Integer> conflicts) {
        if (this.turn == 0) {
            return 0;
        } 
        else if (this.turn == 1){
            if (conflicts.size() == 0){
                this.ownedEven = this.lastMove;
                return 1;
            }
            else {
                return 1;
            }
        }
        else if (this.turn == 2){
            if (conflicts.size() == 0){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1){
                return this.ownedEven;
            }
            else{
                return this.chooseRandomExcluding(this.ownedOdd, conflicts, true);
            }
        }

        else if ((this.turn % 2) != 0) { //odd turns
            if (conflicts.size() == 0 && this.ownedEven == -1) {
                this.ownedHandle = this.lastMove;
                this.ownedEven = this.lastMove;
            }
            if (this.ownedOdd != -1) {
                //return this.chooseRandomExcluding(this.ownedEven, conflicts, false);
                return this.ownedOdd;
            } else {
                double percentage = (weightsOdd[this.nextLastMove]/totalWeight);
                double lowerWeight = weightsOdd[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)*Math.pow(1.1,this.turn/Math.sqrt(this.n))) * weightsOdd[this.nextLastMove];
                double tempTotalWeight = totalWeight - weightsOdd[this.nextLastMove];
                                                                                //Math.exp(-percentage/100); //* weights[this.nextLastMove]/totalWeight;
                for (int i=0; i<weightsOdd.length; ++i){
                    if (i!=this.nextLastMove){
                        //weightsOdd[i] += lowerWeight/(this.n-1);
                        weightsOdd[i] += lowerWeight * (weightsOdd[i]/tempTotalWeight);
                    }
                    else{
                        weightsOdd[i] -= lowerWeight;
                    }
                }
                System.out.println("weights: " + Arrays.toString(weightsOdd));
                return this.chooseRandomExcluding(this.ownedEven, conflicts, true);
            }
        } 
        else { //even turns
            if (conflicts.size() == 0 && this.ownedOdd == -1){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1) {
                return this.ownedEven;
            } else {
                double percentage = (weightsEven[this.nextLastMove]/totalWeight);
                double lowerWeight = weightsEven[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weightsEven[this.nextLastMove];
                                                                                //Math.exp(-percentage/100); //* weights[this.nextLastMove]/totalWeight;
                double tempTotalWeight = totalWeight - weightsEven[this.nextLastMove];
                for (int i=0; i<weightsEven.length; ++i){
                    if (i!=this.nextLastMove){
                        //weightsEven[i] += lowerWeight/(this.n-1);
                        weightsEven[i] += lowerWeight * (weightsEven[i]/tempTotalWeight);
                    }
                    else{
                        weightsEven[i] -= lowerWeight;
                    }
                }

                System.out.println("weights: " + Arrays.toString(weightsEven));
                int randomHandle;
                randomHandle = this.chooseRandomExcluding(this.ownedOdd,conflicts,true);

                return randomHandle;
            }
        }
    }
    
    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts, false);
    }
    
    public int chooseRandomExcluding(int excluding, List<Integer> conflicts, boolean enableProb) {
        boolean avoidLast = conflicts.size() == 0;

        int randomNum = this.weightedRandom(excluding, conflicts);
        return randomNum;
    }

    public int weightedRandom(int excluding, List<Integer> conflicts){
        int randomIndex = -1;
        double tempTotalWeight = totalWeight;
        double tempExcluding = 0;
        double tempConflict = 0;
        if (excluding>-1){
            if ((this.turn % 2) != 0){
                tempExcluding = weightsOdd[excluding];
                weightsOdd[excluding] = 0;
                //tempTotalWeight -= tempExcluding;
            }
            else{
                tempExcluding = weightsEven[excluding];
                weightsEven[excluding] = 0;
                //tempTotalWeight -= tempExcluding;
            }
            tempTotalWeight -= tempExcluding;
        }

        if (conflicts.size() != 0){
            if ((this.turn % 2) != 0){
                tempConflict = weightsOdd[this.lastMove];
                weightsOdd[this.lastMove] = 0;
                //tempTotalWeight -= tempConflict;
            }
            else{
                tempConflict = weightsEven[this.lastMove];
                weightsEven[this.lastMove] = 0;
                //tempTotalWeight -= tempConflict;
            }
            tempTotalWeight -= tempConflict;
        }

        double random = Math.random() * tempTotalWeight;
        
        for (int i=0; i<this.n; ++i){
            if ((this.turn % 2) != 0){
                random -= weightsOdd[i];
                if (random <= 0.0d){
                    randomIndex = i;
                    break;
                }
            }
            else{
                random -= weightsEven[i];
                if (random <= 0.0d){
                    randomIndex = i;
                    break;
                }
            }
        }
        if (tempExcluding!=0){
            if ((this.turn % 2) != 0){
                weightsOdd[excluding] = tempExcluding;
            }
            else{
                weightsEven[excluding] = tempExcluding;
            }
        }
        if (tempConflict!=0){
            if ((this.turn % 2) != 0){
                weightsOdd[this.lastMove] = tempConflict;
            }
            else{
                weightsEven[this.lastMove] = tempConflict;
            }
        }

        return randomIndex;
    }
        
}
