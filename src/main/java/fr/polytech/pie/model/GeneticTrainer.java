package fr.polytech.pie.model;

import fr.polytech.pie.Consts;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Genetic algorithm trainer for Tetris AI coefficient optimization.
 * Optimizes the parameters: Aggregate Height, Complete Lines, Holes, and
 * Bumpiness
 * using a genetic algorithm on the unit 3-sphere.
 */
public class GeneticTrainer {
    private static final Logger LOGGER = Logger.getLogger(GeneticTrainer.class.getName());

    // Genetic algorithm parameters
    private static final int POPULATION_SIZE = 1000;
    private static final int TOURNAMENT_SIZE = 100; // 10% of population
    private static final int OFFSPRING_COUNT = 300; // 30% of population
    private static final double INITIAL_MUTATION_PROBABILITY = 0.05;
    private static final double FINAL_MUTATION_PROBABILITY = 0.01;
    private static final double INITIAL_MUTATION_RANGE = 0.2;
    private static final double FINAL_MUTATION_RANGE = 0.05;
    private static final int GAMES_PER_EVALUATION = 100;
    private static final int MAX_PIECES_PER_GAME = 500;

    // Current training parameters
    private double currentMutationProbability;
    private double currentMutationRange;

    // List of individual parameter vectors (normalized to unit length)
    private List<Individual> population;

    // Random number generator
    private final Random random = new Random();

    // Previous best fitness for monitoring convergence
    private int previousBestFitness = 0;
    private int generationsWithoutImprovement = 0;

    // Configurable parameters
    private final int populationSize;
    private final int gamesPerEvaluation;

    /**
     * Represents an individual in the population with its parameter vector and
     * fitness.
     */
    private static class Individual implements Comparable<Individual> {
        // Parameters: [heightWeight, linesWeight, holesWeight, bumpinessWeight]
        double[] parameters;
        int fitness; // Total lines cleared across all evaluation games

        public Individual(double[] parameters) {
            this.parameters = normalize(parameters);
            this.fitness = 0;
        }

        @Override
        public int compareTo(Individual other) {
            return Integer.compare(this.fitness, other.fitness);
        }

        /**
         * Normalizes a vector to unit length (projects it onto the unit 3-sphere)
         */
        private static double[] normalize(double[] vector) {
            double magnitude = 0;
            for (double v : vector) {
                magnitude += v * v;
            }
            magnitude = Math.sqrt(magnitude);

            // Avoid division by zero
            if (magnitude == 0) {
                return new double[] { 1, 0, 0, 0 }; // Default unit vector
            }

            double[] normalized = new double[vector.length];
            for (int i = 0; i < vector.length; i++) {
                normalized[i] = vector[i] / magnitude;
            }
            return normalized;
        }
    }

    /**
     * Initialize the genetic trainer with custom parameters.
     * 
     * @param populationSize     Size of the population
     * @param gamesPerEvaluation Number of games to run for each individual
     */
    public GeneticTrainer(int populationSize, int gamesPerEvaluation) {
        // Store custom values for this instance
        this.populationSize = populationSize;
        this.gamesPerEvaluation = gamesPerEvaluation;

        initializePopulation();
        currentMutationProbability = INITIAL_MUTATION_PROBABILITY;
        currentMutationRange = INITIAL_MUTATION_RANGE;
    }

    /**
     * Initializes the population with random individuals on the unit 3-sphere.
     */
    private void initializePopulation() {
        population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {
            // Create random parameters in [-1, 1]
            double[] params = new double[4];
            for (int j = 0; j < params.length; j++) {
                params[j] = random.nextDouble() * 2 - 1;
            }

            // Add normalized individual to population
            population.add(new Individual(params));
        }
    }

