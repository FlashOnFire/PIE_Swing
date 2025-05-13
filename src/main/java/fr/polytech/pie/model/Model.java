package fr.polytech.pie.model;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class Model extends Observable {
    private final Game game = new Game();
    public boolean[] keys = new boolean[5];

    public Model() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
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
        }, 0, 50, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> translateCurrentPiece(0, 1), 0, 200, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
    }

    public void translateCurrentPiece(int dx, int dy) {
        game.translateCurrentPiece(dx, dy);
        setChanged();
        notifyObservers();
    }

    public void rotateCurrentPiece() {
        game.rotateCurrentPiece();
        setChanged();
        notifyObservers();
    }

    public Grid getGrid() {
        return game.getGrid();
    }

    public void setKey(int key, boolean state) {
        if (key >= 0 && key < keys.length) {
            keys[key] = state;
        }
    }

    public CurrentPiece getCurrentPiece() {
        return game.getCurrentPiece();
    }
}
