package fr.polytech.pie.vc;

import fr.polytech.pie.Main;
import fr.polytech.pie.model.Game;
import fr.polytech.pie.model.Model;
import org.lwjgl.glfw.GLFWErrorCallback;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("deprecation")
public class VueController implements Observer {

    private final Model model;
    private Renderer currentRenderer;

    public VueController(Model m) {
        this.model = m;

        GLFWErrorCallback.createPrint(System.err).set();
        // This is called from the main thread (since the call path is either from VueController constructor or loop() method)
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Use the Swing event-dispatching thread for the UI
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    // Set the look and feel to the system's native look and feel
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
        model.resetGame();
        if (is3D) {
            model.changeRenderingMode(true);
            switchRenderer(RendererType.GAME_3D);
        } else {
            model.changeRenderingMode(false);
            switchRenderer(RendererType.GAME_2D);
        }
        model.startScheduler();
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
            case MENU -> currentRenderer = new MenuRenderer();
            case GAME_2D -> currentRenderer = new Renderer2D(this);
            case GAME_3D -> currentRenderer = new Renderer3D(model);
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
                case CONTINUE -> {
                    // Continue the game loop
                }
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
                        game.getScore());
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


        // Cleanup GLFW
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).close();
    }
}
