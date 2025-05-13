package fr.polytech.pie.model;

import fr.polytech.pie.Consts;

public class Game{
    private Grid grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);
    private CurrentPiece currentPiece;

    public Game() {
        this.currentPiece = PieceGenerator.generatePiece(grid.getWidth());
    }

    public Grid getGrid() {
        return grid;
    }

    public CurrentPiece getCurrentPiece() {
        return currentPiece;
    }

    public void translateCurrentPiece(int dx, int dy) {
        int originalX = currentPiece.getX();
        int originalY = currentPiece.getY();

        currentPiece.translate(dx, dy);

        if (currentPiece.getX() < 0) {
            currentPiece.setX(0);
        }

        if (currentPiece.getX() + currentPiece.getWidth() > grid.getWidth()) {
            currentPiece.setX(grid.getWidth() - currentPiece.getWidth());
        }

        if (checkCollision()) {
            currentPiece.setX(originalX);
            currentPiece.setY(originalY);

            if (dy > 0) {
                grid.freezePiece(currentPiece);
                currentPiece = PieceGenerator.generatePiece(grid.getWidth());

                if (checkCollision()) {
                    System.out.println("Fin du jeu !");
                    resetGame();
                }
            }
        }
    }

    public void resetGame() {
        grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);
        currentPiece = PieceGenerator.generatePiece(grid.getWidth());
    }

    public boolean checkCollision() {
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (currentPiece.getPiece()[i][j]) {
                    int x = currentPiece.getX() + j;
                    int y = currentPiece.getY() + i;

                    if (x < 0 || x >= grid.getWidth() || y < 0 || y >= grid.getHeight()) {
                        return true;
                    }

                    if (grid.getValue(x, y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void rotateCurrentPiece() {
        currentPiece.rotate((currentPiece) -> grid.checkCollision(currentPiece));

    }



}
