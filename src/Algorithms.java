import com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Algorithms {
    private static boolean done;
    private static int step;
    private final static int NumberOfQueen = 8;
    private static ArrayList<Chromosome> population = new ArrayList<>();
    private final static int InitialNumber = 100;
    private final static int MaxShuffles = 20;
    private final static int MinShuffles = 8;
    private static int mutationCount;
    private final static int MaxStep = 1000;

    private static void algorithms(){
        int popSize = 0;
        Chromosome thisChromo = null;
        initializeChromosomes();
        mutationCount = 0;
        while(!done){
            popSize = population.size();
            for(int i = 0; i < popSize; i++)
            {
                thisChromo = population.get(i);
                if((thisChromo.conflicts() == 0) || step == MaxStep){
                    done = true;
                }
            }
            getFitness();
            SelectionParents();
            Mating();
            PrepareNextStep();
            step ++;
            System.out.println("Step Counter : " + step);

        }
        System.out.println("Algorithms Done");
    }

    private static void initializeChromosomes() {
        Chromosome newChromoso =null;
        int shuffles = 0;
        int chromoIndex = 0;
        for(int i = 0; i < InitialNumber; i++){
            newChromoso = new Chromosome();
            population.add(newChromoso);
            chromoIndex = population.indexOf(newChromoso);
            shuffles = getRandomNumber(MinShuffles,MaxShuffles);
            exchangeMutation(chromoIndex, shuffles);
            population.get(chromoIndex).ConflictsChecking();

        }
        return;


    }

    private static void exchangeMutation(int index, int exchanges) {
        int i =0;
        int tempData = 0;
        Chromosome thisChromo = null;
        int gene1 = 0;
        int gene2 = 0;
        boolean done = false;
        while(!done){
            gene1 = getRandomNumber(0, NumberOfQueen - 1);
            gene2 = getExclusiveRandomNumber(NumberOfQueen - 1, gene1);
            tempData = thisChromo.data(gene1);
            thisChromo.data(gene1,thisChromo.data(gene2));
            thisChromo.data(gene2,tempData);
            if(i == exchanges){
                done = true;
            }
            i++;
        }
        mutationCount ++;
        return;
    }

    private static void getFitness()
    {
        // Lowest errors = 100%, Highest errors = 0%
        int popSize = population.size();
        Chromosome thisChromo = null;
        double MinConflicts = 0;
        double MaxConflicts = 0;

        // The worst score would be the one with the highest energy, best would be lowest.
        MaxConflicts = population.get(maximum()).conflicts();

        // Convert to a weighted percentage.
        MinConflicts = population.get(minimum()).conflicts();

        for(int i = 0; i < popSize; i++)
        {
            thisChromo = population.get(i);
            thisChromo.fitness((MaxConflicts - thisChromo.conflicts()) * 100.0 /(MaxConflicts - MinConflicts));
        }
        return;
    }

    private static void SelectionParents() {

    }

    private static void Mating() {

    }

    private static void PrepareNextStep() {

    }

    public static void main(String[] args)
    {
        algorithms();
        return;
    }

    private static class Chromosome {
        private int Data[] = new int[NumberOfQueen];
        String ChessBoard[][] = new String[NumberOfQueen][NumberOfQueen];
        private int Conflicts;
        private double Fitness;

        public Chromosome(){
            for(int i=0; i < NumberOfQueen; i++){
                this.Data[i] = i;
            }
        }
        public void ConflictsChecking(){
            int x = 0;
            int y = 0;
            int tempx = 0;
            int tempy = 0;
            int conflicts = 0;
            int dx[] = new int[] {-1, 1, -1, 1};
            int dy[] = new int[] {-1, 1, 1, -1};
            //clear Chess Board
            for(int i = 0; i < NumberOfQueen; i++){
                for(int j = 0; j < NumberOfQueen; j++){
                    ChessBoard[i][j] = "";
                }
            }

            //Insert Queens
            for(int i = 0; i < NumberOfQueen; i++){
                ChessBoard[i][Data[i]] = "Q";
            }

            // Walk through each of the Queens and compute the number of conflicts.
            for(int i = 0; i < NumberOfQueen; i++)
            {
                x = i;
                y = this.Data[i];

                // Check diagonals.
                for(int j = 0; j <= 3; j++)
                {
                    tempx = x;
                    tempy = y;
                    done = false;
                    while(!done)
                    {
                        tempx += dx[j];
                        tempy += dy[j];
                        if((tempx < 0 || tempx >= NumberOfQueen) || (tempy < 0 || tempy >= NumberOfQueen)){
                            done = true;
                        }else{
                            if(ChessBoard[tempx][tempy].compareToIgnoreCase("Q") == 0){
                                conflicts++;
                            }
                        }
                    }
                }
            }
            this.Conflicts = conflicts;
        }

        public int data(final int index)
        {
            return Data[index];
        }

        public void data(final int index, final int value)
        {
            Data[index] = value;
            return;
        }

        public int conflicts() {
            return this.Conflicts;
        }
        public void fitness(final double score)
        {
            this.Fitness = score;
            return;
        }
    }
    private static int getRandomNumber(final int low, final int high) {
        return (int)Math.round((high - low) * new Random().nextDouble() + low);
    }
    private static int getExclusiveRandomNumber(final int high, final int except) {
        boolean done = false;
        int getRand = 0;

        while(!done)
        {
            getRand = new Random().nextInt(high);
            if(getRand != except){
                done = true;
            }
        }

        return getRand;
    }

    private static int getRandomNumber(int low, int high, int[] except) {
        boolean done = false;
        int getRand = 0;

        if(high != low){
            while(!done)
            {
                done = true;
                getRand = (int)Math.round((high - low) * new Random().nextDouble() + low);
                for(int i = 0; i < except.length; i++)
                {
                    if(getRand == except[i]){
                        done = false;
                    }
                } // i
            }
            return getRand;
        }else{
            return high;
        }
    }
    private static int minimum()
    {
        // Returns an array index.
        int popSize = 0;
        Chromosome thisChromo = null;
        Chromosome thatChromo = null;
        int winner = 0;
        boolean foundNewWinner = false;
        boolean done = false;

        while(!done)
        {
            foundNewWinner = false;
            popSize = population.size();
            for(int i = 0; i < popSize; i++)
            {
                if(i != winner){             // Avoid self-comparison.
                    thisChromo = population.get(i);
                    thatChromo = population.get(winner);
                    if(thisChromo.conflicts() < thatChromo.conflicts()){
                        winner = i;
                        foundNewWinner = true;
                    }
                }
            }
            if(foundNewWinner == false){
                done = true;
            }
        }
        return winner;
    }

    private static int maximum()
    {
        // Returns an array index.
        int popSize = 0;
        Chromosome thisChromo = null;
        Chromosome thatChromo = null;
        int winner = 0;
        boolean foundNewWinner = false;
        boolean done = false;

        while(!done)
        {
            foundNewWinner = false;
            popSize = population.size();
            for(int i = 0; i < popSize; i++)
            {
                if(i != winner){             // Avoid self-comparison.
                    thisChromo = population.get(i);
                    thatChromo = population.get(winner);
                    if(thisChromo.conflicts() > thatChromo.conflicts()){
                        winner = i;
                        foundNewWinner = true;
                    }
                }
            }
            if(foundNewWinner == false){
                done = true;
            }
        }
        return winner;
    }
        
}

