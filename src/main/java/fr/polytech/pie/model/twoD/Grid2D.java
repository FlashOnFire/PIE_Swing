package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.PieceColor;
import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Position;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Grid2D extends Grid {
    private final PieceColor[][] grid;
    private final int[] heightCache;
    private final int[] holesCache;

    public Grid2D(Position size) {
        super(size);
        this.grid = new PieceColor[size.getY()][size.getX()];
        this.heightCache = new int[size.getX()];
        this.holesCache = new int[size.getX()];

        for (int y = 0; y < size.getY(); y++) {
            Arrays.fill(grid[y], PieceColor.Empty);
        }
    }

    @Override
    public PieceColor getValue(Position position) {
        if (isOutOfBounds(position)) {
            return PieceColor.Empty;
        }
        return grid[position.getY()][position.getX()];
    }

    public void setValue(Position position, PieceColor value) {
        if (!isOutOfBounds(position)) {
            PieceColor oldValue = grid[position.getY()][position.getX()];
            grid[position.getY()][position.getX()] = value;

            if ((oldValue == PieceColor.Empty && value != PieceColor.Empty) ||
                (oldValue != PieceColor.Empty && value == PieceColor.Empty)) {
                updateCache(position.getX());
            }
        }
    }

    private void updateCache(int x) {
        int height = 0;
        int holes = 0;
        boolean foundBlock = false;
        boolean inHole = false;

        for (int y = this.size.getY() - 1; y >= 0; y--) {
            if (grid[y][x] != PieceColor.Empty) {
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
        for (int x = 0; x < size.getX(); x++) {
            updateCache(x);
        }
    }

    @Override
    public void freezePiece(Piece piece) {
        if (!(piece instanceof Piece2D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + piece.getClass().getName());
        }

        PieceColor[][] pieceColor = ((Piece2D) piece).getPiece2d();
        for (int i = 0; i < piece.getWidth(); i++) {
            for (int j = 0; j < piece.getHeight(); j++) {
                if (pieceColor[j][i] != PieceColor.Empty) {
                    var pos = new Position(new int[]{i, j});
                    pos.add(piece.getPosition());
                    setValue(pos, piece.getColor());
                }
            }
        }
    }

    @Override
    public void removePiece(Piece piece) {
        for (int i = 0; i < piece.getWidth(); i++) {
            for (int j = 0; j < piece.getHeight(); j++) {
                if (((Piece2D) piece).getPiece2d()[j][i] != PieceColor.Empty) {
                    var pos = new Position(new int[]{i, j});
                    pos.add(piece.getPosition());
                    setValue(pos, PieceColor.Empty);
                }
            }
        }
    }

    @Override
    public boolean checkCollision(Piece piece) {
        if (!(piece instanceof Piece2D piece2D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + piece.getClass().getName());
        }

        PieceColor[][] pieceColor = piece2D.getPiece2d();
        for (int i = 0; i < piece.getWidth(); i++) {
            for (int j = 0; j < piece.getHeight(); j++) {
                if (pieceColor[j][i] != PieceColor.Empty) {
                    var pos = new Position(new int[]{i, j});
                    pos.add(piece.getPosition());

                    if (isOutOfBounds(pos)) {
                        return true;
                    }

                    if (getValue(pos) != PieceColor.Empty) {
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
        Grid2D copy = new Grid2D(size);
        for (int y = 0; y < size.getY(); y++) {
            System.arraycopy(grid[y], 0, copy.grid[y], 0, size.getX());
        }

        System.arraycopy(heightCache, 0, copy.heightCache, 0, size.getX());
        System.arraycopy(holesCache, 0, copy.holesCache, 0, size.getX());

        return copy;
    }

    @Override
    public int getHoles() {
        int totalHoles = 0;
        for (int x = 0; x < size.getX(); x++) {
            totalHoles += holesCache[x];
        }
        return totalHoles;
    }

    @Override
    public int clearFullLines(boolean dry) {
        int linesCleared = 0;

        for (int y = 0; y < size.getY(); y++) {
            boolean fullLine = true;
            for (int x = 0; x < size.getX(); x++) {
                if (grid[y][x] == PieceColor.Empty) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCleared++;
                if (!dry) {
                    for (int i = y; i < size.getY() - 1; i++) {
                        System.arraycopy(grid[i + 1], 0, grid[i], 0, size.getX());
                    }
                    y--;
                    for (int x = 0; x < size.getX(); x++) {
                        grid[size.getY() - 1][x] = PieceColor.Empty;
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
    public Set<Piece> getPiecesPossibilities(Piece currentPiece) {
        if (!(currentPiece instanceof Piece2D piece2D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }

        Set<Piece> possibilities = new HashSet<>();

        // generate rotations
        Piece2D workingPiece = piece2D.clone();
        for (int i = 0; i < 4; i++) {
            workingPiece.rotate2d(_ -> false);
            workingPiece.getPosition().setY(getHeight() - workingPiece.getHeight());
            possibilities.add(workingPiece.clone());
        }

        // generate translations
        Set<Piece> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int i = 0; i < size.getX(); i++) {
                Piece translatedPiece = piece.clone();
                translatedPiece.getPosition().setX(i);
                if (!checkCollision(translatedPiece)) {
                    newTranslations.add(translatedPiece);
                }
            }
        }
        possibilities = newTranslations;

        // drop the pieces
        for (var piece : possibilities) {
            do {
                piece.getPosition().setY(piece.getPosition().getY() - 1);
            } while (!checkCollision(piece));
            piece.getPosition().setY(piece.getPosition().getY() + 1);
        }

        return possibilities;
    }
}