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
        gridPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 8),
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3)
                )
        ));

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
                        case KeyEvent.VK_I -> keys[5] = isKeyPressed;
                        case KeyEvent.VK_ESCAPE -> {
                            if (isKeyPressed) {
                                frame.dispose();
                            }
                        }
                    }
                    return false;
                });

        scheduler.scheduleAtFixedRate(
                () -> {
                    if (keys[0]) {
                        vueController.getModel().translateCurrentPiece2D(0, 1);
                    }
                    if (keys[1]) {
                        vueController.getModel().translateCurrentPiece2D(0, -1);
                    }
                    if (keys[2]) {
                        vueController.getModel().translateCurrentPiece2D(-1, 0);
                    }
                    if (keys[3]) {
                        vueController.getModel().translateCurrentPiece2D(1, 0);
                    }
                    if (keys[4]) {
                        vueController.getModel().rotateCurrentPiece2D();
                    }
                    if (keys[5]) {
                        vueController.getModel().runAi();
                    }
                }, 0, 50, java.util.concurrent.TimeUnit.MILLISECONDS
        );
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