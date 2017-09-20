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
    private double tieBreakerValue = 0;
	
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
        //tieBreakerValue = 0.9 + (0.02-0.9)*(0.33-1.0/this.n)/(0.33-0.005);
        tieBreakerValue = 0.9 + (0.1-0.9)*(Math.log(0.33)-Math.log(1.0/n))/(Math.log(0.33)-Math.log(0.0005));
        System.out.println("tiebreaker:: " + tieBreakerValue);
        double temptemp = 1.0/this.n;
        /*System.out.println("1/n: " + temptemp);
        System.out.println("n: " + this.n);
        System.exit(1);*/
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
        } 
        else if (this.turn == 1){
            if (conflicts.size() == 0){
                this.ownedEven = this.lastMove;
            }
            return 1;
        }
        
        else if (this.turn == 2){
            if (conflicts.size() == 0){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1){
                return this.ownedEven;
            }
            else{
                return this.chooseRandomExcluding(this.ownedOdd, conflicts);
            }
        }

        else if ((this.turn % 2) != 0) { //odd turns
            if (conflicts.size() == 0 && this.ownedEven == -1) {
                //this.ownedHandle = this.lastMove;
                this.ownedEven = this.lastMove;
            }
            if (this.ownedOdd != -1) {
                return this.ownedOdd;
            } else {
                System.out.println("ownedOdd: " + this.ownedOdd);
                double percentage = (weightsOdd[this.nextLastMove]/totalWeight);
                //double lowerWeight = weightsOdd[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)*Math.pow((1.1*(0.5*(this.turn-1)/(this.n*this.n-1))),this.turn/Math.sqrt(this.n))) * weightsOdd[this.nextLastMove];
                ///double percentage = 0.5;
                double lowerWeight = weightsOdd[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weightsOdd[this.nextLastMove];
                ///double lowerWeight = weightsOdd[this.nextLastMove] - percentage*weightsOdd[this.nextLastMove];
                double tempTotalWeight = totalWeight - weightsOdd[this.nextLastMove];
                double adjustLimit = weightsOdd[this.nextLastMove] / 100;
                System.out.println("lowerWeightOdd: " + lowerWeight);
                /*if (this.ownedEven != -1 && weightsOdd[this.ownedEven]/totalWeight > 0.9){
                    System.out.println("lalala odd" + this.ownedEven);
                    return this.ownedEven;
                }*/
                if (true){//(adjustLimit > 0.05){
                    for (int i=0; i<weightsOdd.length; ++i){
                        if (i!=this.nextLastMove){
                            //weightsOdd[i] += lowerWeight/(this.n-1);
                            weightsOdd[i] += lowerWeight * (weightsOdd[i]/tempTotalWeight);
                        }
                        else{
                            weightsOdd[i] -= lowerWeight;
                        }
                    }
                }
                System.out.println("weightsOdd: " + Arrays.toString(weightsOdd));
                System.out.println("weightsEven: " + Arrays.toString(weightsEven) + "\n");
                return this.chooseRandomExcluding(this.ownedEven, conflicts);
            }
        } 
        else { //even turns
            if (conflicts.size() == 0 && this.ownedOdd == -1){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1) {
                return this.ownedEven;
            } else {
                System.out.println("ownedEven: " + this.ownedEven);
                double percentage = (weightsEven[this.nextLastMove]/totalWeight);
                ///double percentage = 0.5;
                //double lowerWeight = weightsEven[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)*Math.pow((1.1*(0.5*(this.turn-1)/(this.n*this.n-1))),this.turn/Math.sqrt(this.n))) * weightsEven[this.nextLastMove];
                double lowerWeight = weightsEven[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weightsEven[this.nextLastMove];
                ///double lowerWeight = weightsEven[this.nextLastMove] - 0.5*weightsEven[this.nextLastMove];
                double tempTotalWeight = totalWeight - weightsEven[this.nextLastMove];
                double adjustLimit = weightsEven[this.nextLastMove] / 100;
                System.out.println("lowerWeightEven: " + lowerWeight);
                /*if (this.ownedOdd != -1 && weightsEven[this.ownedOdd]/totalWeight > 0.9){
                    return this.ownedOdd;
                }*/
                if (true){;//(adjustLimit > 0.05){
                    for (int i=0; i<weightsEven.length; ++i){
                        if (i!=this.nextLastMove){
                            //weightsEven[i] += lowerWeight/(this.n-1);
                            weightsEven[i] += lowerWeight * (weightsEven[i]/tempTotalWeight);
                        }
                        else{
                            weightsEven[i] -= lowerWeight;
                        }
                    }
                }

                System.out.println("weightsEven: " + Arrays.toString(weightsEven));
                System.out.println("weightsOdd: " + Arrays.toString(weightsOdd) + "\n");
                int randomHandle;
                randomHandle = this.chooseRandomExcluding(this.ownedOdd,conflicts);

                return randomHandle;
            }
        }
    }
    
    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts);
    }
    
    public int chooseRandomExcluding(int excluding, List<Integer> conflicts) {
        boolean avoidLast = conflicts.size() == 0;

        int randomNum = this.weightedRandom(excluding, conflicts);
        return randomNum;
    }

    public int weightedRandom(int excluding, List<Integer> conflicts){
        System.out.println("tiebreaker: " + tieBreakerValue);
        int randomIndex = -1;
        double tempTotalWeight = totalWeight;
        double tempExcluding = 0;
        double tempConflict = 0;
        if (excluding>-1){
            System.out.println("excluding :" + excluding);
            if ((this.turn % 2) != 0){
                if (weightsOdd[excluding]/totalWeight > tieBreakerValue && conflicts.size()==0 && this.turn>this.n*1.6){
                    System.out.println("weightsOddBig: " + weightsOdd[excluding]);
                    return excluding;
                }
                tempExcluding = weightsOdd[excluding];
                weightsOdd[excluding] = 0;
                //tempTotalWeight -= tempExcluding;
            }
            else{
                if (weightsEven[excluding]/totalWeight > tieBreakerValue && conflicts.size()==0 && this.turn>this.n*1.6){
                    System.out.println("weightsEvenBig: " + weightsEven[excluding]);
                    return excluding;
                }
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
