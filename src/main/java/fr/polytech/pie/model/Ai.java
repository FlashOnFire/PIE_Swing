package fr.polytech.pie.model;

import java.util.HashSet;
import java.util.Set;

public class Ai {
    private final Grid grid;

    private double heightWeight = -0.5626037616361799;
    private double linesWeight = 0.7196847345645387;
    private double bumpinessWeight = -0.21226115740599324;
    private double holesWeight = -0.3470966598575362;

    public Ai(Grid grid) {
        this.grid = grid;
    }

    public Ai(Grid grid, double[] parameters) {
        this.grid = grid;
        if (parameters.length != 4) {
            throw new IllegalArgumentException("Parameters must be of length 4");
        }
        this.heightWeight = parameters[0];
        this.linesWeight = parameters[1];
        this.bumpinessWeight = parameters[2];
        this.holesWeight = parameters[3];
    }

    public void makeMove(CurrentPiece currentPiece) {
        final var availablePossiblities = getPiecesPossibilities(currentPiece);

        // Rate each possibility
        double best = Double.NEGATIVE_INFINITY;
        CurrentPiece bestPiece = null;
        for (var possibility : availablePossiblities) {
            // Sum each height of the columns
            grid.freezePiece(possibility);
            int heights = 0;
            if ((grid instanceof Grid3D)) {
                for (int i = 0; i < grid.getWidth(); i++) {
                    for (int j = 0; j < ((Grid3D) grid).getDepth(); j++) {
                        heights += ((Grid3D) grid).getHeightOfColumn3D(i, j);
                    }
                }
            } else if (grid instanceof Grid2D) {
                for (int i = 0; i < grid.getWidth(); i++) {
                    heights += ((Grid2D) grid).getHeightOfColumn2D(i);
                }
            }

            // Count completed lines
            int completedLines = grid.countFullLines();

            // Count holes
            int holes = 0;
            for (int i = 0; i < grid.getWidth(); i++) {
                boolean foundBlock = false;
                for (int j = 0; j < grid.getHeight(); j++) {
                    if (grid.getValue(i, j)) {
                        foundBlock = true;
                    } else if (foundBlock) {
                        holes++;
                    }
                }
            }

            // calculate bumpiness (to avoid having a big vertical hole)
            int bumpiness = 0;
            if (grid instanceof Grid3D) {
                for (int i = 0; i < grid.getWidth() - 1; i++) {
                    for (int j = 0; j < ((Grid3D) grid).getDepth() - 1; j++) {
                        bumpiness += Math.abs(((Grid3D) grid).getHeightOfColumn3D(i, j) - ((Grid3D) grid).getHeightOfColumn3D(i, j + 1));
                    }
                }
            } else if (grid instanceof Grid2D) {
                for (int i = 0; i < grid.getWidth() - 1; i++) {
                    bumpiness += Math.abs(((Grid2D) grid).getHeightOfColumn2D(i) - ((Grid2D) grid).getHeightOfColumn2D(i + 1));
                }
            }


            double finalScore = heightWeight * heights +
                    linesWeight * completedLines +
                    holesWeight * holes +
                    bumpinessWeight * bumpiness;

            if (finalScore > best) {
                best = finalScore;
                bestPiece = possibility;
            }
            grid.removePiece(possibility);
        }

        grid.freezePiece(bestPiece != null ? bestPiece : currentPiece);
    }

    private Set<CurrentPiece> getPiecesPossibilities(CurrentPiece currentPiece) {
        Set<CurrentPiece> possibilities = new HashSet<>();

        if (grid instanceof Grid2D) {
            // generate rotations
            CurrentPiece2D workingPiece = ((CurrentPiece2D) currentPiece).copy();
            for (int i = 0; i < 4; i++) {
                workingPiece.rotate2d(grid::checkCollision);
                possibilities.add(workingPiece.copy());
            }

            // generate translations
            Set<CurrentPiece> newTranslations = new HashSet<>();
            for (var piece : possibilities) {
                for (int i = 0; i < grid.getWidth(); i++) {
                    CurrentPiece translatedPiece = ((CurrentPiece2D) piece).copy();
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
                    piece.setY(piece.getY() + 1);
                } while (!grid.checkCollision(piece));
                piece.setY(piece.getY() - 1);
            }
        } else if (grid instanceof Grid3D) {
            // generate rotations
            CurrentPiece3D workingPiece = ((CurrentPiece3D) currentPiece).copy();
            for (var axis : RotationAxis.values()) {
                for (int i = 0; i < 4; i++) {
                    workingPiece.rotate3D(axis, grid::checkCollision);
                    possibilities.add(workingPiece.copy());
                }
            }


            // generate translations
            Set<CurrentPiece> newTranslations = new HashSet<>();
            for (var piece : possibilities) {
                for (int i = 0; i < grid.getWidth(); i++) {
                    for (int j = 0; j < ((Grid3D) grid).getDepth(); j++) {
                        CurrentPiece translatedPiece = ((CurrentPiece3D) piece).copy();
                        translatedPiece.setX(i);
                        ((CurrentPiece3D) translatedPiece).setZ(j);
                        if (!grid.checkCollision(translatedPiece)) {
                            newTranslations.add(translatedPiece);
                        }
                    }
                }
            }
            possibilities = newTranslations;

            // drops the pieces
            for (var piece : possibilities) {
                do {
                    piece.setY(piece.getY() + 1);
                } while (!grid.checkCollision(piece));
                piece.setY(piece.getY() - 1);
            }
        }
        return possibilities;
    }
}
