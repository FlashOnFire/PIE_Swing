package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Ai;
import fr.polytech.pie.model.CurrentPiece;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class Ai2D implements Ai {
    static protected double heightWeight = -0.7303205229567257;
    static protected double linesWeight = 0.6082323862482821;
    static protected double bumpinessWeight = -0.22463833194827965;
    static protected double holesWeight = -0.21499515782089093;
    private final Grid2D grid;
    private final ExecutorService executorService;
    private final int availableProcessors;

    public Ai2D(Grid2D grid) {
        super();
        this.grid = grid;
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(availableProcessors);
    }

    public Ai2D(Grid2D grid, double[] parameters) {
        this.grid = grid;
        heightWeight = parameters[0];
        linesWeight = parameters[1];
        bumpinessWeight = parameters[2];
        holesWeight = parameters[3];
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(availableProcessors);
    }

    @Override
    public void makeMove(CurrentPiece currentPiece, CurrentPiece nextPiece) {
        if (!(currentPiece instanceof CurrentPiece2D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }

        final var availablePossibilities = getPiecesPossibilities((CurrentPiece2D) currentPiece);

        // Rate each possibility using multiple threads
        double best = Double.NEGATIVE_INFINITY;
        CurrentPiece bestPiece = null;
        
        try {
            List<Callable<PieceMoveScore>> tasks = getCallables(nextPiece, availablePossibilities);

            // Execute all tasks
            List<Future<PieceMoveScore>> results = executorService.invokeAll(tasks);
            
            // Find the best move from all results
            for (Future<PieceMoveScore> result : results) {
                PieceMoveScore moveScore = result.get();
                if (moveScore.score > best) {
                    best = moveScore.score;
                    bestPiece = moveScore.piece;
                }
            }
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error during parallel processing: " + e.getMessage());
            e.printStackTrace();
            // Fallback to the current piece if error occurs
            bestPiece = currentPiece;
        }

        grid.freezePiece(bestPiece != null ? bestPiece : currentPiece);
    }

    @NotNull
    private List<Callable<PieceMoveScore>> getCallables(CurrentPiece nextPiece, Set<CurrentPiece2D> availablePossibilities) {
        List<Callable<PieceMoveScore>> tasks = new ArrayList<>();

        // Create tasks for evaluating each possible move
        for (CurrentPiece2D possibility : availablePossibilities) {
            tasks.add(() -> {
                Grid2D threadLocalGrid = (Grid2D) grid.copy(); // Create a copy of the grid for thread-safe operations
                double bestScore = Double.NEGATIVE_INFINITY;

                threadLocalGrid.freezePiece(possibility);
                Set<CurrentPiece2D> nextPiecePossibilities = getPiecesPossibilities((CurrentPiece2D) nextPiece.clone(), threadLocalGrid);

                for (CurrentPiece2D nextPiecePossibility : nextPiecePossibilities) {
                    threadLocalGrid.freezePiece(nextPiecePossibility);
                    double score = getScore(threadLocalGrid);
                    if (score > bestScore) {
                        bestScore = score;
                    }
                    threadLocalGrid.removePiece(nextPiecePossibility);
                }

                return new PieceMoveScore(possibility, bestScore);
            });
        }
        return tasks;
    }

    private double getScore(Grid2D gridToScore) {
        int heights = 0;
        for (int i = 0; i < gridToScore.getWidth(); i++) {
            heights = heights + gridToScore.getHeightOfColumn2D(i);
        }

        // Count completed lines
        int completedLines = gridToScore.clearFullLines(true);

        // Count holes
        int holes = gridToScore.getHoles();

        // calculate bumpiness (to avoid having a big vertical hole)
        int bumpiness = 0;
        for (int i = 0; i < gridToScore.getWidth() - 1; i++) {
            bumpiness += Math.abs(gridToScore.getHeightOfColumn2D(i) - gridToScore.getHeightOfColumn2D(i + 1));
        }

        return heightWeight * heights +
                linesWeight * completedLines +
                holesWeight * holes +
                bumpinessWeight * bumpiness;
    }

    private double getRate() {
        return getScore(grid);
    }

    private Set<CurrentPiece2D> getPiecesPossibilities(CurrentPiece2D currentPiece) {
        return getPiecesPossibilities(currentPiece, grid);
    }

    private Set<CurrentPiece2D> getPiecesPossibilities(CurrentPiece2D currentPiece, Grid2D gridToUse) {
        Set<CurrentPiece2D> possibilities = new HashSet<>();

        // generate rotations
        CurrentPiece2D workingPiece = currentPiece.clone();
        for (int i = 0; i < 4; i++) {
            workingPiece.rotate2d(_ -> false);
            workingPiece.setY(gridToUse.getHeight() - workingPiece.getHeight());
            possibilities.add(workingPiece.clone());
        }

        // generate translations
        Set<CurrentPiece2D> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int i = 0; i < gridToUse.getWidth(); i++) {
                CurrentPiece2D translatedPiece = piece.clone();
                translatedPiece.setX(i);
                if (!gridToUse.checkCollision(translatedPiece)) {
                    newTranslations.add(translatedPiece);
                }
            }
        }
        possibilities = newTranslations;

        // drops the pieces
        for (var piece : possibilities) {
            do {
                piece.setY(piece.getY() - 1);
            } while (!gridToUse.checkCollision(piece));
            piece.setY(piece.getY() + 1);
        }

        return possibilities;
    }

    // Helper class to store piece and its score
    private record PieceMoveScore(CurrentPiece2D piece, double score) {
    }
}
