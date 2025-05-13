package fr.polytech.pie.model;

import fr.polytech.pie.Consts;

import java.util.Observable;

@SuppressWarnings("deprecation")
public class Model extends Observable {
    Grid grid = new Grid(Consts.SIZE);
    CurrentPiece currentPiece;

    public boolean[] keys = new boolean[5];

    public Model() {
        currentPiece = PieceGenerator.generatePiece(new boolean[][]{}, grid.getSize(), grid.getSize());

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
            }
        });
        t.setDaemon(true);
        t.start();

        Thread gravityThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                translateCurrentPiece(0, 1);
            }
        });
        gravityThread.setDaemon(true);
        gravityThread.start();
    }

    public void translateCurrentPiece(int dx, int dy) {
        int orginalY = currentPiece.getY();
        currentPiece.translate(dx, dy);

        if (currentPiece.getX() < 0) {
            currentPiece.setX(0);
        }

        if (currentPiece.getX() + currentPiece.getWidth() > grid.getSize()) {
            currentPiece.setX(grid.getSize() - currentPiece.getWidth());
        }

        if (currentPiece.getY() + currentPiece.getHeight() > grid.getSize()) {
            currentPiece.setY(orginalY);
            grid.freezePiece(currentPiece);
            currentPiece = PieceGenerator.generatePiece(new boolean[][]{}, grid.getSize(), grid.getSize());
            System.out.println("New piece generated");
        }

        if (currentPiece.getY() < 0) {

        }

        setChanged();
        notifyObservers();
    }

    public void rotateCurrentPiece() {
        currentPiece.rotate();
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
