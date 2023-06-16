package src;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

enum Type {HYDROPHOB, HYDROPHIL}

enum Direction {
    RIGHT(1), 
    LEFT(2), 
    AHEAD(3), 
    STOP(4);

    private static final Random RANDOM = new Random(); 
    private final int value;

    Direction(int value) {
        this.value = value;
    }

    public static Direction getRandom() {
        return values()[RANDOM.nextInt(values().length - 1)];
    }

    public static Direction getRandom(Direction notIncluded) {
        if (notIncluded == STOP) {
            return STOP;
        }
        else {
            while(true) {
                int random = RANDOM.nextInt(values().length - 1);
                if (notIncluded.value != random) {
                    Direction a = values()[random];
                    return a;
                }
            }
        }
    }
}

public class Calculation {
    public int numOfOverlapped = 0; 
    public int numOfHHpairs = 0;

    private int[][] overlappedMap;
    private int[][] coordinates;

    public void resetData() {
        this.overlappedMap = null;
        this.coordinates = null;
        this.numOfOverlapped = 0;
        this.numOfHHpairs = 0;
    }

    public int factorial(int n) {
        if(n == 0) 
            return 0;
        else {
            int fact = 1;

            for (int i = 2; i <= n; i++) {
                fact = fact * i;
            }
            return fact;
        }
    }

    private void setOverlappedMap(int x, int y, int sequenceSize) {
        int currentValue = this.overlappedMap[x + sequenceSize - 1][y + sequenceSize - 1];
        this.overlappedMap[x + sequenceSize - 1][y + sequenceSize - 1] += 1;

        if (currentValue != 0) {
            this.numOfOverlapped = this.numOfOverlapped - factorial(currentValue - 1) + factorial(currentValue);
        }
    }

    private void setCoordinate(int currentX, int currentY, int previousX, int previousY, Type type) {
        int sequenceSize = (this.coordinates.length + 1) / 2;

        if (type == Type.HYDROPHOB) {
            this.coordinates[currentX + sequenceSize - 1][currentY + sequenceSize - 1] += 1;

            int neighbourX_1 = currentX + 1;
            int neighbourY_1 = currentY;
            int neighbourX_2 = currentX - 1;
            int neighbourY_2 = currentY;
            int neighbourX_3 = currentX;
            int neighbourY_3 = currentY + 1;
            int neighbourX_4 = currentX;
            int neighbourY_4 = currentY - 1;
            int neighbourValue;

            if (neighbourX_1 != previousX || neighbourY_1 != previousY) {
                neighbourValue = this.coordinates[neighbourX_1 + sequenceSize - 1][neighbourY_1 + sequenceSize - 1];
                if (neighbourValue >= 1) {
                    this.numOfHHpairs += neighbourValue;
                }
            }

            if (neighbourX_2 != previousX || neighbourY_2 != previousY) {
                neighbourValue = this.coordinates[neighbourX_2 + sequenceSize - 1][neighbourY_2 + sequenceSize - 1];
                if (neighbourValue >= 1) {
                    this.numOfHHpairs += neighbourValue;
                }
            }

            if (neighbourX_3 != previousX || neighbourY_3 != previousY) {
                neighbourValue = this.coordinates[neighbourX_3 + sequenceSize - 1][neighbourY_3 + sequenceSize - 1];
                if (neighbourValue >= 1) {
                    this.numOfHHpairs += neighbourValue;
                }
            }

            if (neighbourX_4 != previousX || neighbourY_4 != previousY) {
                neighbourValue = this.coordinates[neighbourX_4 + sequenceSize - 1][neighbourY_4 + sequenceSize - 1];
                if (neighbourValue >= 1) {
                    this.numOfHHpairs += neighbourValue;
                }
            }
        }
    }

