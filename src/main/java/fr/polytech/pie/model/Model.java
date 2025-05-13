package fr.polytech.pie.model;

import fr.polytech.pie.Consts;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class Model extends Observable {
    Grid grid = new Grid(Consts.SIZE);
    CurrentPiece currentPiece;

    public boolean[] keys = new boolean[5];

    public Model() {
        currentPiece = PieceGenerator.generatePiece(new boolean[][]{}, grid.getSize(), grid.getSize());

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {
                    if (keys[0]) {
                        translateCurrentPiece(0, -1); // Down
                    }
                    if (keys[1]) {
                        translateCurrentPiece(0, 1); // Up
                    }
                    if (keys[2]) {
                        translateCurrentPiece(-1, 0); // Right
                    }
                    if (keys[3]) {
                        translateCurrentPiece(1, 0); // Left
                    }
                    if (keys[4]) {
                        rotateCurrentPiece(); // Rotate
                    }
                }, 0, 50, TimeUnit.MILLISECONDS
        );

        scheduler.scheduleAtFixedRate(
                () -> translateCurrentPiece(0, 1), 0, 200, TimeUnit.MILLISECONDS
        );

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
    }

    public void translateCurrentPiece(int dx, int dy) {
        int originalX = currentPiece.getX();
        int originalY = currentPiece.getY();

        currentPiece.translate(dx, dy);

        if (currentPiece.getX() < 0) {
            currentPiece.setX(0);
        }

        if (currentPiece.getX() + currentPiece.getWidth() > grid.getSize()) {
            currentPiece.setX(grid.getSize() - currentPiece.getWidth());
        }

        if (checkCollision()) {
            currentPiece.setX(originalX);
            currentPiece.setY(originalY);

            if (dy > 0) {
                grid.freezePiece(currentPiece);
                currentPiece = PieceGenerator.generatePiece(new boolean[][]{}, grid.getSize(), grid.getSize());
                System.out.println("New piece generated");
            }
        }

        setChanged();
        notifyObservers();
    }

    private boolean checkCollision() {
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (currentPiece.getPiece()[i][j]) {
                    int x = currentPiece.getX() + j;
                    int y = currentPiece.getY() + i;

                    if (x < 0 || x >= grid.getSize() || y < 0 || y >= grid.getSize()) {
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
        setChanged();
        notifyObservers();
    }

    public CurrentPiece getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(CurrentPiece currentPiece) {
        this.currentPiece = currentPiece;
        setChanged();
        notifyObservers();
    }

    public Grid getGrid() {
        return grid;
    }

    public void setKey(int key, boolean state) {
        if (key >= 0 && key < keys.length) {
            keys[key] = state;
        }
    }
}
