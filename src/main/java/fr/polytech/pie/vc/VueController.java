package fr.polytech.pie.vc;

import fr.polytech.pie.Main;
import fr.polytech.pie.model.Game;
import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.render.threedee.Renderer3D;
import fr.polytech.pie.vc.render.twodee.Renderer2D;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class VueController implements Observer {

    private final Model model;
    private Renderer currentRenderer;

    public VueController(Model m) {
        this.model = m;

        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        m.addObserver(this);

        switchRenderer(RendererType.MENU);
    }

    void startGame(boolean is3D) {
        model.startGame(is3D);

        if (is3D) {
            switchRenderer(RendererType.GAME_3D);
        } else {
            switchRenderer(RendererType.GAME_2D);
        }
    }

    void stopGame() {
        model.stopGame();
        switchRenderer(RendererType.MENU);
    }

    private void switchRenderer(RendererType type) {
        if (currentRenderer != null) {
            currentRenderer.cleanup();
        }

        switch (type) {
            case MENU -> currentRenderer = new MenuRenderer(model.getHighScore(false), model.getHighScore(true), this);
            case GAME_2D -> currentRenderer = new Renderer2D(this);
            case GAME_3D -> currentRenderer = new Renderer3D(this);
        }

        currentRenderer.initialize();
        if (type != RendererType.MENU) {
            update(model, null);
        }
    }

    public void loop() {
        loop:
        while (true) {
            switch (currentRenderer.loop()) {
                case CONTINUE -> {}
                case SHOW_MENU -> stopGame();
                case START_GAME_2D -> startGame(false);
                case START_GAME_3D -> startGame(true);
                case QUIT -> {
                    break loop;
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Model model = (Model) o;
        try {
            if (currentRenderer != null) {
                if (model.getGame().is3D() && !(currentRenderer instanceof Renderer3D) || !model.getGame().is3D() && !(currentRenderer instanceof Renderer2D)) {
                    return;
                }

                Game game = model.getGame();

                currentRenderer.update(
                        game.getGrid(),
                        game.getCurrentPiece(),
                        game.getNextPiece(),
                        game.getScore(),
                        game.isGameOver()
                );
            }
        } catch (Exception e) {
            Logger.getLogger(VueController.class.getName()).log(Level.SEVERE, "Error updating VueController", e);
        }
    }

    public Model getModel() {
        return model;
    }

    public void cleanup() {
        if (currentRenderer != null) {
            currentRenderer.cleanup();
        }
        currentRenderer = null;
    }

    public void setDifficulty(int difficulty) {
        model.setDifficulty(difficulty);
    }

    public int getDifficulty() {
        return model.getDifficulty();
    }
}
