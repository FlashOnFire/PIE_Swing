package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.twoD.CurrentPiece2D;
import fr.polytech.pie.model.Grid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Renderer2D implements Renderer {
    private final VueController vueController;

    final JFrame frame = new JFrame("Tetris");

    private static final Color EMPTY_CELL_COLOR = Color.WHITE;

    private final JPanel[][] panels;
    private final JLabel scoreLabel;
    private final JPanel gridPanel;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final boolean[] keys = new boolean[6];
    private final boolean[] keyProcessed = new boolean[6];
    private final long[] keyLastProcessedTime = new long[6];
    private static final long KEY_REPEAT_DELAY = 150;

    // Définition d'une enum pour représenter les actions des touches
    private enum KeyAction {
        MOVE_DOWN(0, KeyEvent.VK_Z, true), DROP(1, KeyEvent.VK_SPACE, false), MOVE_LEFT(2, KeyEvent.VK_Q, true), MOVE_RIGHT(3, KeyEvent.VK_D, true), ROTATE(4, KeyEvent.VK_A, true, 2.0), // facteur 2 pour le délai
        RUN_AI(5, KeyEvent.VK_I, true);

        final int index;
        final int keyCode;
        final boolean repeatable;
        final double delayFactor;

        KeyAction(int index, int keyCode, boolean repeatable) {
            this(index, keyCode, repeatable, 1.0);
        }

        KeyAction(int index, int keyCode, boolean repeatable, double delayFactor) {
            this.index = index;
            this.keyCode = keyCode;
            this.repeatable = repeatable;
            this.delayFactor = delayFactor;
        }
    }

    public Renderer2D(VueController vueController) {
        this.vueController = vueController;
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(400, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.LIGHT_GRAY);
        this.scoreLabel = new JLabel("Score: 0");
        this.scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        this.scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.panels = new JPanel[Consts.GRID_HEIGHT][Consts.GRID_WIDTH];
        this.gridPanel = new JPanel();
        this.gridPanel.setBackground(Color.black);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scorePanel.add(scoreLabel);
        scorePanel.setBackground(Color.LIGHT_GRAY);
        frame.add(scorePanel, BorderLayout.NORTH);
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    @Override
    public void initialize() {
        gridPanel.setLayout(new GridLayout(Consts.GRID_HEIGHT, Consts.GRID_WIDTH));

        for (int y = 0; y < Consts.GRID_HEIGHT; y++) {
            for (int x = 0; x < Consts.GRID_WIDTH; x++) {

                JPanel panel = new TetrisCubePanel(EMPTY_CELL_COLOR);
                panel.setPreferredSize(new Dimension(25, 25));
                panel.setLayout(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

                panels[y][x] = panel;
                gridPanel.add(panel);
            }
        }

        gridPanel.setPreferredSize(new Dimension(400, 800));
        gridPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 3), BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 8), BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3))));

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED) {
                return false;
            }

            boolean isKeyPressed = e.getID() == KeyEvent.KEY_PRESSED;

            for (KeyAction action : KeyAction.values()) {
                if (e.getKeyCode() == action.keyCode) {
                    keys[action.index] = isKeyPressed;
                    if (isKeyPressed) {
                        keyProcessed[action.index] = false;
                    }
                    break;
                }
            }

            return false;
        });

        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();

            // Traitement des actions avec l'enum
            for (KeyAction action : KeyAction.values()) {
                int i = action.index;

                if (keys[i]) {
                    boolean shouldProcess = !keyProcessed[i] || (action.repeatable && currentTime - keyLastProcessedTime[i] > KEY_REPEAT_DELAY * action.delayFactor);

                    if (shouldProcess) {
                        performAction(action);
                        keyLastProcessedTime[i] = currentTime;
                        keyProcessed[i] = true;
                    }
                }
            }
        }, 0, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void performAction(KeyAction action) {
        switch (action) {
            case MOVE_DOWN -> vueController.getModel().translateCurrentPiece2D(0, 1);
            case DROP -> vueController.getModel().dropCurrentPiece();
            case MOVE_LEFT -> vueController.getModel().translateCurrentPiece2D(-1, 0);
            case MOVE_RIGHT -> vueController.getModel().translateCurrentPiece2D(1, 0);
            case ROTATE -> vueController.getModel().rotateCurrentPiece2D();
            case RUN_AI -> vueController.getModel().runAi();
        }
    }

    public LoopStatus loop() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (frame.isDisplayable()) {
            return LoopStatus.CONTINUE;
        }

        return LoopStatus.SHOW_MENU;
    }

    @Override
    public void update(Grid grid, CurrentPiece currentPiece, int score) {
        assert currentPiece instanceof CurrentPiece2D : "Current piece is not a 2D piece";

        try {
            SwingUtilities.invokeAndWait(() -> {
                clearGrid();
                drawCurrentPiece((CurrentPiece2D) currentPiece);
                drawFrozenPieces(grid);
                scoreLabel.setText("Score: " + score);
                gridPanel.repaint();
            });
        } catch (Exception e) {
            Logger.getLogger(Renderer2D.class.getName()).log(Level.SEVERE, "Error updating panel", e);
        }
    }

    @Override
    public void cleanup() {
        scheduler.shutdown();
    }

    private void clearGrid() {
        for (int y = 0; y < Consts.GRID_HEIGHT; y++) {
            for (int x = 0; x < Consts.GRID_WIDTH; x++) {
                panels[y][x].setBackground(EMPTY_CELL_COLOR);
            }
        }
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < Consts.GRID_WIDTH && y >= 0 && y < Consts.GRID_HEIGHT;
    }

    private void drawCell(int x, int y, Color color) {
        if (isWithinBounds(x, y)) {
            panels[panels.length - y - 1][x].setBackground(color);
        }
    }

    private void drawFrozenPieces(Grid grid) {
        for (int i = 0; i < grid.getWidth(); i++) {
            for (int j = 0; j < grid.getHeight(); j++) {
                if (grid.getValue(i, j) != Piece.Empty) {
                    drawCell(i, j, grid.getValue(i, j).getColor());
                }
            }
        }
    }

    private void drawCurrentPiece(CurrentPiece2D currentPiece) {
        Piece[][] piece = currentPiece.getPiece2d();
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (piece[j][i] != Piece.Empty) {
                    drawCell(currentPiece.getX() + i, currentPiece.getY() + j, piece[j][i].getColor());
                }
            }
        }
    }
}
