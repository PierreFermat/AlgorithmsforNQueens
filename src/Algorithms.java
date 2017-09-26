import java.util.ArrayList;
import java.util.Random;

public class Algorithms
{
    private static final int InitialNumber = 75;                    // Population size at start.
    private static final int ChessBoardSize = 11;                    // chess board width.
    private static final int MaxStep = 2000;                  // Arbitrary number of test cycles.
    private static final double MatingProbability = 0.7;        // Probability of two chromosomes mating. Range: 0.0 < MATING_PROBABILITY < 1.0
    private static final double MutationProbability = 0.001;           // Mutation Rate. Range: 0.0 < MUTATION_RATE < 1.0
    private static final int MinSelection = 10;                    // Minimum parents allowed for selection.
    private static final int MaxSelection = 50;                    // Maximum parents allowed for selection. Range: MIN_SELECT < MAX_SELECT < START_SIZE
    private static final int OFFSPRING_PER_GENERATION = 20;      // New offspring created per generation. Range: 0 < OFFSPRING_PER_GENERATION < MAX_SELECT.
    private static final int MinShuffles = 8;               // For randomizing starting chromosomes
    private static final int MaxShuffles = 20;


    private static int step = 0;
    private static int childCount = 0;
    private static int nextMutation = 0;                         // For scheduling mutations.
    private static int mutations = 0;

    private static ArrayList<Chromosome> population = new ArrayList<Chromosome>();

    private static void algorithm()
    {
        int popSize = 0;
        Chromosome thisChromo = null;
        boolean done = false;

        initializeChromosomes();
        mutations = 0;
        nextMutation = getRandomNumber(0, (int)Math.round(1.0 / MutationProbability));

        while(!done)
        {
            popSize = population.size();
            for(int i = 0; i < popSize; i++)
            {
                thisChromo = population.get(i);
                if((thisChromo.conflicts() == 0) || step == MaxStep){
                    done = true;
                }
            }

            getFitness();

            rouletteSelection();

            mating();

            prepareNextStep();

            step++;
            System.out.println("Epoch: " + step);
        }

        System.out.println("done.");

        if(step != MaxStep){
            popSize = population.size();
            for(int i = 0; i < popSize; i++)
            {
                thisChromo = population.get(i);
                if(thisChromo.conflicts() == 0){
                    printbestSolution(thisChromo);
                }
            }
        }
        System.out.println("Completed " + step + " steps.");
        System.out.println("Encountered " + mutations + " mutations in " + childCount + " children.");
        return;
    }

    private static void getFitness()
    {
        int popSize = population.size();
        Chromosome thisChromo = null;
        double bestScore = 0;
        double worstScore = 0;

        worstScore = population.get(maximum()).conflicts();

        bestScore = worstScore - population.get(minimum()).conflicts();

        for(int i = 0; i < popSize; i++)
        {
            thisChromo = population.get(i);
            thisChromo.fitness((worstScore - thisChromo.conflicts()) * 100.0 / bestScore);
        }

        return;
    }


    private static void rouletteSelection()
    {
        int j = 0;
        int popSize = population.size();
        double genTotal = 0.0;
        double selTotal = 0.0;
        int maximumToSelect = getRandomNumber(MinSelection, MaxSelection);
        double rouletteSpin = 0.0;
        Chromosome thisChromo = null;
        Chromosome thatChromo = null;
        boolean done = false;

        for(int i = 0; i < popSize; i++)
        {
            thisChromo = population.get(i);
            genTotal += thisChromo.fitness();
        }

        genTotal *= 0.01;

        for(int i = 0; i < popSize; i++)
        {
            thisChromo = population.get(i);
            thisChromo.selectionProbability(thisChromo.fitness() / genTotal);
        }

        for(int i = 0; i < maximumToSelect; i++)
        {
            rouletteSpin = getRandomNumber(0, 99);
            j = 0;
            selTotal = 0;
            done = false;
            while(!done)
            {
                thisChromo = population.get(j);
                selTotal += thisChromo.selectionProbability();
                if(selTotal >= rouletteSpin){
                    if(j == 0){
                        thatChromo = population.get(j);
                    }else if(j >= popSize - 1){
                        thatChromo = population.get(popSize - 1);
                    }else{
                        thatChromo = population.get(j - 1);
                    }
                    thatChromo.selected(true);
                    done = true;
                }else{
                    j++;
                }
            }
        }
        return;
    }

