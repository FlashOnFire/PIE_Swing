package fr.polytech.pie.model;

import java.util.Observable;
import fr.polytech.pie.Consts;

@SuppressWarnings("deprecation")
public class M extends Observable {
    private int x = 0;
    private int y = 0;

    public M() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setX(x + 1);
                setY(y + 1);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        while (x < 0) {
            x += Consts.SIZE;
        }
        x = x % Consts.SIZE;

        this.x = x;
        setChanged();
        notifyObservers();
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        while (y < 0) {
            y += Consts.SIZE;
        }
        y = y % Consts.SIZE;

        this.y = y;
        setChanged();
        notifyObservers();
    }
}