    /**
     * Runs the genetic algorithm for the specified number of generations.
     * 
     * @param generations Number of generations to evolve
     * @return The best individual found (parameter vector)
     */
    public double[] train(int generations) {
        for (int gen = 0; gen < generations; gen++) {
            LOGGER.log(Level.INFO, "Starting generation {0}", gen + 1);

            // Update adaptive parameters
            updateAdaptiveParameters(gen, generations);

            // Evaluate fitness of all individuals
            evaluatePopulation();

            // Sort population by fitness (descending)
            population.sort(Collections.reverseOrder());

            // Check for improvement and update tracking variables
            int currentBestFitness = population.getFirst().fitness;
            if (currentBestFitness > previousBestFitness) {
                generationsWithoutImprovement = 0;
                previousBestFitness = currentBestFitness;
            } else {
                generationsWithoutImprovement++;
            }

            // Log best fitness in this generation
            LOGGER.log(Level.INFO, "Best fitness in generation {0}: {1}, Mutation rate: {2}, Mutation range: {3}",
                    new Object[] { gen + 1, currentBestFitness, currentMutationProbability, currentMutationRange });
            LOGGER.log(Level.INFO, "Parameters: {0}, {1}, {2}, {3}",
                    new Object[] {
                            population.getFirst().parameters[0],
                            population.getFirst().parameters[1],
                            population.getFirst().parameters[2],
                            population.getFirst().parameters[3]
                    });

            // Create offspring through selection, crossover, and mutation
            List<Individual> offspring = createOffspring();

            // Replace worst individuals with offspring
            replaceWorstIndividuals(offspring);

            // If no improvement for many generations, reset part of the population to avoid
            // local optima
            if (generationsWithoutImprovement > 10) {
                resetStagnantPopulation();
                generationsWithoutImprovement = 0;
            }
        }

        // Return the best parameter vector after all generations
        population.sort(Collections.reverseOrder());
        return population.getFirst().parameters;
    }

    /**
     * Updates the adaptive parameters based on training progress.
     * 
     * @param currentGeneration Current generation number
     * @param totalGenerations  Total number of generations
     */
    private void updateAdaptiveParameters(int currentGeneration, int totalGenerations) {
        // Calculate progress (0 to 1)
        double progress = (double) currentGeneration / totalGenerations;

        // Linear decrease of mutation probability and range
        currentMutationProbability = INITIAL_MUTATION_PROBABILITY
                - progress * (INITIAL_MUTATION_PROBABILITY - FINAL_MUTATION_PROBABILITY);

        currentMutationRange = INITIAL_MUTATION_RANGE
                - progress * (INITIAL_MUTATION_RANGE - FINAL_MUTATION_RANGE);
    }

    /**
     * Reset part of the population to escape local optima
     */
    private void resetStagnantPopulation() {
        LOGGER.log(Level.INFO, "Stagnation detected. Resetting 20% of the population.");

        // Keep the top 20% of individuals
        int keepCount = (int) (populationSize * 0.2);

        // Replace the bottom 80% with new random individuals
        for (int i = keepCount; i < populationSize; i++) {
            double[] params = new double[4];
            for (int j = 0; j < params.length; j++) {
                params[j] = random.nextDouble() * 2 - 1;
            }
            population.set(i, new Individual(params));
        }
    }

    /**
     * Evaluates the fitness of all individuals in the population.
     */
    private void evaluatePopulation() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Use a more efficient parallel approach with work stealing
        try {
            List<Future<Integer>> futures = new ArrayList<>();

            // Submit evaluation tasks for all individuals in batches
            for (Individual individual : population) {
                Future<Integer> future = executor.submit(() -> evaluateIndividual(individual));
                futures.add(future);
            }

            // Collect results as they complete
            for (int i = 0; i < futures.size(); i++) {
                population.get(i).fitness = futures.get(i).get();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during parallel evaluation", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Evaluates a single individual by running multiple games with its parameters.
     * Uses a progressive difficulty approach.
     * 
     * @param individual The individual to evaluate
     * @return The fitness score (total lines cleared)
     */
    private int evaluateIndividual(Individual individual) {
        int totalLinesCleared = 0;

        // Determine the generation-appropriate game parameters
        int piecesPerGame = getAdaptivePieceCount();

        // Run multiple games to get a reliable fitness measure
        for (int game = 0; game < gamesPerEvaluation; game++) {
            int linesCleared = runGame(individual.parameters, piecesPerGame);
            totalLinesCleared += linesCleared;
        }

        return totalLinesCleared;
    }

    /**
     * Determines adaptive piece count based on training progress.
     * Early in training, fewer pieces help find quickly effective strategies.
     * Later, more pieces test long-term effectiveness.
     */
    private int getAdaptivePieceCount() {
        // Start with fewer pieces and gradually increase as training progresses
        double progressRatio = Math.min(1.0, (double) generationsWithoutImprovement / 5);

        // Between 100 and MAX_PIECES_PER_GAME
        return 100 + (int) (progressRatio * (MAX_PIECES_PER_GAME - 100));
    }

    /**
     * Runs a single game with the given parameters and returns the number of lines
     * cleared.
     * 
     * @param parameters The AI parameters to use
     * @param maxPieces  Maximum number of pieces for this game
     * @return Number of lines cleared in this game
     */
    private int runGame(double[] parameters, int maxPieces) {
        // Create a new game and grid for evaluation
        Grid grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);
        Ai ai = new Ai(grid, parameters);
        int linesCleared = 0;
        int piecesWithoutLines = 0;

        // Run the game for a fixed number of pieces
        for (int piece = 0; piece < maxPieces; piece++) {
            CurrentPiece currentPiece = PieceGenerator.generatePiece(grid.getWidth(), false);

            // If we can't place the piece, game over
            if (grid.checkCollision(currentPiece)) {
                break;
            }

            // Let the AI make a move
            ai.makeMove(currentPiece);

            // Count and clear lines
            int newLines = grid.clearFullLines();
            linesCleared += newLines;

            // Early termination logic for efficiency
            if (newLines > 0) {
                piecesWithoutLines = 0;
            } else {
                piecesWithoutLines++;
            }

            // Check if the grid is getting too high (likely to lose soon)
            int maxHeight = 0;
            for (int i = 0; i < grid.getWidth(); i++) {
                maxHeight = Math.max(maxHeight, grid.getHeightOfColumn(i));
            }

            // If we haven't cleared lines in a long time and the grid is high, terminate
            // early
            if (piecesWithoutLines > 25 && maxHeight > grid.getHeight() * 0.7) {
                break;
            }
        }

        return linesCleared;
    }

    /**
     * Creates offspring through tournament selection and crossover.
     * 
     * @return List of offspring individuals
     */
    private List<Individual> createOffspring() {
        List<Individual> offspring = new ArrayList<>(OFFSPRING_COUNT);

        for (int i = 0; i < OFFSPRING_COUNT; i++) {
            // Tournament selection to pick parents
            Individual parent1 = tournamentSelection();
            Individual parent2 = tournamentSelection();

            // Weighted average crossover
            Individual child = crossover(parent1, parent2);

            // Potentially mutate
            if (random.nextDouble() < currentMutationProbability) {
                mutate(child);
            }

            offspring.add(child);
        }

        return offspring;
    }

    /**
     * Performs tournament selection to pick a parent.
     * 
     * @return Selected parent individual
     */
    private Individual tournamentSelection() {
        // Randomly select individuals for the tournament
        List<Individual> tournament = new ArrayList<>(TOURNAMENT_SIZE);
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = random.nextInt(populationSize);
            tournament.add(population.get(randomIndex));
        }

        // Sort by fitness and return the best one
        tournament.sort(Collections.reverseOrder());
        return tournament.getFirst();
    }