    private static void mating()
    {
        int getRand = 0;
        int parentA = 0;
        int parentB = 0;
        int newIndex1 = 0;
        int newIndex2 = 0;
        Chromosome newChromo1 = null;
        Chromosome newChromo2 = null;

        for(int i = 0; i < OFFSPRING_PER_GENERATION; i++)
        {
            parentA = chooseParent();
            getRand = getRandomNumber(0, 100);
            if(getRand <= MatingProbability * 100){
                parentB = chooseParent(parentA);
                newChromo1 = new Chromosome();
                newChromo2 = new Chromosome();
                population.add(newChromo1);
                newIndex1 = population.indexOf(newChromo1);
                population.add(newChromo2);
                newIndex2 = population.indexOf(newChromo2);

                partiallyMappedCrossover(parentA, parentB, newIndex1, newIndex2);

                if(childCount - 1 == nextMutation){
                    exchangeMutation(newIndex1, 1);
                }else if(childCount == nextMutation){
                    exchangeMutation(newIndex2, 1);
                }

                population.get(newIndex1).computeConflicts();
                population.get(newIndex2).computeConflicts();

                childCount += 2;

                if(childCount % (int)Math.round(1.0 / MutationProbability) == 0){
                    nextMutation = childCount + getRandomNumber(0, (int)Math.round(1.0 / MutationProbability));
                }
            }
        } // i
        return;
    }

    private static void partiallyMappedCrossover(int chromA, int chromB, int child1, int child2)
    {
        int j = 0;
        int item1 = 0;
        int item2 = 0;
        int pos1 = 0;
        int pos2 = 0;
        Chromosome thisChromo = population.get(chromA);
        Chromosome thatChromo = population.get(chromB);
        Chromosome newChromo1 = population.get(child1);
        Chromosome newChromo2 = population.get(child2);
        int crossPoint1 = getRandomNumber(0, ChessBoardSize - 1);
        int crossPoint2 = getExclusiveRandomNumber(ChessBoardSize - 1, crossPoint1);

        if(crossPoint2 < crossPoint1){
            j = crossPoint1;
            crossPoint1 = crossPoint2;
            crossPoint2 = j;
        }

        for(int i = 0; i < ChessBoardSize; i++)
        {
            newChromo1.data(i, thisChromo.data(i));
            newChromo2.data(i, thatChromo.data(i));
        }

        for(int i = crossPoint1; i <= crossPoint2; i++)
        {
            item1 = thisChromo.data(i);
            item2 = thatChromo.data(i);

            for(j = 0; j < ChessBoardSize; j++)
            {
                if(newChromo1.data(j) == item1){
                    pos1 = j;
                }else if(newChromo1.data(j) == item2){
                    pos2 = j;
                }
            } // j

            if(item1 != item2){
                newChromo1.data(pos1, item2);
                newChromo1.data(pos2, item1);
            }

            for(j = 0; j < ChessBoardSize; j++)
            {
                if(newChromo2.data(j) == item2){
                    pos1 = j;
                }else if(newChromo2.data(j) == item1){
                    pos2 = j;
                }
            } // j

            if(item1 != item2){
                newChromo2.data(pos1, item1);
                newChromo2.data(pos2, item2);
            }

        } // i
        return;
    }
    private static void exchangeMutation(final int index, final int exchanges)
    {
        int i =0;
        int tempData = 0;
        Chromosome thisChromo = null;
        int gene1 = 0;
        int gene2 = 0;
        boolean done = false;

        thisChromo = population.get(index);

        while(!done)
        {
            gene1 = getRandomNumber(0, ChessBoardSize - 1);
            gene2 = getExclusiveRandomNumber(ChessBoardSize - 1, gene1);

            tempData = thisChromo.data(gene1);
            thisChromo.data(gene1, thisChromo.data(gene2));
            thisChromo.data(gene2, tempData);

            if(i == exchanges){
                done = true;
            }
            i++;
        }
        mutations++;
        return;
    }

    private static int chooseParent()
    {
        int parent = 0;
        Chromosome thisChromo = null;
        boolean done = false;

        while(!done)
        {
            parent = getRandomNumber(0, population.size() - 1);
            thisChromo = population.get(parent);
            if(thisChromo.selected() == true){
                done = true;
            }
        }

        return parent;
    }

    private static int chooseParent(final int parentA)
    {
        int parent = 0;
        Chromosome thisChromo = null;
        boolean done = false;

        while(!done)
        {
            parent = getRandomNumber(0, population.size() - 1);
            if(parent != parentA){
                thisChromo = population.get(parent);
                if(thisChromo.selected() == true){
                    done = true;
                }
            }
        }

        return parent;
    }

