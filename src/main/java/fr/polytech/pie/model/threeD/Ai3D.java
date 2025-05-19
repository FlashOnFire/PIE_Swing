package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.Ai;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.RotationAxis;
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

public class Ai3D implements Ai {
    static protected double heightWeight = -0.6500491536113875;
    static protected double linesWeight = 0.5122503282774851;
    static protected double bumpinessWeight = -0.1763291765999144;
    static protected double holesWeight = -0.5328636979081272;
    private final Grid3D grid;
    private final ExecutorService executorService;
    private final int availableProcessors;

    public Ai3D(Grid3D grid) {
        super();
        this.grid = grid;
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(availableProcessors);
    }

    // maybe needed later for training
    public Ai3D(Grid3D grid, double[] parameters) {
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
        if (!(currentPiece instanceof CurrentPiece3D)) {
            throw new IllegalArgumentException("Ai3D can only handle CurrentPiece3D instances");
        }

        final var availablePossibilities = getPiecesPossibilities((CurrentPiece3D) currentPiece);

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
    private List<Callable<PieceMoveScore>> getCallables(CurrentPiece nextPiece, Set<CurrentPiece3D> availablePossibilities) {
        List<Callable<PieceMoveScore>> tasks = new ArrayList<>();

        // Create tasks for evaluating each possible move
        for (CurrentPiece3D possibility : availablePossibilities) {
            tasks.add(() -> {
                Grid3D threadLocalGrid = (Grid3D) grid.copy(); // Create a copy of the grid for thread-safe operations
                double bestScore = Double.NEGATIVE_INFINITY;

                threadLocalGrid.freezePiece(possibility);
                Set<CurrentPiece3D> nextPiecePossibilities = getPiecesPossibilities((CurrentPiece3D) nextPiece.clone(), threadLocalGrid);

                for (CurrentPiece3D nextPiecePossibility : nextPiecePossibilities) {
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

    private double getScore(Grid3D gridToScore) {
        int heights = 0;
        for (int x = 0; x < gridToScore.getWidth(); x++) {
            for (int z = 0; z < gridToScore.getDepth(); z++) {
                heights += gridToScore.getHeightOfColumn3D(x, z);
            }
        }

        // Count completed planes
        int completedPlanes = gridToScore.clearFullLines(true);

        // Count holes
        int holes = grid.getHoles();


        // Calculate bumpiness (to avoid having a big vertical hole)
        int bumpiness = 0;
        for (int x = 0; x < gridToScore.getWidth() - 1; x++) {
            for (int z = 0; z < gridToScore.getDepth() - 1; z++) {
                bumpiness += Math.abs(gridToScore.getHeightOfColumn3D(x, z) - gridToScore.getHeightOfColumn3D(x + 1, z));
                bumpiness += Math.abs(gridToScore.getHeightOfColumn3D(x, z) - gridToScore.getHeightOfColumn3D(x, z + 1));
            }
        }

        return heightWeight * heights +
                linesWeight * completedPlanes +
                holesWeight * holes +
                bumpinessWeight * bumpiness;
    }

    private Set<CurrentPiece3D> getPiecesPossibilities(CurrentPiece3D currentPiece) {
        return getPiecesPossibilities(currentPiece, grid);
    }
    
    private Set<CurrentPiece3D> getPiecesPossibilities(CurrentPiece3D currentPiece, Grid3D gridToUse) {
        Set<CurrentPiece3D> possibilities = new HashSet<>();

        // Generate rotations
        CurrentPiece3D workingPiece = currentPiece.copy();
        for (var axe : RotationAxis.values()) {
            for (int i = 0; i < 4; i++) {
                workingPiece.rotate3D(axe, gridToUse::checkCollision);
                workingPiece.setY(gridToUse.getHeight() - workingPiece.getHeight());
                possibilities.add(workingPiece.copy());
            }
        }

        // Generate translations
        Set<CurrentPiece3D> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int x = 0; x < gridToUse.getWidth(); x++) {
                for (int z = 0; z < gridToUse.getDepth(); z++) {
                    CurrentPiece3D translatedPiece = piece.copy();
                    translatedPiece.setX(x);
                    translatedPiece.setZ(z);
                    if (!gridToUse.checkCollision(translatedPiece)) {
                        newTranslations.add(translatedPiece);
                    }
                }
            }
        }
        possibilities = newTranslations;

        // Drop the pieces
        for (var piece : possibilities) {
            piece.setY(0);
            int y = 0;
            do {
                piece.setY(y++);
            } while (gridToUse.checkCollision(piece));
        }

        return possibilities;
    }

    // Helper class to store piece and its score
        private record PieceMoveScore(CurrentPiece3D piece, double score) {
    }

}
