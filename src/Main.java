package src;
import java.util.*;

/**
     * X: HYDROPHOB
     * O: HYDROPPHIL
     * 
     *      O -- X
     *      |
     *      O    X -- O 
     *      |    |    |
     *      O -- X    X
     * 
     * sequence: 10110001
     * conformation: ALLRRARS
     * 
     * 
     *            X -- O 
     *            |    |
     *      O -- X,X   X
     *      |    |
     *      O -- O
     *  sequence: 10110001
     *  conformation: ALLRLLLS
     * 
     * 
     *      O -- X -- X    X
     *      |         |    |
     *      X    X -- O    O
     *      |              |
     *      O -- X -- O -- X 
     * 
     * sequence: 101101010101
     * conformation: RLLALALAALAS
     * 
     * 
     *                     X -- O
     *                          | 
     *      O -- O         X -- O
     *      |    |         |
     *      X    X -- O -- O 
     *      |
     *      O -- O
     * 
     * sequence: 001001001001
     * conformation: LRARRLALRLLS
     *
     * 
     *      O -- O
     *      |    |
     *      X    X -- O    O -- O 
     *      |         |    |    |
     *      X -- X    X -- X    X
     *           |              |
     *      O -- X    X    X -- O
     *      |         |
     *      O -- X -- O 
     *  
     * fitnest = -9  
     * sequence: 10100111100101100101
     * conformation: ARARRLLRARRLRLLRRARS
     * conformation Overlapped: ARARRLLRARRARLLRRARS
     */


public class Main {
    static String sequence1 = "10110001";
    static String sequence2 = "10110001";
    static String sequence3 = "101101010101";
    static String sequence4 = "001001001001";
    static String sequence5 = "10100111100101100101";
    static String sequence6 = "1111000011111111111000000111111111111000111111111111000111111111111000100110011000101";

    static String conformation1 = "ALLRRARS";
    static String conformation2 = "ALLRLLLS";
    static String conformation3 = "RLLALALAALAS";
    static String conformation4 = "LRARRLALRLLS";
    static String conformation5 = "ARARRLLRARRLRLLRRARS";

    
    
    public static void main(String[] args) throws Exception {    
        try {
            // Read config file
            Map<String, String> configs = Calculation.readConfig("./config.env");
            double mutationRate = Double.parseDouble(configs.get("mutation_rate"));
            double crossoverRate = Double.parseDouble(configs.get("crossover_rate"));
            int populationSize = Integer.parseInt(configs.get("population_size"));
            int generationSize = Integer.parseInt(configs.get("generation_size"));
            int tournamentSize = Integer.parseInt(configs.get("tournament_size"));
            double tournamentParam = Double.parseDouble(configs.get("tournament_parameter"));
            double requiredFitness = Double.parseDouble(configs.get("required_fitness"));

            List<Type> sequence = Calculation.convertToSequence(Examples.SEQ50);
            Population population = new Population(sequence, populationSize);
            List<Map<String, Double>> history = new ArrayList<>();

            // Start genetic algorithm
            population = Calculation.geneticAlgorithm(population, 
                                                      generationSize, 
                                                      history, 
                                                      mutationRate, 
                                                      crossoverRate, 
                                                      tournamentSize,
                                                      tournamentParam,
                                                      requiredFitness);
            Calculation.writeCSV(history);
            population.generatBestImage();

            System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}