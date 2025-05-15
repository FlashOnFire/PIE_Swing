package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Ai;
import fr.polytech.pie.model.CurrentPiece;

import java.util.HashSet;
import java.util.Set;

public class Ai2D extends Ai {
    private final Grid2D grid;

    public Ai2D(Grid2D grid) {
        super();
        this.grid = grid;
    }

    public Ai2D(Grid2D grid, double[] parameters) {
        super(parameters);
        this.grid = grid;
    }

    @Override
    public void makeMove(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece2D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }

        final var availablePossiblities = getPiecesPossibilities((CurrentPiece2D) currentPiece);

        // Rate each possibility
        double best = Double.NEGATIVE_INFINITY;
        CurrentPiece bestPiece = null;
        int bestHeight = 0;
        int bestLines = 0;
        int bestHoles = 0;
        int bestBumpiness = 0;
        for (var possibility : availablePossiblities) {
            // Sum each height of the columns
            grid.freezePiece(possibility);
            int heights = 0;
            for (int i = 0; i < grid.getWidth(); i++) {
                heights = heights + grid.getHeightOfColumn2D(i);
            }

            // Count completed lines
            int completedLines = grid.countFullLines();

            // Count holes
            int holes = 0;
            for (int i = 0; i < grid.getWidth(); i++) {
                boolean foundBlock = false;
                for (int j = grid.getHeight() - 1; j >= 0; j--) {
                    if (grid.getValue(i, j)) {
                        foundBlock = true;
                    } else if (foundBlock) {
                        holes++;
                    }
                }
            }

            // calculate bumpiness (to avoid having a big vertical hole)
            int bumpiness = 0;
            for (int i = 0; i < grid.getWidth() - 1; i++) {
                bumpiness += Math.abs(grid.getHeightOfColumn2D(i) - grid.getHeightOfColumn2D(i + 1));
            }

            double finalScore = heightWeight * heights +
                    linesWeight * completedLines +
                    holesWeight * holes +
                    bumpinessWeight * bumpiness;

            if (finalScore > best) {
                best = finalScore;
                bestPiece = possibility;
                bestHeight = heights;
                bestLines = completedLines;
                bestHoles = holes;
                bestBumpiness = bumpiness;
            }
            grid.removePiece(possibility);
        }
//        System.out.println("Best height: " + bestHeight +
//                ", lines: " + bestLines +
//                ", holes: " + bestHoles +
//                ", bumpiness: " + bestBumpiness +
//                ", score: " + best);
        grid.freezePiece(bestPiece != null ? bestPiece : currentPiece);
    }

    private Set<CurrentPiece2D> getPiecesPossibilities(CurrentPiece2D currentPiece) {
        Set<CurrentPiece2D> possibilities = new HashSet<>();

        // generate rotations
        CurrentPiece2D workingPiece = currentPiece.copy();
        for (int i = 0; i < 4; i++) {
            workingPiece.rotate2d(grid::checkCollision);
            possibilities.add(workingPiece.copy());
        }

        // generate translations
        Set<CurrentPiece2D> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int i = 0; i < grid.getWidth(); i++) {
                CurrentPiece2D translatedPiece = piece.copy();
                translatedPiece.setX(i);
                if (!grid.checkCollision(translatedPiece)) {
                    newTranslations.add(translatedPiece);
                }
            }
        }
        possibilities = newTranslations;

        // drops the pieces
        for (var piece : possibilities) {
            do {
                piece.setY(piece.getY() - 1);
            } while (!grid.checkCollision(piece));
            piece.setY(piece.getY() + 1);
        }

        return possibilities;
    }
}