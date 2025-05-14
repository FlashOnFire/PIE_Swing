package fr.polytech.pie.model;

import java.util.HashSet;
import java.util.Set;

public class Ai {
    private Grid grid;

    public Ai(Grid grid) {
        this.grid = grid;
    }

    public void makeMove(CurrentPiece currentPiece) {
        final var availablePossiblities = getPiecesPossibilities(currentPiece);

        //Rate each possibility
        int best = Integer.MIN_VALUE;
        CurrentPiece bestPiece = null;
        for (var possibility : availablePossiblities) {
            //Sum each height of the columns
            grid.freezePiece(possibility);
            int heights = 0;
            for (int i = 0; i < grid.getWidth(); i++) {
                heights = heights + grid.getHeightOfColumn(i);
            }
            System.out.println("height: " + heights);

            //Count completed lines
            int completedLines = 0;
            for (int i = 0; i < grid.getHeight(); i++) {
                for (int j = 0; j < grid.getWidth(); j++) {
                    if (grid.getValue(j, i)) {
                        completedLines++;
                    }
                }
            }
            System.out.println("completed lines: " + completedLines);

            //Count holes
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
            System.out.println("holes: " + holes);

            //calculate bumpiness (to avoid having a big vertical hole)
            int bumpiness = 0;
            for (int i = 0; i < grid.getWidth() - 1; i++) {
                bumpiness += Math.abs(grid.getHeightOfColumn(i) - grid.getHeightOfColumn(i + 1));
            }

            double finalScore = (-0.510066) * heights + (0.760666) * completedLines + (-0.35663) * holes + (-0.184483) * bumpiness;
            if (finalScore > best) {
                best = (int) finalScore;
                bestPiece = possibility;
            }
            grid.removePiece(possibility);
        }

        grid.freezePiece(bestPiece);
    }

    private Set<CurrentPiece> getPiecesPossibilities(CurrentPiece currentPiece) {
        Set<CurrentPiece> possibilities = new HashSet<>();

        //generate rotations
        for (int i = 0; i < 4; i++) {
            if (currentPiece instanceof CurrentPiece2D) {
                ((CurrentPiece2D) currentPiece).rotate2d(e -> grid.checkCollision(e));
                possibilities.add(((CurrentPiece2D) currentPiece).copy());
            }
        }

        //generate translations
        Set<CurrentPiece> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int i = 0; i < grid.getWidth(); i++) {
                piece.setX(i);
                if (!grid.checkCollision(piece)) {
                    newTranslations.add(((CurrentPiece2D) currentPiece).copy());
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

        return possibilities;
    }
}
