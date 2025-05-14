package fr.polytech.pie.model;

import fr.polytech.pie.Consts;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Genetic algorithm trainer for Tetris AI coefficient optimization.
 * Optimizes the parameters: Aggregate Height, Complete Lines, Holes, and Bumpiness
 * using a genetic algorithm on the unit 3-sphere.
 */
public class GeneticTrainer {
    private static final Logger LOGGER = Logger.getLogger(GeneticTrainer.class.getName());

    // Genetic algorithm parameters
    private static final int POPULATION_SIZE = 1000;
    private static final int TOURNAMENT_SIZE = 100; // 10% of population
    private static final int OFFSPRING_COUNT = 300; // 30% of population
    private static final double MUTATION_PROBABILITY = 0.05;
    private static final double MUTATION_RANGE = 0.2;
    private static final int GAMES_PER_EVALUATION = 100;
    private static final int MAX_PIECES_PER_GAME = 500;

    // List of individual parameter vectors (normalized to unit length)
    private List<Individual> population;

    // Random number generator
    private final Random random = new Random();

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

        // Copy constructor
        public Individual(Individual other) {
            this.parameters = Arrays.copyOf(other.parameters, other.parameters.length);
            this.fitness = other.fitness;
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
     * Initialize the genetic trainer with a random population.
     */
    public GeneticTrainer() {
        initializePopulation();
    }

    /**
     * Initializes the population with random individuals on the unit 3-sphere.
     */
    private void initializePopulation() {
        population = new ArrayList<>(POPULATION_SIZE);

        for (int i = 0; i < POPULATION_SIZE; i++) {
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

            // Evaluate fitness of all individuals
            evaluatePopulation();

            // Sort population by fitness (descending)
            Collections.sort(population, Collections.reverseOrder());

            // Log best fitness in this generation
            LOGGER.log(Level.INFO, "Best fitness in generation {0}: {1}",
                    new Object[] { gen + 1, population.get(0).fitness });
            LOGGER.log(Level.INFO, "Parameters: {0}, {1}, {2}, {3}",
                    new Object[] {
                            population.get(0).parameters[0],
                            population.get(0).parameters[1],
                            population.get(0).parameters[2],
                            population.get(0).parameters[3]
                    });

            // Create offspring through selection, crossover, and mutation
            List<Individual> offspring = createOffspring();

            // Replace worst individuals with offspring
            replaceWorstIndividuals(offspring);
        }

        // Return the best parameter vector after all generations
        Collections.sort(population, Collections.reverseOrder());
        return population.get(0).parameters;
    }

    /**
     * Evaluates the fitness of all individuals in the population.
     */
    private void evaluatePopulation() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Submit evaluation tasks for all individuals
        List<Future<Void>> futures = new ArrayList<>();

        for (Individual individual : population) {
            futures.add(executor.submit(() -> {
                individual.fitness = evaluateIndividual(individual);
                return null;
            }));
        }

        // Wait for all evaluations to complete
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error evaluating individual", e);
            }
        }

        executor.shutdown();
    }

    /**
     * Evaluates a single individual by running multiple games with its parameters.
     * 
     * @param individual The individual to evaluate
     * @return The fitness score (total lines cleared)
     */
    private int evaluateIndividual(Individual individual) {
        int totalLinesCleared = 0;

        // Run multiple games to get a reliable fitness measure
        for (int game = 0; game < GAMES_PER_EVALUATION; game++) {
            int linesCleared = runGame(individual.parameters);
            totalLinesCleared += linesCleared;
        }

        return totalLinesCleared;
    }

    /**
     * Runs a single game with the given parameters and returns the number of lines
     * cleared.
     * 
     * @param parameters The AI parameters to use
     * @return Number of lines cleared in this game
     */
    private int runGame(double[] parameters) {
        // Create a new game and grid for evaluation
        Grid grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);
        TestAi ai = new TestAi(grid, parameters);
        int linesCleared = 0;

        // Run the game for a fixed number of pieces
        for (int piece = 0; piece < MAX_PIECES_PER_GAME; piece++) {
            CurrentPiece currentPiece = PieceGenerator.generatePiece(grid.getWidth());

            // If we can't place the piece, game over
            if (grid.checkCollision(currentPiece)) {
                break;
            }

            // Let the AI make a move and count cleared lines
            linesCleared += ai.makeMove(currentPiece);
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
            if (random.nextDouble() < MUTATION_PROBABILITY) {
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
            int randomIndex = random.nextInt(POPULATION_SIZE);
            tournament.add(population.get(randomIndex));
        }

        // Sort by fitness and return the best one
        tournament.sort(Collections.reverseOrder());
        return tournament.get(0);
    }

    /**
     * Performs weighted average crossover between two parents.
     * 
     * @param parent1 First parent
     * @param parent2 Second parent
     * @return Offspring individual
     */
    private Individual crossover(Individual parent1, Individual parent2) {
        // Calculate weights based on fitness
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
     * Mutates an individual by randomly adjusting one parameter.
     * 
     * @param individual The individual to mutate
     */
    private void mutate(Individual individual) {
        // Select a random parameter to mutate
        int paramIndex = random.nextInt(4);

        // Apply random adjustment within the mutation range
        double adjustment = (random.nextDouble() * 2 - 1) * MUTATION_RANGE;
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
     * Special version of the AI that uses custom parameters and
     * returns the number of lines cleared from a move.
     */
    private static class TestAi {
        private Grid grid;
        private double heightWeight;
        private double linesWeight;
        private double holesWeight;
        private double bumpinessWeight;

        public TestAi(Grid grid, double[] parameters) {
            this.grid = grid;
            this.heightWeight = parameters[0];
            this.linesWeight = parameters[1];
            this.holesWeight = parameters[2];
            this.bumpinessWeight = parameters[3];
        }

        public int makeMove(CurrentPiece currentPiece) {
            final var availablePossibilities = getPiecesPossibilities(currentPiece);

            double bestScore = Double.NEGATIVE_INFINITY;
            CurrentPiece bestPiece = null;
            int bestLinesCleared = 0;

            for (var possibility : availablePossibilities) {
                grid.freezePiece(possibility);

                // Calculate aggregate height
                int heights = 0;
                for (int i = 0; i < grid.getWidth(); i++) {
                    heights += grid.getHeightOfColumn(i);
                }

                // Count lines that would be cleared
                int linesCleared = grid.countFullLines();

                // Count holes
                int holes = 0;
                for (int i = 0; i < grid.getWidth(); i++) {
                    boolean foundBlock = false;
                    for (int j = 0; j < grid.getHeight(); j++) {
                        if (grid.getValue(i, j)) {
                            foundBlock = true;
                        } else if (foundBlock) {
                            holes++;
                            foundBlock = false;
                        }
                    }
                }

                // Calculate bumpiness
                int bumpiness = 0;
                for (int i = 0; i < grid.getWidth() - 1; i++) {
                    bumpiness += Math.abs(grid.getHeightOfColumn(i) - grid.getHeightOfColumn(i + 1));
                }

                // Calculate score using the individual's parameters
                double score = heightWeight * heights +
                        linesWeight * linesCleared +
                        holesWeight * holes +
                        bumpinessWeight * bumpiness;

                if (score > bestScore) {
                    bestScore = score;
                    bestPiece = possibility;
                    bestLinesCleared = linesCleared;
                }

                grid.removePiece(possibility);
            }

            // Place the best piece and clear lines
            grid.freezePiece(bestPiece);
            grid.clearFullLines();

            return bestLinesCleared;
        }

        private Set<CurrentPiece> getPiecesPossibilities(CurrentPiece currentPiece) {
            Set<CurrentPiece> possibilities = new HashSet<>();

            // Generate rotations
            CurrentPiece workingPiece = currentPiece.clone();
            for (int i = 0; i < 4; i++) {
                workingPiece.rotate(e -> grid.checkCollision(e));
                possibilities.add(workingPiece.clone());
            }

            // Generate translations
            Set<CurrentPiece> newTranslations = new HashSet<>();
            for (var piece : possibilities) {
                for (int i = 0; i < grid.getWidth(); i++) {
                    CurrentPiece translatedPiece = piece.clone();
                    translatedPiece.setX(i);
                    if (!grid.checkCollision(translatedPiece)) {
                        newTranslations.add(translatedPiece);
                    }
                }
            }
            possibilities = newTranslations;

            // Drop the pieces
            for (var piece : possibilities) {
                do {
                    piece.setY(piece.getY() + 1);
                } while (!grid.checkCollision(piece));
                piece.setY(piece.getY() - 1);
            }

            return possibilities;
        }
    }

    /**
     * Main method to run the genetic algorithm and print the best parameters.
     */
    public static void main(String[] args) {
        int generations = 50;
        if (args.length > 0) {
            try {
                generations = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid generations argument, using default: {0}", generations);
            }
        }

        LOGGER.log(Level.INFO, "Starting genetic training for {0} generations", generations);
        GeneticTrainer trainer = new GeneticTrainer();
        double[] bestParameters = trainer.train(generations);

        LOGGER.log(Level.INFO, "Optimal parameters found:");
        LOGGER.log(Level.INFO, "Height Weight: {0}", bestParameters[0]);
        LOGGER.log(Level.INFO, "Lines Weight: {0}", bestParameters[1]);
        LOGGER.log(Level.INFO, "Holes Weight: {0}", bestParameters[2]);
        LOGGER.log(Level.INFO, "Bumpiness Weight: {0}", bestParameters[3]);
    }
}