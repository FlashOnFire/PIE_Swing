package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Grid2D extends Grid {
    private final Piece[][] grid;
    private final int[] heightCache;
    private final int[] holesCache;

    public Grid2D(int width, int height) {
        super(width, height);
        this.grid = new Piece[height][width];
        this.heightCache = new int[width];
        this.holesCache = new int[width];

        for (int y = 0; y < height; y++) {
            Arrays.fill(grid[y], Piece.Empty);
        }
    }

    @Override
    public Piece getValue(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Piece.Empty;
        }
        return grid[y][x];
    }

    public void setValue(int x, int y, Piece value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            Piece oldValue = grid[y][x];
            grid[y][x] = value;

            if ((oldValue == Piece.Empty && value != Piece.Empty) ||
                (oldValue != Piece.Empty && value == Piece.Empty)) {
                updateCache(x);
            }
        }
    }

    private void updateCache(int x) {
        int height = 0;
        int holes = 0;
        boolean foundBlock = false;
        boolean inHole = false;

        for (int y = this.height - 1; y >= 0; y--) {
            if (grid[y][x] != Piece.Empty) {
                if (!foundBlock) {
                    height = y + 1;
                }
                foundBlock = true;
                inHole = false;
            } else if (foundBlock) {
                if (!inHole) {
                    holes++;
                    inHole = true;
                }
            }
        }

        heightCache[x] = height;
        holesCache[x] = holes;
    }

    private void recalculateAllCaches() {
        for (int x = 0; x < width; x++) {
            updateCache(x);
        }
    }

    @Override
    public void freezePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece2D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + currentPiece.getClass().getName());
        }

        Piece[][] piece = ((CurrentPiece2D) currentPiece).getPiece2d();
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (piece[j][i] != Piece.Empty) {
                    int x = currentPiece.getX() + i;
                    int y = currentPiece.getY() + j;
                    setValue(x, y, currentPiece.getColor());
                }
            }
        }
    }

    @Override
    public void removePiece(CurrentPiece currentPiece) {
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (((CurrentPiece2D) currentPiece).getPiece2d()[j][i] != Piece.Empty) {
                    int x = currentPiece.getX() + i;
                    int y = currentPiece.getY() + j;
                    setValue(x, y, Piece.Empty);
                }
            }
        }
    }

    @Override
    public boolean checkCollision(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece2D piece2D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + currentPiece.getClass().getName());
        }

        Piece[][] piece = piece2D.getPiece2d();
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (piece[j][i] != Piece.Empty) {
                    int x = currentPiece.getX() + i;
                    int y = currentPiece.getY() + j;

                    if (x < 0 || x >= width || y < 0 || y >= height) {
                        return true;
                    }

                    if (getValue(x, y) != Piece.Empty) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int clearFullLines() {
        int linesCleared = clearFullLines(false);
        if (linesCleared > 0) {
            recalculateAllCaches();
        }
        return linesCleared;
    }

    @Override
    public Grid copy() {
        Grid2D copy = new Grid2D(width, height);
        for (int y = 0; y < height; y++) {
            System.arraycopy(grid[y], 0, copy.grid[y], 0, width);
        }

        System.arraycopy(heightCache, 0, copy.heightCache, 0, width);
        System.arraycopy(holesCache, 0, copy.holesCache, 0, width);

        return copy;
    }

    @Override
    public int getHoles() {
        int totalHoles = 0;
        for (int x = 0; x < width; x++) {
            totalHoles += holesCache[x];
        }
        return totalHoles;
    }

    @Override
    public int clearFullLines(boolean dry) {
        int linesCleared = 0;

        for (int y = 0; y < height; y++) {
            boolean fullLine = true;
            for (int x = 0; x < width; x++) {
                if (grid[y][x] == Piece.Empty) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCleared++;
                if (!dry) {
                    for (int i = y; i < height - 1; i++) {
                        System.arraycopy(grid[i + 1], 0, grid[i], 0, width);
                    }
                    y--;
                    for (int x = 0; x < width; x++) {
                        grid[height - 1][x] = Piece.Empty;
                    }
                }
            }
        }

        return linesCleared;
    }

    public int getHeightOfColumn2D(int x) {
        return heightCache[x];
    }

    @Override
    public Set<CurrentPiece> getPiecesPossibilities(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece2D currentPiece2D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }
        Set<CurrentPiece> possibilities = new HashSet<>();

        // generate rotations
        CurrentPiece2D workingPiece = currentPiece2D.clone();
        for (int i = 0; i < 4; i++) {
            workingPiece.rotate2d(_ -> false);
            workingPiece.setY(getHeight() - workingPiece.getHeight());
            possibilities.add(workingPiece.clone());
        }

        // generate translations
        Set<CurrentPiece> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int i = 0; i < getWidth(); i++) {
                CurrentPiece translatedPiece = piece.clone();
                translatedPiece.setX(i);
                if (!checkCollision(translatedPiece)) {
                    newTranslations.add(translatedPiece);
                }
            }
        }
        possibilities = newTranslations;

        // drops the pieces
        for (var piece : possibilities) {
            do {
                piece.setY(piece.getY() - 1);
            } while (!checkCollision(piece));
            piece.setY(piece.getY() + 1);
        }

        return possibilities;
    }
}