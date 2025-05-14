package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.CurrentPiece2D;
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

    JFrame frame = new JFrame("Tetris");

    private static final Color EMPTY_CELL_COLOR = Color.WHITE;
    private static final Color CURRENT_PIECE_COLOR = Color.RED;
    private static final Color FROZEN_PIECE_COLOR = Color.BLUE;

    private final JPanel[][] panels;
    private final JLabel scoreLabel;
    private final JPanel gridPanel;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final boolean[] keys = new boolean[5];

    public Renderer2D(VueController vueController) {
        this.vueController = vueController;
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(400, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        this.scoreLabel = new JLabel();
        this.panels = new JPanel[Consts.GRID_HEIGHT][Consts.GRID_WIDTH];
        this.gridPanel = new JPanel();
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    @Override
    public void initialize() {
        gridPanel.setLayout(new GridLayout(Consts.GRID_HEIGHT, Consts.GRID_WIDTH));
        for (int y = 0; y < Consts.GRID_HEIGHT; y++) {
            for (int x = 0; x < Consts.GRID_WIDTH; x++) {
                panels[y][x] = new JPanel();
                panels[y][x].setPreferredSize(new Dimension(20, 20));
                panels[y][x].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                gridPanel.add(panels[y][x]);
            }
        }

        gridPanel.setPreferredSize(new Dimension(400, 800));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED) {
                        return false;
                    }

                    boolean isKeyPressed = e.getID() == KeyEvent.KEY_PRESSED;


                    // 2D mode controls
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_Z -> keys[0] = isKeyPressed; // Up
                        case KeyEvent.VK_S -> keys[1] = isKeyPressed; // Down
                        case KeyEvent.VK_Q -> keys[2] = isKeyPressed; // Left
                        case KeyEvent.VK_D -> keys[3] = isKeyPressed; // Right
                        case KeyEvent.VK_A -> keys[4] = isKeyPressed; // Rotate
                        case KeyEvent.VK_ESCAPE -> {
                            if (isKeyPressed) {
                                frame.dispose();
                                vueController.cleanup();
                            }
                        }
                    }
                    return false;
                });

        scheduler.scheduleAtFixedRate(() -> {
            CurrentPiece2D currentPiece = (CurrentPiece2D) vueController.getModel().getCurrentPiece();

            if (keys[0]) {
                currentPiece.translate2d(0, -1);
            }
            if (keys[1]) {
                currentPiece.translate2d(0, 1);
            }
            if (keys[2]) {
                currentPiece.translate2d(-1, 0);
            }
            if (keys[3]) {
                currentPiece.translate2d(1, 0);
            }
            if (keys[4]) {
                currentPiece.rotate2d(piece -> vueController.getModel().getGrid().checkCollision(piece));
            }
        }, 0, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void loop() {
        while (frame.isDisplayable()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
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
            panels[y][x].setBackground(color);
        }
    }

    private void drawFrozenPieces(Grid grid) {
        for (int i = 0; i < grid.getWidth(); i++) {
            for (int j = 0; j < grid.getHeight(); j++) {
                if (grid.getValue(i, j)) {
                    drawCell(i, j, FROZEN_PIECE_COLOR);
                }
            }
        }
    }

    private void drawCurrentPiece(CurrentPiece2D currentPiece) {
        boolean[][] piece = currentPiece.getPiece2d();
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (piece[i][j]) {
                    drawCell(
                            currentPiece.getX() + j,
                            currentPiece.getY() + i,
                            CURRENT_PIECE_COLOR);
                }
            }
        }
    }
}