    private static void prepareNextStep()
    {
        int popSize = 0;
        Chromosome thisChromo = null;

        popSize = population.size();
        for(int i = 0; i < popSize; i++)
        {
            thisChromo = population.get(i);
            thisChromo.selected(false);
        }
        return;
    }

    private static void printbestSolution(Chromosome bestSolution)
    {
        String board[][] = new String[ChessBoardSize][ChessBoardSize];

        // Clear the board.
        for(int x = 0; x < ChessBoardSize; x++)
        {
            for(int y = 0; y < ChessBoardSize; y++)
            {
                board[x][y] = "";
            }
        }

        for(int x = 0; x < ChessBoardSize; x++)
        {
            board[x][bestSolution.data(x)] = "Q";
        }

        // Display the board.
        System.out.println("Board:");
        for(int y = 0; y < ChessBoardSize; y++)
        {
            for(int x = 0; x < ChessBoardSize; x++)
            {
                if(board[x][y] == "Q"){
                    System.out.print("Q ");
                }else{
                    System.out.print(". ");
                }
            }
            System.out.print("\n");
        }

        return;
    }

    private static int getRandomNumber(final int low, final int high)
    {
        return (int)Math.round((high - low) * new Random().nextDouble() + low);
    }

    private static int getExclusiveRandomNumber(final int high, final int except)
    {
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
                if(i != winner){
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

    private static void initializeChromosomes()
    {
        int shuffles = 0;
        Chromosome newChromo = null;
        int chromoIndex = 0;

        for(int i = 0; i < InitialNumber; i++)
        {
            newChromo = new Chromosome();
            population.add(newChromo);
            chromoIndex = population.indexOf(newChromo);

            shuffles = getRandomNumber(MinShuffles, MaxShuffles);

            exchangeMutation(chromoIndex, shuffles);

            population.get(chromoIndex).computeConflicts();

        }
        return;
    }

    private static class Chromosome
    {
        private int mData[] = new int[ChessBoardSize];
        private double mFitness = 0.0;
        private boolean mSelected = false;
        private double mSelectionProbability = 0.0;
        private int mConflicts = 0;

        public Chromosome()
        {
            for(int i = 0; i < ChessBoardSize; i++)
            {
                this.mData[i] = i;
            }
            return;
        }

        public void computeConflicts()
        {
            int x = 0;
            int y = 0;
            int tempx = 0;
            int tempy = 0;
            String board[][] = new String[ChessBoardSize][ChessBoardSize];
            int conflicts = 0;
            int dx[] = new int[] {-1, 1, -1, 1};
            int dy[] = new int[] {-1, 1, 1, -1};
            boolean done = false;

            for(int i = 0; i < ChessBoardSize; i++)
            {
                for(int j = 0; j < ChessBoardSize; j++)
                {
                    board[i][j] = "";
                }
            }

            for(int i = 0; i < ChessBoardSize; i++)
            {
                board[i][this.mData[i]] = "Q";
            }

            for(int i = 0; i < ChessBoardSize; i++)
            {
                x = i;
                y = this.mData[i];

                for(int j = 0; j <= 3; j++)
                {
                    tempx = x;
                    tempy = y;
                    done = false;
                    while(!done)
                    {
                        tempx += dx[j];
                        tempy += dy[j];
                        if((tempx < 0 || tempx >= ChessBoardSize) || (tempy < 0 || tempy >= ChessBoardSize)){
                            done = true;
                        }else{
                            if(board[tempx][tempy].compareToIgnoreCase("Q") == 0){
                                conflicts++;
                            }
                        }
                    }
                }
            }

            this.mConflicts = conflicts;
        }

        public int conflicts()
        {
            return this.mConflicts;
        }

        public double selectionProbability()
        {
            return mSelectionProbability;
        }

        public void selectionProbability(final double SelProb)
        {
            mSelectionProbability = SelProb;
            return;
        }

        public boolean selected()
        {
            return mSelected;
        }

        public void selected(final boolean sValue)
        {
            mSelected = sValue;
            return;
        }

        public double fitness()
        {
            return mFitness;
        }

        public void fitness(final double score)
        {
            mFitness = score;
            return;
        }

        public int data(final int index)
        {
            return mData[index];
        }

        public void data(final int index, final int value)
        {
            mData[index] = value;
            return;
        }
    } // Chromosome

    public static void main(String[] args)
    {
        algorithm();
        return;
    }

}
