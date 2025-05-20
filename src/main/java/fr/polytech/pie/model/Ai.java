package fr.polytech.pie.model;

import fr.polytech.pie.model.threeD.Grid3D;
import fr.polytech.pie.model.twoD.Grid2D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Ai {
    static protected double heightWeight;
    static protected double linesWeight;
    static protected double bumpinessWeight;
    static protected double holesWeight;
    private final Grid grid;
    private final ExecutorService executorService;
    private final int availableProcessors;
    private final boolean is3D;

    public Ai(Grid grid, boolean is3D) {
        this.grid = grid;
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(availableProcessors);
        this.is3D = is3D;
        if (is3D) {
            heightWeight = -0.6500491536113875;
            linesWeight = 0.5122503282774851;
            bumpinessWeight = -0.1763291765999144;
            holesWeight = -0.5328636979081272;
        } else {
            heightWeight = -0.7303205229567257;
            linesWeight = 0.6082323862482821;
            bumpinessWeight = -0.22463833194827965;
            holesWeight = -0.21499515782089093;
        }
    }

    public Ai(Grid grid, double[] parameters, boolean is3D) {
        this.grid = grid;
        this.is3D = is3D;
        heightWeight = parameters[0];
        linesWeight = parameters[1];
        bumpinessWeight = parameters[2];
        holesWeight = parameters[3];
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(availableProcessors);
    }

    @NotNull
    private List<Callable<PieceMoveScore>> getCallables(CurrentPiece nextPiece, Set<CurrentPiece> availablePossibilities) {
        List<Callable<PieceMoveScore>> tasks = new ArrayList<>();

        // Create tasks for evaluating each possible move
        for (CurrentPiece possibility : availablePossibilities) {
            tasks.add(() -> {
                Grid threadLocalGrid = grid.copy();
                double bestScore = Double.NEGATIVE_INFINITY;

                threadLocalGrid.freezePiece(possibility);
                Set<CurrentPiece> nextPiecePossibilities = threadLocalGrid.getPiecesPossibilities(nextPiece);

                for (CurrentPiece nextPiecePossibility : nextPiecePossibilities) {
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

    private double getScore(Grid gridToScore) {
        int heights = getHeights(gridToScore);
        int completedLines = gridToScore.clearFullLines(true);
        int holes = gridToScore.getHoles();
        int bumpiness = getBumpiness(gridToScore);

        return heightWeight * heights +
                linesWeight * completedLines +
                holesWeight * holes +
                bumpinessWeight * bumpiness;
    }

    private int getBumpiness(Grid gridToScore) {
        int bumpiness = 0;
        if (is3D && gridToScore instanceof Grid3D grid3D) {
            for (int x = 0; x < grid3D.getWidth(); x++) {
                for (int z = 0; z < grid3D.getDepth(); z++) {
                    if (x < grid3D.getWidth() - 1) {
                        bumpiness += Math.abs(grid3D.getHeightOfColumn3D(x, z) - grid3D.getHeightOfColumn3D(x + 1, z));
                    }
                    if (z < grid3D.getDepth() - 1) {
                        bumpiness += Math.abs(grid3D.getHeightOfColumn3D(x, z) - grid3D.getHeightOfColumn3D(x, z + 1));
                    }
                }
            }
        } else if (gridToScore instanceof Grid2D grid2D) {
            for (int i = 0; i < gridToScore.getWidth() - 1; i++) {
                bumpiness += Math.abs(grid2D.getHeightOfColumn2D(i) - grid2D.getHeightOfColumn2D(i + 1));
            }
        }
        return bumpiness;
    }

    private int getHeights(Grid gridToScore) {
        int heights = 0;
        if (is3D && gridToScore instanceof Grid3D grid3D) {
            for (int i = 0; i < grid3D.getWidth(); i++) {
                for (int j = 0; j < grid3D.getDepth(); j++) {
                    heights += grid3D.getHeightOfColumn3D(i, j);
                }
            }
        } else if (gridToScore instanceof Grid2D grid2D) {
            for (int i = 0; i < grid2D.getWidth(); i++) {
                heights += grid2D.getHeightOfColumn2D(i);
            }
        }
        return heights;
    }


    public void makeMove(CurrentPiece currentPiece, CurrentPiece nextPiece) {
        final var availablePossibilities = grid.getPiecesPossibilities(currentPiece);

        double best = Double.NEGATIVE_INFINITY;
        CurrentPiece bestPiece = null;

        try {
            List<Callable<PieceMoveScore>> tasks = getCallables(nextPiece, availablePossibilities);

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
            // Fallback to the current piece if error occurs
            bestPiece = currentPiece;
        }

        grid.freezePiece(bestPiece != null ? bestPiece : currentPiece);
    }

    private record PieceMoveScore(CurrentPiece piece, double score) {
    }

}