    /**
     * Performs crossover between two parents using either weighted averaging
     * or spherical interpolation based on the situation.
     * 
     * @param parent1 First parent
     * @param parent2 Second parent
     * @return Offspring individual
     */
    private Individual crossover(Individual parent1, Individual parent2) {
        // Occasionally use spherical interpolation instead of weighted averaging
        if (random.nextDouble() < 0.2) {
            return sphericalInterpolation(parent1, parent2);
        }

        // Standard weighted average crossover
        double totalFitness = parent1.fitness + parent2.fitness;
        double weight1 = parent1.fitness / totalFitness;
        double weight2 = parent2.fitness / totalFitness;

        // If both parents have 0 fitness, use equal weights
        if (totalFitness == 0) {
            weight1 = weight2 = 0.5;
        }

        // Weighted average of parameters
        double[] childParams = new double[4];
        for (int i = 0; i < 4; i++) {
            childParams[i] = parent1.parameters[i] * weight1 + parent2.parameters[i] * weight2;
        }

        // Create new individual with the combined parameters (will be normalized)
        return new Individual(childParams);
    }

    /**
     * Performs spherical interpolation (SLERP) between two parameter vectors.
     * This maintains the unit 3-sphere constraint while allowing exploration
     * along the geodesic path between the two vectors.
     * 
     * @param parent1 First parent
     * @param parent2 Second parent
     * @return Offspring created via spherical interpolation
     */
    private Individual sphericalInterpolation(Individual parent1, Individual parent2) {
        // Calculate the dot product to find the angle between vectors
        double dotProduct = 0;
        for (int i = 0; i < 4; i++) {
            dotProduct += parent1.parameters[i] * parent2.parameters[i];
        }

        // Clamp dot product to avoid numerical issues
        dotProduct = Math.max(-1, Math.min(1, dotProduct));

        // Calculate the angle between the vectors
        double theta = Math.acos(dotProduct);

        // If vectors are very close, just use linear interpolation
        if (theta < 1e-5) {
            return crossover(parent1, parent2);
        }

        // Relative fitness values determine the interpolation parameter
        double totalFitness = parent1.fitness + parent2.fitness;
        double t = (totalFitness == 0) ? 0.5 : parent1.fitness / totalFitness;

        // Add some randomness to explore more of the space between parents
        t = t * 0.8 + random.nextDouble() * 0.2;

        // SLERP formula
        double sinTheta = Math.sin(theta);
        double[] childParams = new double[4];
        double scale1 = Math.sin((1 - t) * theta) / sinTheta;
        double scale2 = Math.sin(t * theta) / sinTheta;

        for (int i = 0; i < 4; i++) {
            childParams[i] = parent1.parameters[i] * scale1 + parent2.parameters[i] * scale2;
        }

        return new Individual(childParams);
    }

