package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.Ai;
import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.RotationAxis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Ai3D implements Ai {
    // Optimized weights based on provided values
    private static final double HEIGHT_WEIGHT = -0.6500491536113875;
    private static final double LINES_WEIGHT = 0.5122503282774851;
    private static final double BUMPINESS_WEIGHT = -0.1763291765999144;
    private static final double HOLES_WEIGHT = -0.5328636979081272;
    
    private final Grid3D grid;
    private final ExecutorService executorService;

    public Ai3D(Grid3D grid) {
        this.grid = grid;
        this.executorService = Executors.newWorkStealingPool();
    }

    // Constructor for potential AI training purposes
    public Ai3D(Grid3D grid, double[] parameters) {
        this.grid = grid;
        this.executorService = Executors.newWorkStealingPool();
        // Parameters still accessible if needed for training
    }

    @Override
    public void makeMove(CurrentPiece currentPiece, CurrentPiece nextPiece) {
        if (!(currentPiece instanceof CurrentPiece3D)) {
            throw new IllegalArgumentException("Ai3D can only handle CurrentPiece3D instances");
        }

        Collection<CurrentPiece3D> possibilities = getPiecesPossibilities((CurrentPiece3D) currentPiece);
        if (possibilities.isEmpty()) {
            grid.freezePiece(currentPiece);
            return;
        }

        // Use CompletionService for better task management
        CompletionService<PieceMoveScore> completionService = new ExecutorCompletionService<>(executorService);
        AtomicReference<PieceMoveScore> bestMove = new AtomicReference<>(new PieceMoveScore(null, Double.NEGATIVE_INFINITY));
        int taskCount = 0;

        // Submit tasks
        for (CurrentPiece3D possibility : possibilities) {
            completionService.submit(() -> {
                Grid3D threadLocalGrid = (Grid3D) grid.copy();
                threadLocalGrid.freezePiece(possibility);
                double score = calculateScore(threadLocalGrid);
                return new PieceMoveScore(possibility, score);
            });
            taskCount++;
        }

        // Collect results
        try {
            for (int i = 0; i < taskCount; i++) {
                Future<PieceMoveScore> result = completionService.take();
                PieceMoveScore moveScore = result.get();
                
                if (moveScore.score > bestMove.get().score) {
                    bestMove.set(moveScore);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing moves: " + e.getMessage());
        }

        // Apply best move or fallback to current piece
        CurrentPiece bestPiece = bestMove.get().piece != null ? bestMove.get().piece : currentPiece;
        grid.freezePiece(bestPiece);
    }

    private double calculateScore(Grid3D gridToScore) {
        int height = calculateTotalHeight(gridToScore);
        int completedPlanes = gridToScore.clearFullLines(true);
        int holes = countHoles(gridToScore);
        int bumpiness = calculateBumpiness(gridToScore);

        return HEIGHT_WEIGHT * height +
               LINES_WEIGHT * completedPlanes +
               HOLES_WEIGHT * holes +
               BUMPINESS_WEIGHT * bumpiness;
    }
    
    private int calculateTotalHeight(Grid3D gridToScore) {
        int totalHeight = 0;
        final int width = gridToScore.getWidth();
        final int depth = gridToScore.getDepth();
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                totalHeight += gridToScore.getHeightOfColumn3D(x, z);
            }
        }
        return totalHeight;
    }
    
    private int countHoles(Grid3D gridToScore) {
        int holes = 0;
        final int width = gridToScore.getWidth();
        final int depth = gridToScore.getDepth();
        final int height = gridToScore.getHeight();
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                boolean foundBlock = false;
                for (int y = height - 1; y >= 0; y--) {
                    if (gridToScore.getValue(x, y, z) != Piece.Empty) {
                        foundBlock = true;
                    } else if (foundBlock) {
                        holes++;
                    }
                }
            }
        }
        return holes;
    }
    
    private int calculateBumpiness(Grid3D gridToScore) {
        int bumpiness = 0;
        final int width = gridToScore.getWidth();
        final int depth = gridToScore.getDepth();
        
        for (int x = 0; x < width - 1; x++) {
            for (int z = 0; z < depth - 1; z++) {
                bumpiness += Math.abs(gridToScore.getHeightOfColumn3D(x, z) - gridToScore.getHeightOfColumn3D(x + 1, z));
                bumpiness += Math.abs(gridToScore.getHeightOfColumn3D(x, z) - gridToScore.getHeightOfColumn3D(x, z + 1));
            }
        }
        return bumpiness;
    }

    private Collection<CurrentPiece3D> getPiecesPossibilities(CurrentPiece3D currentPiece) {
        List<CurrentPiece3D> results = new ArrayList<>();
        Set<String> uniquePositions = new HashSet<>();
        
        // Generate all possible piece orientations
        List<CurrentPiece3D> orientations = generateOrientations(currentPiece);
        
        // For each orientation, find all valid positions
        for (CurrentPiece3D orientation : orientations) {
            for (int x = 0; x < grid.getWidth(); x++) {
                for (int z = 0; z < grid.getDepth(); z++) {
                    CurrentPiece3D positioned = orientation.copy();
                    positioned.setX(x);
                    positioned.setZ(z);
                    
                    // Skip if invalid position
                    if (grid.checkCollision(positioned)) {
                        continue;
                    }
                    
                    // Drop the piece to its lowest valid position
                    dropPiece(positioned);
                    
                    // Use a string representation to detect duplicates
                    String posKey = getPiecePositionKey(positioned);
                    if (uniquePositions.add(posKey)) {
                        results.add(positioned);
                    }
                }
            }
        }
        
        return results;
    }
    
    private List<CurrentPiece3D> generateOrientations(CurrentPiece3D piece) {
        List<CurrentPiece3D> orientations = new ArrayList<>();
        Set<String> uniqueOrientations = new HashSet<>();
        
        for (RotationAxis axis : RotationAxis.values()) {
            CurrentPiece3D rotated = piece.copy();
            for (int i = 0; i < 4; i++) {
                rotated.rotate3D(axis, grid::checkCollision);
                
                // Save only unique orientations
                String orientationKey = getPieceOrientationKey(rotated);
                if (uniqueOrientations.add(orientationKey)) {
                    orientations.add(rotated.copy());
                }
            }
        }
        
        return orientations;
    }
    
    private void dropPiece(CurrentPiece3D piece) {
        // Reset piece to top position
        piece.setY(grid.getHeight() - piece.getHeight());
        
        // Drop until collision
        while (!grid.checkCollision(piece)) {
            piece.setY(piece.getY() - 1);
        }
        
        // Move back up one position to be valid
        piece.setY(piece.getY() + 1);
    }
    
    private String getPiecePositionKey(CurrentPiece3D piece) {
        // Create a unique identifier for the piece's position and orientation
        return piece.getX() + "," + piece.getY() + "," + piece.getZ() + "," + getPieceOrientationKey(piece);
    }
    
    private String getPieceOrientationKey(CurrentPiece3D piece) {
        // For simplicity, use the piece's blocks as orientation identifier
        StringBuilder key = new StringBuilder();
        for (Piece[][] layer : piece.getPiece3d()) {
            for (int i = 0; i < layer.length; i++) {
                for (int j = 0; j < layer[i].length; j++) {
                    if (layer[i][j] != Piece.Empty) {
                        key.append(i).append(",").append(j).append(";");
                    }
                }
            }
        }
        return key.toString();
    }
    
    private static class PieceMoveScore {
        final CurrentPiece3D piece;
        final double score;
        
        PieceMoveScore(CurrentPiece3D piece, double score) {
            this.piece = piece;
            this.score = score;
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
