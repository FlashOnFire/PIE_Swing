package fr.polytech.pie.model;

import fr.polytech.pie.model.threeD.Grid3D;
import fr.polytech.pie.model.twoD.Grid2D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Ai {
    private final ExecutorService executorService = Executors.newWorkStealingPool();

    private final AIParameters params;
    private final Grid grid;

    public Ai(Grid grid, AIParameters parameters) {
        this.grid = grid;
        this.params = parameters;
    }

    @NotNull
    private List<Callable<PieceMoveScore>> getCallables(Piece nextPiece, Set<Piece> availablePossibilities) {
        List<Callable<PieceMoveScore>> tasks = new ArrayList<>();

        // Create tasks for evaluating each possible move
        for (Piece possibility : availablePossibilities) {
            tasks.add(() -> {
                Grid threadLocalGrid = grid.copy();
                double bestScore = Double.NEGATIVE_INFINITY;

                threadLocalGrid.freezePiece(possibility);
                Set<Piece> nextPiecePossibilities = threadLocalGrid.getPiecesPossibilities(nextPiece);

                for (Piece nextPiecePossibility : nextPiecePossibilities) {
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

        return params.heightWeight() * heights +
                params.linesWeight() * completedLines +
                params.holesWeight() * holes +
                params.bumpinessWeight() * bumpiness;
    }

    private int getBumpiness(Grid gridToScore) {
        int bumpiness = 0;

        if (gridToScore instanceof Grid3D grid3D) {
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
        if (gridToScore instanceof Grid3D grid3D) {
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


    public void makeMove(Piece piece, Piece nextPiece) {
        final var availablePossibilities = grid.getPiecesPossibilities(piece);

        double best = Double.NEGATIVE_INFINITY;
        Piece bestPiece = null;

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
        }

        // Fallback to the current piece if an error occurs
        grid.freezePiece(bestPiece != null ? bestPiece : piece);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    private record PieceMoveScore(Piece piece, double score) {
    }
}