    /** 
     * @return Map<String, Double>
     *  Keys:
     *      "fitness": fitness of the given conformation
     *      "hh pairs": Nunmber of H-H contacts
     *      "overlapped": Number of overlapped nodes
    */
    public Map<String, Double> getFitness(List<Direction> conformation, List<Type> sequence) throws Exception {
        if (conformation.size() != sequence.size()) {
            throw new Exception("Conformation length doesn't mantch sequence lenght");
        }
        
        // Initial the coordinate 
        this.coordinates = new int[sequence.size() * 2 - 1][sequence.size() * 2 - 1];
        if (sequence.get(0) == Type.HYDROPHOB) {
            this.coordinates[sequence.size() - 1][sequence.size() - 1] = 1;
        }

        // Initial the overlapped Map
        this.overlappedMap= new int[sequence.size() * 2 - 1][sequence.size() * 2 - 1];
        this.overlappedMap[sequence.size() - 1][sequence.size() - 1] = 1;

        double angle = 0;
        short currentX = 0;
        short currentY = 0;
        for(int i = 0; i < conformation.size() - 1; i++) {
            Direction dir = conformation.get(i);
            short nextX = 0;
            short nextY = 0;
            short x = 0;
            short y = 0;

            // Set up coordination
            if (dir != Direction.STOP) {
                if (dir == Direction.LEFT) {
                    x = -1;
                    y = 0;
                    nextX = (short) Math.round(currentX + (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle))));
                    nextY = (short) Math.round(currentY + (-x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle))));
                    angle -= 90;
                }
                else if (dir == Direction.RIGHT) {
                    x = 1;
                    y = 0;
                    nextX = (short) Math.round(currentX + (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle))));
                    nextY = (short) Math.round(currentY + -x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle)));
                    angle += 90;
                }
                else {
                    x = 0;
                    y = 1;
                    nextX = (short) Math.round(currentX + (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle))));
                    nextY = (short) Math.round(currentY + -x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle)));
                }

                this.setCoordinate(nextX, nextY, currentX, currentY, sequence.get(i+1));
                currentX = nextX; 
                currentY = nextY;
            }

            this.setOverlappedMap(nextX, nextY, sequence.size());
        }
        double fitness = (double) this.numOfHHpairs / (double) (this.numOfOverlapped + 1);

        Map<String, Double> result = new HashMap<String, Double>();
        result.put("fitness", fitness);
        result.put("hh pairs", (double) this.numOfHHpairs);
        result.put("overlapped", (double) this.numOfOverlapped);
        this.resetData();
        return result;
    }

    static List<Direction> convertToConformation(String str) throws Exception {
        List<Direction> conformation = new ArrayList<Direction>();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == 'R') {
                conformation.add(Direction.RIGHT);
            }
            else if (c == 'L') {
                conformation.add(Direction.LEFT);
            }
            else if (c == 'A') {
                conformation.add(Direction.AHEAD);
            }
            else if (c == 'S') {
                conformation.add(Direction.STOP);
                break;
            }
            else {
                throw new Exception("Invalid string sequence");
            }
        }
        return conformation;
    }

    static List<Type> convertToSequence(String str) throws Exception {
        List<Type> sequence = new ArrayList<Type>();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '1') {
                sequence.add(Type.HYDROPHOB);
            }
            else if (c == '0') {
                sequence.add(Type.HYDROPHIL);
            }
            else {
                throw new Exception("Invalid string sequence");
            }
        }
        return sequence;
    }

    static void writeCSV(List<Map<String, Double>> history) throws IOException {
        FileWriter writer = new FileWriter(new File("./log.csv"));
        StringBuilder sb = new StringBuilder();

        // write the header row
        StringJoiner header = new StringJoiner(",");
        for (String key : history.get(0).keySet()) {
            header.add(key);
        }
        sb.append(header).append("\n");

        // write the data rows
        for (Map<String, Double> map : history) {
            StringJoiner row = new StringJoiner(",");
            for (Double value : map.values()) {
                row.add(Double.toString(value));
            }
            sb.append(row).append("\n");
        }
        writer.write(sb.toString());
        writer.close();
    }

    static Map<String, String> readConfig(String filePath) throws FileNotFoundException {
        Map<String, String> configs = new HashMap<String, String>();

        File myObj = new File(filePath);
        Scanner myReader = new Scanner(myObj);

        while (myReader.hasNextLine()) {
            String[] line = myReader.nextLine().split("=");
            configs.put(line[0], line[1]);
        }

        myReader.close();
        return configs;
    }

    public static double dynamicControlling(List<Map<String, Double>> history, double currentMutationRate, double initialMutationRage) {
        // Get average fitness of the last 10 generations
        double averageFitness = 0;
        int size = history.size();
        int averageSize = 10;
        if (size >= averageSize) {
            for (int i = size - averageSize; i < size; i++) {
                averageFitness += history.get(i).get("avarage fitness");
            }
            averageFitness /= averageSize;
        }
        else {
            for (int i = 0; i < size; i++) {
                averageFitness += history.get(i).get("avarage fitness");
            }
            averageFitness /= size;
        }

        // if distance between average fitness and current fitness is less than 0.2, increase the mutation rate by 0.001
        // else decrease the mutation rate by 0.001
        // if the mutation rate is less than the initial mutation rate, set it to the initial mutation rate
        double distance = Math.abs(averageFitness - history.get(size - 1).get("avarage fitness"));
        double minMutationRate = initialMutationRage;
        if (distance < 0.2) {
            currentMutationRate += 0.001;
        }
        else {
            currentMutationRate -= 0.001;
        }
        if (currentMutationRate < minMutationRate) {
            currentMutationRate = minMutationRate;
        }
        return currentMutationRate;
    }

    public static Population geneticAlgorithm(Population population, 
                                              int numOfGen, 
                                              List<Map<String, Double>> history, 
                                              double initialMutationRate, 
                                              double crossOverRate, 
                                              int tournamentSize, 
                                              double tournamentParam,
                                              double requiredFitness) throws Exception {
        Calculation calculator = new Calculation();
        double globalBestFitness = 0;
        double mutationRate = initialMutationRate;
        List<Direction> globalBestCandidate = new ArrayList<>();
        Map<String, Double> dataOfBestCandidate = new HashMap<>();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numOfGen; i++) {
            // Update global best candidate (Conformation)
            if (globalBestFitness < population.bestFitness) {
                globalBestFitness = population.bestFitness;

                globalBestCandidate = new ArrayList<>();
                dataOfBestCandidate = new HashMap<>();
                globalBestCandidate.addAll(population.conformations.get(population.bestCandidateIndex));
                dataOfBestCandidate.putAll(calculator.getFitness(globalBestCandidate, population.sequence));
            }

            // Write history
            Map<String, Double> newDic = new LinkedHashMap<String, Double>();
            newDic.put("generation", (double) population.generation);
            newDic.put("avarage fitness", Math.round(population.evaluate() * 100)/100.0);
            newDic.put("local best fitness", Math.round(population.bestFitness * 100)/100.0);
            newDic.put("global best fitness", Math.round(globalBestFitness * 100)/100.0);
            newDic.put("h-h pairs of best candidate", dataOfBestCandidate.get("hh pairs"));
            newDic.put("overlapped pairs of best candidate", dataOfBestCandidate.get("overlapped"));
            newDic.put("mutation rate", mutationRate);
            history.add(newDic);

            if (globalBestFitness == requiredFitness) {
                break;
            }

            // crossover and mutation
            mutationRate = dynamicControlling(history, mutationRate, initialMutationRate);
            population.mutation(mutationRate);
            population.crossOver(crossOverRate);
            //population.proportionalSelection();
            population.tournamentSelection(tournamentSize, tournamentParam);
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution time: " + executionTime / 1000 + " seconds");

        Graphic.generateImage(globalBestCandidate, population.sequence, "GlobalBestCandidate.png");
        return population;
    }
}
