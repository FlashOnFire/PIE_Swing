package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Ai;
import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;

import java.util.HashSet;
import java.util.Set;

public class Ai2D implements Ai {
    protected double heightWeight = -0.7303205229567257;
    protected double linesWeight = 0.6082323862482821;
    protected double bumpinessWeight = -0.22463833194827965;
    protected double holesWeight = -0.21499515782089093;
    private final Grid2D grid;

    public Ai2D(Grid2D grid) {
        super();
        this.grid = grid;
    }

    public Ai2D(Grid2D grid, double[] parameters) {
        this.grid = grid;
        this.heightWeight = parameters[0];
        this.linesWeight = parameters[1];
        this.bumpinessWeight = parameters[2];
        this.holesWeight = parameters[3];
    }

    @Override
    public void makeMove(CurrentPiece currentPiece, CurrentPiece nextPiece) {
        if (!(currentPiece instanceof CurrentPiece2D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }

        final var availablePossibilities = getPiecesPossibilities((CurrentPiece2D) currentPiece);

        // Rate each possibility
        double best = Double.NEGATIVE_INFINITY;
        CurrentPiece bestPiece = null;
        for (var possibility : availablePossibilities) {
            grid.freezePiece(possibility);
            final var nextPiecePossibilities = getPiecesPossibilities((CurrentPiece2D) nextPiece);
            for (var nextPiecePossibility : nextPiecePossibilities) {
                grid.freezePiece(nextPiecePossibility);
                double finalScore = getRate();
                if (finalScore > best) {
                    best = finalScore;
                    bestPiece = possibility;
                }
                grid.removePiece(nextPiecePossibility);
            }
            grid.removePiece(possibility);
        }
        grid.freezePiece(bestPiece != null ? bestPiece : currentPiece);
    }


    private double getRate() {
        int heights = 0;
        for (int i = 0; i < grid.getWidth(); i++) {
            heights = heights + grid.getHeightOfColumn2D(i);
        }

        // Count completed lines
        int completedLines = grid.clearFullLines(true);

        // Count holes
        int holes = 0;
        for (int i = 0; i < grid.getWidth(); i++) {
            boolean foundBlock = false;
            for (int j = grid.getHeight() - 1; j >= 0; j--) {
                if (grid.getValue(i, j) != Piece.Empty) {
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

        return heightWeight * heights +
                linesWeight * completedLines +
                holesWeight * holes +
                bumpinessWeight * bumpiness;
    }

    private Set<CurrentPiece2D> getPiecesPossibilities(CurrentPiece2D currentPiece) {
        Set<CurrentPiece2D> possibilities = new HashSet<>();

        // generate rotations
        CurrentPiece2D workingPiece = currentPiece.clone();
        for (int i = 0; i < 4; i++) {
            workingPiece.rotate2d(_ -> false);
            workingPiece.setY(grid.getHeight() - workingPiece.getHeight());
            possibilities.add(workingPiece.clone());
        }

        // generate translations
        Set<CurrentPiece2D> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int i = 0; i < grid.getWidth(); i++) {
                CurrentPiece2D translatedPiece = piece.clone();
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