    /**
     * Mutates an individual by randomly adjusting one parameter.
     * 
     * @param individual The individual to mutate
     */
    private void mutate(Individual individual) {
        // Select a random parameter to mutate
        int paramIndex = random.nextInt(4);

        // Apply random adjustment within the mutation range
        double adjustment = (random.nextDouble() * 2 - 1) * currentMutationRange;
        individual.parameters[paramIndex] += adjustment;

        // Re-normalize to keep on unit 3-sphere
        individual.parameters = Individual.normalize(individual.parameters);
    }

    /**
     * Replaces the worst individuals in the population with the given offspring.
     * 
     * @param offspring List of offspring to add to the population
     */
    private void replaceWorstIndividuals(List<Individual> offspring) {
        // Sort population by fitness (ascending, so worst are first)
        Collections.sort(population);

        // Replace worst individuals with offspring
        for (int i = 0; i < offspring.size(); i++) {
            population.set(i, offspring.get(i));
        }
    }

    /**
     * Main method to run the genetic algorithm and print the best parameters.
     * --generations 20 --population 500 --games 50 for a quick test
     * --generations 100 --population 2000 --games 200 for a better test
     * --output parameters.txt to save the best parameters to a file
     */
    public static void main(String[] args) {
        // Default configuration
        int generations = 50;
        int populationSize = POPULATION_SIZE;
        int gamesPerEvaluation = GAMES_PER_EVALUATION;
        String outputFile = null;

        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--generations") && i + 1 < args.length) {
                try {
                    generations = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid generations value, using default: {0}", generations);
                }
            } else if (args[i].equals("--population") && i + 1 < args.length) {
                try {
                    populationSize = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid population size, using default: {0}", populationSize);
                }
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                try {
                    gamesPerEvaluation = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid games per evaluation, using default: {0}", gamesPerEvaluation);
                }
            } else if (args[i].equals("--output") && i + 1 < args.length) {
                outputFile = args[++i];
            } else if (args[i].equals("--help")) {
                printHelp();
                return;
            }
        }

        LOGGER.log(Level.INFO, "Starting genetic training with:");
        LOGGER.log(Level.INFO, "  Generations: {0}", generations);
        LOGGER.log(Level.INFO, "  Population size: {0}", populationSize);
        LOGGER.log(Level.INFO, "  Games per evaluation: {0}", gamesPerEvaluation);

        GeneticTrainer trainer = new GeneticTrainer(populationSize, gamesPerEvaluation);
        double[] bestParameters = trainer.train(generations);

        LOGGER.log(Level.INFO, "Optimal parameters found:");
        LOGGER.log(Level.INFO, "Height Weight: {0}", bestParameters[0]);
        LOGGER.log(Level.INFO, "Lines Weight: {0}", bestParameters[1]);
        LOGGER.log(Level.INFO, "Holes Weight: {0}", bestParameters[2]);
        LOGGER.log(Level.INFO, "Bumpiness Weight: {0}", bestParameters[3]);

        // Save parameters to file if specified
        if (outputFile != null) {
            saveParametersToFile(bestParameters, outputFile);
        }
    }

    /**
     * Print help information for command line options
     */
    private static void printHelp() {
        System.out.println("GeneticTrainer - Optimize Tetris AI parameters using genetic algorithms");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --generations <num>  Number of generations to evolve (default: 50)");
        System.out.println("  --population <num>   Population size (default: 1000)");
        System.out.println("  --games <num>        Games per evaluation (default: 100)");
        System.out.println("  --output <filename>  Save best parameters to a file");
        System.out.println("  --help               Display this help message");
    }

    /**
     * Save parameters to a file
     */
    private static void saveParametersToFile(double[] parameters, String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
            writer.println("// Tetris AI parameters generated by GeneticTrainer");
            writer.println("// " + new java.util.Date());
            writer.println("double heightWeight = " + parameters[0] + ";");
            writer.println("double linesWeight = " + parameters[1] + ";");
            writer.println("double holesWeight = " + parameters[2] + ";");
            writer.println("double bumpinessWeight = " + parameters[3] + ";");
            LOGGER.log(Level.INFO, "Parameters saved to {0}", filename);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save parameters to file: " + e.getMessage(), e);
        }
    }
}