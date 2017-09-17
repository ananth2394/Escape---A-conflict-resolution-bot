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
    private double[] weights;
    private double totalWeight = 0.0;
	
    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        this.conflictsPerRound = new ArrayList<List<Integer>>();
        this.moves = new ArrayList<Integer>();
        weights = new double[n];
        for (int i=0; i<n; ++i){
            totalWeight += 100;
            weights[i] = 100;
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
        else if ((this.turn % 2) != 0) {
            if (conflicts.size() == 0) {
                this.ownedHandle = this.lastMove;
            }
            if (this.ownedHandle != -1) {
                return this.chooseRandomExcluding(this.ownedHandle, conflicts, false);
            } else {
                return this.chooseRandom(conflicts);
            }
        } 
        else {
            if (this.ownedHandle != -1) {
                return this.ownedHandle;
            } else {
                //double percentage = 0.5;
                double percentage = (weights[this.nextLastMove]/totalWeight);
                //System.out.println("percentage: " + percentage);

                //double lowerWeight = weights[this.nextLastMove] - (percentage*weights[this.nextLastMove]);
                //double lowerWeight = weights[this.nextLastMove] - Math.exp(-percentage*(Math.log(this.n)*Math.log(this.n)/1.1)) * weights[this.nextLastMove];
                double lowerWeight = weights[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weights[this.nextLastMove];
                                                                                //Math.exp(-percentage/100); //* weights[this.nextLastMove]/totalWeight;
                //System.out.println("lower weight: " + lowerWeight);
                for (int i=0; i<weights.length; ++i){
                    if (i!=this.nextLastMove){
                        weights[i] += lowerWeight/(this.n-1);
                        //System.out.println("A");
                    }
                    else{
                        weights[i] -= lowerWeight;
                        //System.out.println("B");
                    }
                }

                System.out.println("weights: " + Arrays.toString(weights));
                int randomHandle;
                if (this.rand.nextInt(this.n)!=0){
                    randomHandle = this.chooseRandomExcluding(this.nextLastMove, conflicts, true);
                }
                else{
                    randomHandle = this.chooseRandomExcluding(-1, conflicts, true);
                }

                //return this.chooseRandomExcluding(this.nextLastMove, conflicts, true);
                return randomHandle;
            }
        }
    }
    
    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts, false);
    }
    
    public int chooseRandomExcluding(int excluding, List<Integer> conflicts, boolean enableProb) {
        boolean avoidLast = conflicts.size() == 0;
        /*
        List<Integer> choices = new ArrayList<Integer>();
        for (int i = 0; i < this.n; i++) {
            choices.add(i);
        }
        if (conflicts.size() != 0) {
            choices.remove(new Integer(this.lastMove));
        }
        if (enableProb){
            int temp = this.rand.nextInt(this.n);
            //System.out.println("temp" + temp);
            if (temp!=0){
                choices.remove(new Integer(excluding));
                //System.out.println(choices);
            }
        }
        else
            choices.remove(new Integer(excluding));
        int index = this.rand.nextInt(choices.size());
        return choices.get(index);
        */

        int randomNum = this.weightedRandom(excluding, conflicts);
        /*if (this.rand.nextInt(2)!=0){
            randomNum = this.weightedRandom(excluding, conflicts);
        }
        else{
            randomNum = this.weightedRandom(-1, conflicts);
        }*/

        /*if (conflicts.size() != 0){
            while (randomNum==this.lastMove || randomNum==excluding){
                randomNum = this.weightedRandom(0);
            }
        }*/
        return randomNum;
    }

    public int weightedRandom(int excluding, List<Integer> conflicts){
        int randomIndex = -1;
        //System.out.println("weights original: " + Arrays.toString(weights));
        double tempTotalWeight = totalWeight;
        //System.out.println("tempTotalWeight original: " + tempTotalWeight);
        double tempExcluding = 0;
        double tempConflict = 0;
        if (excluding>-1){
            tempExcluding = weights[excluding];
            weights[excluding] = 0;
            tempTotalWeight -= tempExcluding;
        }

        if (conflicts.size() != 0){
            tempConflict = weights[this.lastMove];
            weights[this.lastMove] = 0;
            tempTotalWeight -= tempConflict;
        }

        double random = Math.random() * tempTotalWeight;
        //System.out.println("random: " + random);
        //System.out.println("tempTotalWeight: " + tempTotalWeight);
        //System.out.println("weights: " + Arrays.toString(weights));
        
        for (int i=0; i<weights.length; ++i){
            random -= weights[i];
            //System.out.println("[" + i + "]: " + random);
            if (random <= 0.0d){
                randomIndex = i;
                break;
            }
            else if (i==weights.length-1){
                randomIndex = i;
            }
        }
        //System.out.println("hihihi" + randomIndex);
        if (tempExcluding!=0){
            weights[excluding] = tempExcluding;
        }
        if (tempConflict!=0){
            weights[this.lastMove] = tempConflict;
        }

        //System.out.println("randomIndex: " + randomIndex);
        return randomIndex;
    }
        
}
