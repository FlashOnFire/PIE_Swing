package fr.polytech.pie.vc;

import fr.polytech.pie.Main;
import fr.polytech.pie.model.Model;
import org.lwjgl.glfw.GLFWErrorCallback;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("deprecation")
public class VueController implements Observer {
    private final ScheduledExecutorService scheduler;

    private final Model model;
    private Renderer currentRenderer;

    public VueController(ScheduledExecutorService scheduler, Model m) {
        this.scheduler = scheduler;
        this.model = m;

        GLFWErrorCallback.createPrint(System.err).set();
        // Initialize GLFW in the main thread to avoid issues with OpenGL context
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
            case GAME_3D -> currentRenderer = new Renderer3D(model);
        }

        currentRenderer.initialize();
        if (type != RendererType.MENU) {
            update(model, null);
        }
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

    public void cleanup() {
        if (currentRenderer != null) {
            currentRenderer.cleanup();
        }
        currentRenderer = null;
        scheduler.shutdown();

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
