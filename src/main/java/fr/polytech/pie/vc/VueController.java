package fr.polytech.pie.vc;

import fr.polytech.pie.model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class VueController implements Observer {
    private final ScheduledExecutorService scheduler;

    private final Model model;
    private final JPanel gridPanel = new JPanel();
    private Renderer currentRenderer;
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final JPanel menuPanel = new JPanel();
    private final JButton play2DButton = new JButton("Play 2D");
    private final JButton play3DButton = new JButton("Play 3D");

    public VueController(ScheduledExecutorService scheduler, Model m) {
        this.scheduler = scheduler;
        this.model = m;
        m.addObserver(this);

        this.currentRenderer = new MenuRenderer(this);
        this.currentRenderer.initialize();
    }

    void startGame(boolean is3D) {
        //model.setRenderingMode(is3D);
        model.resetGame();
        if (is3D) {
            switchRenderer(RendererType.GAME_3D);
        } else {
            switchRenderer(RendererType.GAME_2D);
        }

        currentRenderer.loop();

        switchRenderer(RendererType.MENU);
    }

    private void switchRenderer(RendererType type) {
        if (currentRenderer != null) {
            currentRenderer.cleanup();
        }

        switch (type) {
            case MENU -> currentRenderer = new MenuRenderer(this);
            case GAME_2D -> currentRenderer = new Renderer2D(this);
            case GAME_3D -> currentRenderer = new Renderer3D(scheduler, model);
        }

        currentRenderer.initialize();
        update(model, null);
    }

    @Override
    public void update(Observable o, Object arg) {
        Model model = (Model) o;
        try {
            if (currentRenderer != null) {
                currentRenderer.update(model.getGrid(), model.getCurrentPiece(), model.getScore());
            }
        } catch (Exception e) {
            Logger.getLogger(VueController.class.getName()).log(Level.SEVERE, "Error updating VueController", e);
        }
    }

    public Model getModel() {
        return model;
    }
}
