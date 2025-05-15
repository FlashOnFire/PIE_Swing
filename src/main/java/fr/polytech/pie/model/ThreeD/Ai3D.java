package fr.polytech.pie.model.ThreeD;

import fr.polytech.pie.model.Ai;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.RotationAxis;

import java.util.HashSet;
import java.util.Set;

public class Ai3D extends Ai {
    private Grid3D grid;

    public Ai3D(Grid3D grid) {
        super();
        this.grid = grid;
    }

    public Ai3D(Grid3D grid, double[] parameters) {
        super(parameters);
        this.grid = grid;
    }

    @Override
        public void makeMove(CurrentPiece currentPiece) {
            if (!(currentPiece instanceof CurrentPiece3D)) {
                throw new IllegalArgumentException("Ai3D can only handle CurrentPiece3D instances");
            }

            final var availablePossibilities = getPiecesPossibilities((CurrentPiece3D) currentPiece);

            // Rate each possibility
            double best = Double.NEGATIVE_INFINITY;
            CurrentPiece bestPiece = null;
            for (var possibility : availablePossibilities) {
                // Sum each height of the columns
                grid.freezePiece(possibility);
                int heights = 0;
                for (int x = 0; x < grid.getWidth(); x++) {
                    for (int z = 0; z < grid.getDepth(); z++) {
                        heights += grid.getHeightOfColumn3D(x, z);
                    }
                }

                // Count completed planes
                int completedPlanes = grid.countFullLines();

                // Count holes
                int holes = 0;
                for (int x = 0; x < grid.getWidth(); x++) {
                    for (int z = 0; z < grid.getDepth(); z++) {
                        boolean foundBlock = false;
                        for (int y = 0; y < grid.getHeight(); y++) {
                            int localX = x - possibility.getX();
                            int localY = y - possibility.getY();
                            int localZ = z - possibility.getZ();
                            boolean isPieceBlock = localX >= 0 && localX < possibility.getHeight()
                                    && localY >= 0 && localY < possibility.getWidth()
                                    && localZ >= 0 && localZ < possibility.getDepth()
                                    && grid.getValue(x, y, z);
                            if (isPieceBlock) {
                                foundBlock = true;
                            } else if (foundBlock && !grid.getValue(x, y, z)) {
                                holes++;
                            }
                        }
                    }
                }

                // Calculate bumpiness (to avoid having a big vertical hole)
                int bumpiness = 0;
                for (int x = 0; x < grid.getWidth() - 1; x++) {
                    for (int z = 0; z < grid.getDepth() - 1; z++) {
                        bumpiness += Math.abs(grid.getHeightOfColumn3D(x, z) - grid.getHeightOfColumn3D(x + 1, z));
                        bumpiness += Math.abs(grid.getHeightOfColumn3D(x, z) - grid.getHeightOfColumn3D(x, z + 1));
                    }
                }

                double finalScore = heightWeight * heights +
                        linesWeight * completedPlanes +
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

    private Set<CurrentPiece3D> getPiecesPossibilities(CurrentPiece3D currentPiece) {
        Set<CurrentPiece3D> possibilities = new HashSet<>();

        // Generate rotations
        CurrentPiece3D workingPiece = currentPiece.copy();
        for (var axe: RotationAxis.values()) {
            for (int i = 0; i < 4; i++) {
                workingPiece.rotate3D(axe, grid::checkCollision);
                possibilities.add(workingPiece.copy());
            }
        }

        // Generate translations
        Set<CurrentPiece3D> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int x = 0; x < grid.getWidth(); x++) {
                for (int z = 0; z < grid.getDepth(); z++) {
                    CurrentPiece3D translatedPiece = piece.copy();
                    translatedPiece.setX(x);
                    translatedPiece.setZ(z);
                    if (!grid.checkCollision(translatedPiece)) {
                        newTranslations.add(translatedPiece);
                    }
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