package fr.polytech.pie.vc;

import fr.polytech.pie.model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class VueController implements Observer {
    private final Model model;
    private final JPanel gridPanel = new JPanel();
    private Renderer currentRenderer;
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final JPanel menuPanel = new JPanel();
    private final JButton play2DButton = new JButton("Play 2D");
    private final JButton play3DButton = new JButton("Play 3D");
    private boolean gameStarted = false;

    public VueController(Model m) {
        this.model = m;

        setupKeyboardInput();
    }

    /*private void startGame(boolean is3D) {
        model.setRenderingMode(is3D);
        model.resetGame();
        updateRenderer();
        showGame();
        gameStarted = true;
    }*/

    private void setupKeyboardInput() {
        /*KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (!gameStarted) {
                        return false;
                    }

                    if (e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED) {
                        return false;
                    }

                    boolean isKeyPressed = e.getID() == KeyEvent.KEY_PRESSED;

                    if (model.is3D()) {
                        // 3D mode controls
                        switch (e.getKeyChar()) {
                            case 'z' -> model.setKey(0, isKeyPressed); // Up
                            case 's' -> model.setKey(1, isKeyPressed); // Down
                            case 'q' -> model.setKey(2, isKeyPressed); // Left
                            case 'd' -> model.setKey(3, isKeyPressed); // Right
                            case 'a' -> model.setKey(4, isKeyPressed); // Rotate Z
                            case 'r' -> model.setKey(5, isKeyPressed); // Forward (z+)
                            case 'f' -> model.setKey(6, isKeyPressed); // Backward (z-)
                            case 't' -> model.setKey(7, isKeyPressed); // Rotate X
                            case 'g' -> model.setKey(8, isKeyPressed); // Rotate Y
                            case 'i' -> model.setKey(10, isKeyPressed);
                            case 'm' -> {
                                if (isKeyPressed) {
                                    model.setKey(9, false); // Reset key state
                                    //model.switchRenderingMode();
                                    updateRenderer();
                                }
                            }
                            case 'p' -> {
                                if (isKeyPressed) {
                                    //showMenu();
                                }
                            }
                        }
                    } else {
                        // 2D mode controls
                        switch (e.getKeyChar()) {
                            case 'z' -> model.setKey(0, isKeyPressed); // Up
                            case 's' -> model.setKey(1, isKeyPressed); // Down
                            case 'q' -> model.setKey(2, isKeyPressed); // Left
                            case 'd' -> model.setKey(3, isKeyPressed); // Right
                            case 'a' -> model.setKey(4, isKeyPressed); // Rotate
                            case 'i' -> model.setKey(10, isKeyPressed);
                            case 'm' -> {
                                if (isKeyPressed) {
                                    //model.switchRenderingMode();
                                    updateRenderer();
                                }
                            }
                            case 'p' -> {
                                if (isKeyPressed) {
                                    //showMenu();
                                }
                            }
                        }
                    }
                    return false;
                });*/
    }

    private void updateRenderer() {
        if (currentRenderer != null) {
            currentRenderer.cleanup();
        }

        /*if (model.is3D()) {
//            currentRenderer = renderer3D;
        } else {
            currentRenderer = renderer2D;
        }*/

        currentRenderer = new Renderer2D();
        currentRenderer.initialize();
        update(model, null);
    }

    @Override
    public void update(Observable o, Object arg) {
        Model model = (Model) o;
        try {
            if (currentRenderer != null && gameStarted) {
                currentRenderer.update(model.getGrid(), model.getCurrentPiece(), model.getScore());
            }
        } catch (Exception e) {
            Logger.getLogger(VueController.class.getName()).log(Level.SEVERE, "Error updating VueController", e);
        }
    }
}
