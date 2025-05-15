package fr.polytech.pie.model;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.TwoD.Ai2D;
import fr.polytech.pie.model.TwoD.Grid2D;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeneticTrainer {
    private static final int POPULATION_SIZE = 1000;
    private static final int TOURNAMENT_SIZE = 100;
    private static final int OFFSPRING_COUNT = 300;
    private static final double INITIAL_MUTATION_PROBABILITY = 0.05;
    private static final double FINAL_MUTATION_PROBABILITY = 0.01;
    private static final double INITIAL_MUTATION_RANGE = 0.2;
    private static final double FINAL_MUTATION_RANGE = 0.05;
    private static final int GAMES_PER_EVALUATION = 100;
    private static final int MAX_PIECES_PER_GAME = 500;
    private static final int MIN_PIECES_PER_GAME = 100;
    private static final int STAGNATION_THRESHOLD = 10;

    private double currentMutationProbability;
    private double currentMutationRange;
    private List<Individual> population;
    private final Random random = new Random();
    private int previousBestFitness = 0;
    private int generationsWithoutImprovement = 0;
    private final int populationSize;
    private final int gamesPerEvaluation;

    private static class Individual implements Comparable<Individual> {
        double[] parameters;
        int fitness;

        public Individual(double[] parameters) {
            this.parameters = normalize(parameters);
            this.fitness = 0;
        }

        @Override
        public int compareTo(Individual other) {
            return Integer.compare(this.fitness, other.fitness);
        }

        private static double[] normalize(double[] vector) {
            double magnitude = 0;
            for (double v : vector) {
                magnitude += v * v;
            }
            magnitude = Math.sqrt(magnitude);

            if (magnitude < 1e-10) {
                return new double[]{1, 0, 0, 0};
            }

            double[] normalized = new double[vector.length];
            for (int i = 0; i < vector.length; i++) {
                normalized[i] = vector[i] / magnitude;
            }
            return normalized;
        }
    }

    public GeneticTrainer(int populationSize, int gamesPerEvaluation) {
        this.populationSize = populationSize;
        this.gamesPerEvaluation = gamesPerEvaluation;
        initializePopulation();
        currentMutationProbability = INITIAL_MUTATION_PROBABILITY;
        currentMutationRange = INITIAL_MUTATION_RANGE;
    }

    private void initializePopulation() {
        population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            double[] params = new double[4];
            for (int j = 0; j < params.length; j++) {
                params[j] = random.nextDouble() * 2 - 1;
            }
            population.add(new Individual(params));
        }
    }

    public double[] train(int generations) {
        for (int gen = 0; gen < generations; gen++) {
            System.out.println("Starting generation " + (gen + 1));

            updateAdaptiveParameters(gen, generations);

            evaluatePopulation();
            population.sort(Collections.reverseOrder());

            checkImprovement();

            List<Individual> offspring = createOffspring();
            replaceWorstIndividuals(offspring);

            if (generationsWithoutImprovement > STAGNATION_THRESHOLD) {
                resetStagnantPopulation();
                generationsWithoutImprovement = 0;
            }
        }

        population.sort(Collections.reverseOrder());
        return population.getFirst().parameters;
    }

    private void checkImprovement() {
        int currentBestFitness = population.getFirst().fitness;

        System.out.println("Best fitness: " + currentBestFitness +
                ", Mutation rate: " + currentMutationProbability +
                ", range: " + currentMutationRange);
        System.out.println("Parameters: " +
                population.getFirst().parameters[0] + ", " +
                population.getFirst().parameters[1] + ", " +
                population.getFirst().parameters[2] + ", " +
                population.getFirst().parameters[3]);

        if (currentBestFitness > previousBestFitness) {
            generationsWithoutImprovement = 0;
            previousBestFitness = currentBestFitness;
        } else {
            generationsWithoutImprovement++;
        }
    }

    private void updateAdaptiveParameters(int currentGeneration, int totalGenerations) {
        double progress = (double) currentGeneration / totalGenerations;
        currentMutationProbability = INITIAL_MUTATION_PROBABILITY
                - progress * (INITIAL_MUTATION_PROBABILITY - FINAL_MUTATION_PROBABILITY);
        currentMutationRange = INITIAL_MUTATION_RANGE
                - progress * (INITIAL_MUTATION_RANGE - FINAL_MUTATION_RANGE);
    }

    private void resetStagnantPopulation() {
        System.out.println("Stagnation detected. Resetting 80% of the population.");
        int keepCount = (int) (populationSize * 0.2);

        for (int i = keepCount; i < populationSize; i++) {
            double[] params = new double[4];
            for (int j = 0; j < params.length; j++) {
                params[j] = random.nextDouble() * 2 - 1;
            }
            population.set(i, new Individual(params));
        }
    }

    private void evaluatePopulation() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try {
            List<Future<Integer>> futures = new ArrayList<>();

            for (Individual individual : population) {
                futures.add(executor.submit(() -> evaluateIndividual(individual)));
            }

            for (int i = 0; i < futures.size(); i++) {
                population.get(i).fitness = futures.get(i).get();
            }
        } catch (Exception e) {
            System.out.println("Error during evaluation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private int evaluateIndividual(Individual individual) {
        int totalLinesCleared = 0;
        int piecesPerGame = getAdaptivePieceCount();

        for (int game = 0; game < gamesPerEvaluation; game++) {
            totalLinesCleared += runGame(individual.parameters, piecesPerGame);
        }

        return totalLinesCleared;
    }

    private int getAdaptivePieceCount() {
        double progressRatio = Math.min(1.0, (double) generationsWithoutImprovement / 5);
        return MIN_PIECES_PER_GAME + (int) (progressRatio * (MAX_PIECES_PER_GAME - MIN_PIECES_PER_GAME));
    }

    private int runGame(double[] parameters, int maxPieces) {
        Grid2D grid = new Grid2D(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);

        Ai2D ai = new Ai2D(grid, parameters);
        int linesCleared = 0;
        int piecesWithoutLines = 0;

        for (int piece = 0; piece < maxPieces; piece++) {
            CurrentPiece currentPiece = PieceGenerator.generatePiece(grid.getWidth(), false);

            if (grid.checkCollision(currentPiece)) {
                break;
            }

            ai.makeMove(currentPiece);
            int newLines = grid.clearFullLines();
            linesCleared += newLines;

            if (newLines > 0) {
                piecesWithoutLines = 0;
            } else {
                piecesWithoutLines++;
            }

            int maxHeight = 0;
            for (int i = 0; i < grid.getWidth(); i++) {
                maxHeight = Math.max(maxHeight, ((Grid2D) grid).getHeightOfColumn2D(i));
            }

            if (piecesWithoutLines > 25 && maxHeight > grid.getHeight() * 0.7) {
                break;
            }
        }

        return linesCleared;
    }

    private List<Individual> createOffspring() {
        List<Individual> offspring = new ArrayList<>(OFFSPRING_COUNT);

        for (int i = 0; i < OFFSPRING_COUNT; i++) {
            Individual parent1 = tournamentSelection();
            Individual parent2 = tournamentSelection();
            Individual child = crossover(parent1, parent2);

            if (random.nextDouble() < currentMutationProbability) {
                mutate(child);
            }

            offspring.add(child);
        }

        return offspring;
    }

    private Individual tournamentSelection() {
        List<Individual> tournament = new ArrayList<>(TOURNAMENT_SIZE);
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.add(population.get(random.nextInt(populationSize)));
        }

        tournament.sort(Collections.reverseOrder());
        return tournament.getFirst();
    }

    private Individual crossover(Individual parent1, Individual parent2) {
        if (random.nextDouble() < 0.2) {
            return sphericalInterpolation(parent1, parent2);
        }

        double totalFitness = parent1.fitness + parent2.fitness;
        double weight1 = totalFitness == 0 ? 0.5 : parent1.fitness / totalFitness;
        double weight2 = totalFitness == 0 ? 0.5 : parent2.fitness / totalFitness;

        double[] childParams = new double[4];
        for (int i = 0; i < 4; i++) {
            childParams[i] = parent1.parameters[i] * weight1 + parent2.parameters[i] * weight2;
        }

        return new Individual(childParams);
    }

    private Individual sphericalInterpolation(Individual parent1, Individual parent2) {
        double dotProduct = 0;
        for (int i = 0; i < 4; i++) {
            dotProduct += parent1.parameters[i] * parent2.parameters[i];
        }
        dotProduct = Math.max(-1, Math.min(1, dotProduct));

        double theta = Math.acos(dotProduct);

        if (theta < 1e-5) {
            return crossover(parent1, parent2);
        }

        double totalFitness = parent1.fitness + parent2.fitness;
        double t = (totalFitness == 0) ? 0.5 : parent1.fitness / totalFitness;
        t = t * 0.8 + random.nextDouble() * 0.2;

        double sinTheta = Math.sin(theta);
        double[] childParams = new double[4];
        double scale1 = Math.sin((1 - t) * theta) / sinTheta;
        double scale2 = Math.sin(t * theta) / sinTheta;

        for (int i = 0; i < 4; i++) {
            childParams[i] = parent1.parameters[i] * scale1 + parent2.parameters[i] * scale2;
        }

        return new Individual(childParams);
    }

    private void mutate(Individual individual) {
        int paramIndex = random.nextInt(4);
        double adjustment = (random.nextDouble() * 2 - 1) * currentMutationRange;
        individual.parameters[paramIndex] += adjustment;
        individual.parameters = Individual.normalize(individual.parameters);
    }

    private void replaceWorstIndividuals(List<Individual> offspring) {
        Collections.sort(population);
        for (int i = 0; i < offspring.size(); i++) {
            population.set(i, offspring.get(i));
        }
    }

    public static void main(String[] args) {
        int generations = 50;
        int populationSize = POPULATION_SIZE;
        int gamesPerEvaluation = GAMES_PER_EVALUATION;
        String outputFile = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--generations":
                    if (i + 1 < args.length) generations = parseIntArg(args[++i], generations);
                    break;
                case "--population":
                    if (i + 1 < args.length) populationSize = parseIntArg(args[++i], populationSize);
                    break;
                case "--games":
                    if (i + 1 < args.length) gamesPerEvaluation = parseIntArg(args[++i], gamesPerEvaluation);
                    break;
                case "--output":
                    if (i + 1 < args.length) outputFile = args[++i];
                    break;
                case "--help":
                    printHelp();
                    return;
            }
        }

        System.out.println("Starting genetic training with:");
        System.out.println("  Generations: " + generations);
        System.out.println("  Population size: " + populationSize);
        System.out.println("  Games per evaluation: " + gamesPerEvaluation);

        GeneticTrainer trainer = new GeneticTrainer(populationSize, gamesPerEvaluation);
        double[] bestParameters = trainer.train(generations);

        System.out.println("Optimal parameters found:");
        System.out.println("Height Weight: " + bestParameters[0]);
        System.out.println("Lines Weight: " + bestParameters[1]);
        System.out.println("Holes Weight: " + bestParameters[2]);
        System.out.println("Bumpiness Weight: " + bestParameters[3]);

        if (outputFile != null) {
            saveParametersToFile(bestParameters, outputFile);
        }
    }

    private static int parseIntArg(String arg, int defaultValue) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            System.out.println("Invalid value, using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static void printHelp() {
        System.out.println("GeneticTrainer - Optimize Tetris AI parameters");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --generations <num>  Number of generations (default: 50)");
        System.out.println("  --population <num>   Population size (default: 1000)");
        System.out.println("  --games <num>        Games per evaluation (default: 100)");
        System.out.println("  --output <filename>  Save parameters to file");
        System.out.println("  --help               Display this help message");
    }

    private static void saveParametersToFile(double[] parameters, String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
            writer.println("// Tetris AI parameters generated by GeneticTrainer");
            writer.println("// " + new java.util.Date());
            writer.println("double heightWeight = " + parameters[0] + ";");
            writer.println("double linesWeight = " + parameters[1] + ";");
            writer.println("double holesWeight = " + parameters[2] + ";");
            writer.println("double bumpinessWeight = " + parameters[3] + ";");
            System.out.println("Parameters saved to " + filename);
        } catch (Exception e) {
            System.out.println("Failed to save parameters: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
