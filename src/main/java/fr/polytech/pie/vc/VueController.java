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
public class VueController extends JFrame implements Observer {
    private final Model model;
    private final JPanel gridPanel = new JPanel();
    private Renderer currentRenderer;
    private final Renderer renderer2D;
    //    private final Renderer renderer3D;
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final JPanel menuPanel = new JPanel();
    private final JButton play2DButton = new JButton("Play 2D");
    private final JButton play3DButton = new JButton("Play 3D");
    private boolean gameStarted = false;

    public VueController(Model m) {
        super("Tetris");
        this.model = m;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        JLabel scoreLabel = new JLabel("Score: 0");
        topPanel.add(scoreLabel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        JButton switchModeButton = new JButton("Switch Mode");
        switchModeButton.addActionListener(_ -> {
            model.switchRenderingMode();
            updateRenderer();
        });
        bottomPanel.add(switchModeButton);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);

        renderer2D = new Renderer2D(scoreLabel, gridPanel);
//        renderer3D = new Renderer3D(scoreLabel, gridPanel);

        setupMenuPanel();

        cardPanel.add(menuPanel, "MENU");
        cardPanel.add(gridPanel, "GAME");

        this.add(cardPanel, BorderLayout.CENTER);

        setupKeyboardInput();

        showMenu();

        this.pack();
    }

    private void setupMenuPanel() {
        menuPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("TETRIS", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLUE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 20));

        play2DButton.setFont(new Font("Arial", Font.BOLD, 24));
        play2DButton.addActionListener(_ -> startGame(false));

        play3DButton.setFont(new Font("Arial", Font.BOLD, 24));
        play3DButton.addActionListener(_ -> startGame(true));

        buttonPanel.add(play2DButton);
        buttonPanel.add(play3DButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH);

        menuPanel.add(titleLabel, BorderLayout.NORTH);
        menuPanel.add(centerPanel, BorderLayout.CENTER);

        JTextArea controlsText = new JTextArea();
        controlsText.setEditable(false);
        controlsText.setFont(new Font("Arial", Font.PLAIN, 12));
        controlsText.setText(
                """
                        2D Controls:
                        z - Up, s - Down, q - Left, d - Right, a - Rotate
                        
                        3D Controls:
                        z - Up, s - Down, q - Left, d - Right, a - Rotate Z
                        r - Forward, f - Backward, t - Rotate X, g - Rotate Y
                        m - Switch between 2D and 3D mode""");

        menuPanel.add(controlsText, BorderLayout.SOUTH);
    }

    private void startGame(boolean is3D) {
        model.setRenderingMode(is3D);
        model.resetGame();
        updateRenderer();
        showGame();
        gameStarted = true;
    }

    private void showMenu() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "MENU");
        gameStarted = false;
    }

    private void showGame() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "GAME");
    }

    private void setupKeyboardInput() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
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
                                    model.switchRenderingMode();
                                    updateRenderer();
                                }
                            }
                            case 'p' -> {
                                if (isKeyPressed) {
                                    showMenu();
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
                                    model.switchRenderingMode();
                                    updateRenderer();
                                }
                            }
                            case 'p' -> {
                                if (isKeyPressed) {
                                    showMenu();
                                }
                            }
                        }
                    }
                    return false;
                });
    }

    private void updateRenderer() {
        if (currentRenderer != null) {
            currentRenderer.cleanup();
        }

        gridPanel.removeAll();

        if (model.is3D()) {
//            currentRenderer = renderer3D;
        } else {
            currentRenderer = renderer2D;
        }

        currentRenderer.initialize();
        this.pack();
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
