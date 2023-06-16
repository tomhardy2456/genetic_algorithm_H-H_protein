package src;
import java.net.CacheRequest;
import java.util.*;

public class Population{
    List<List<Direction>> conformations = new ArrayList<List<Direction>>();
    List<Type> sequence;
    int generation = 1;

    /**
     * Data is a list of dictionary storing fitness, h-h pairs and overlapped pairs of each conformation
     * Keywords: 
     *      data["fitness"]: fitness of a conformation with the corresponding index
     *      data["hh pairs"]: Number of H-H pairs of the corresponding conformation 
     *      data["overlapped"]: Number of overlapped pairs of the corresponding conformation 
    */      
    List<Map<String, Double>> data = new ArrayList<Map<String, Double>>();
    double bestFitness = 0;
    double fitnessSum = 0;
    int bestCandidateIndex = 0;

    public Population(List<Type> sequence, int populationSize) throws Exception {
        this.sequence = sequence;
        this.generateFirstGeneration(sequence, populationSize);
        this.updateData();
    }

    private void generateFirstGeneration(List<Type> sequence, int populationSize) throws Exception {
        for (int i = 0; i < populationSize; i++) {
            List<Direction> newConformation = new ArrayList<Direction>();

            // Generate a conformation randomly
            for (int j = 0; j < sequence.size() - 1; j++) {
                newConformation.add(Direction.getRandom());
            }
            newConformation.add(Direction.STOP);
            this.conformations.add(newConformation);
        }
    }

    public void generateImages(int numOfConformation) throws Exception {
        for (int i = 0; i < numOfConformation; i++) {
            String imgName = Integer.toString(i) + ".png";
            Graphic.generateImage(this.conformations.get(i), this.sequence, imgName);
        }
    }

    public void generatBestImage() throws Exception {
        String imgName = "LocalBestCandidate.png";
        Graphic.generateImage(this.conformations.get(this.bestCandidateIndex), this.sequence, imgName);
    }

    public void updateData() throws Exception {
        Calculation calculator = new Calculation();
        this.data = new ArrayList<Map<String, Double>>();
        this.bestFitness = 0;
        this.fitnessSum = 0;

        for (int i = 0; i < this.conformations.size(); i++) {
            // Add data (fitness, hh pairs, overlapped) in data
            Map<String, Double> data = calculator.getFitness(this.conformations.get(i), this.sequence);
            this.data.add(data);
            this.fitnessSum += data.get("fitness");

            if (data.get("fitness") > this.bestFitness) {
                this.bestFitness = data.get("fitness");
                this.bestCandidateIndex = i;
            }
        }
    }

    public double evaluate() {
        return this.fitnessSum / this.data.size();
    }

    /** Select next generation using fitness proportional selection with Roulette wheel*/
    public void proportionalSelection() throws Exception {
        this.updateData();
        
        int populationSize = this.conformations.size();
        List<List<Direction>> selected = new ArrayList<List<Direction>>();
        // Select individuals proportional to their fitness
        while (selected.size() < populationSize) {
            double rand = Math.random() * this.fitnessSum;

            double cumulativeFitness = 0.0;
            for (int i = 0; i < populationSize; i++) {
                cumulativeFitness += this.data.get(i).get("fitness");
                if (cumulativeFitness > rand) {
                    selected.add(this.conformations.get(i));
                    break;
                }
            }
        }

        this.generation++;
        this.conformations = selected;
        this.updateData();
    }

    public void tournamentSelection(int tournamentSize, double tournamentParam) throws Exception {
        if (tournamentParam > 1 && tournamentParam < 0) {
            throw new Exception("Invalid tourament parameter: 0 < tourament parameter < 1");
        }


        List<List<Direction>> selected = new ArrayList<List<Direction>>();
        while (selected.size() < this.conformations.size()) {
            Random random = new Random();

            // Get tournament candidates according to tournament size
            List<List<Direction>> tour_cand = new ArrayList<List<Direction>>();
            List<Integer> generatedIndex = new ArrayList<Integer>();
            while (tour_cand.size() != tournamentSize) {
                int cand_index = random.nextInt(conformations.size());

                if (!generatedIndex.contains(cand_index)) {
                    tour_cand.add(this.conformations.get(cand_index));
                    generatedIndex.add(cand_index);
                }
            }

            double rand = Math.random();
            Calculation calculator = new Calculation();
            List<Direction> selectedCand = tour_cand.get(0);
            double selectedFitness = calculator.getFitness(selectedCand, this.sequence).get("fitness");
            for (int i = 1; i < tour_cand.size(); i++) {
                List<Direction> currentCand = tour_cand.get(i);
                double currentFitness = calculator.getFitness(currentCand, this.sequence).get("fitness");

                if (rand < tournamentParam) {
                    if (currentFitness > selectedFitness) {
                        selectedCand = currentCand;
                    }
                }
                else {
                    if (currentFitness < selectedFitness) {
                        selectedCand = currentCand;
                    }
                }
            }
            selected.add(selectedCand);
        }

        this.generation++;
        this.conformations = selected;
        this.updateData();
    }

    public void mutation(double mutationRate) throws Exception {
        Calculation calculator = new Calculation();

        for(int i = 0; i < this.conformations.size(); i++) {
            for(int j = 0; j < this.conformations.get(i).size(); j++) {
                if (Math.random() < mutationRate) {
                    Direction newPoint = Direction.getRandom(this.conformations.get(i).get(j));
                    this.conformations.get(i).set(j, newPoint);
                    this.data.set(i, calculator.getFitness(this.conformations.get(i), sequence));
                }
            }
        }
    }

    public void mutation_2(double mutationRate) throws Exception {
        Calculation calculator = new Calculation();
        int num = (int) (mutationRate * this.conformations.size() * this.sequence.size());

        for(int i = 0; i < num; i++) {
            Random random = new Random();
            int m = random.nextInt(this.conformations.size());
            int n = random.nextInt(this.sequence.size());

            Direction newPoint = Direction.getRandom(this.conformations.get(m).get(n));
            this.conformations.get(m).set(n, newPoint);
            this.data.set(m, calculator.getFitness(this.conformations.get(m), sequence));
        }
    }

    public void crossOver(double rate) {
        for (int i = 0; i < this.conformations.size(); i++) {
            int firstIndex = i;
            int secondIndex = i;

            List<Direction> candidate1 = new ArrayList<>();
            List<Direction> candidate2 = new ArrayList<>();

            if (Math.random() < rate) {
                Random rnd = new Random();
                candidate1 = this.conformations.get(i);

                while (secondIndex == i) {
                    secondIndex = rnd.nextInt(this.conformations.size());
                }
                candidate2 = this.conformations.get(secondIndex);

                List<List<Direction>> result = crossOver(candidate1, candidate2);
                this.conformations.set(firstIndex, result.get(0));
                this.conformations.set(secondIndex, result.get(1));
            }
        }
    }

    public List<List<Direction>> crossOver(List<Direction> candidate1, List<Direction> candidate2) {
        List<Direction> newCandidate1 = new ArrayList<>();
        List<Direction> newCandidate2 = new ArrayList<>();
        List<List<Direction>> result = new ArrayList<List<Direction>>();

        int crossoverPoint = 0;
        while (crossoverPoint == 0) {
            crossoverPoint = (int) (Math.random() * candidate1.size());
        }
    
        // Copy the first part of parent1 to the child
        for (int i = 0; i < crossoverPoint; i++) {
            newCandidate2.add(candidate1.get(i));
            newCandidate1.add(candidate2.get(i));
        }
        
        // Copy the second part of parent2 to the child
        for (int i = crossoverPoint; i < candidate1.size(); i++) {
            newCandidate1.add(candidate1.get(i));
            newCandidate2.add(candidate2.get(i));
        }

        result.add(newCandidate1);
        result.add(newCandidate2);
        return result;
    }
}
