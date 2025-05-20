package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.twoD.CurrentPiece2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Renderer2D implements Renderer {
    private final VueController vueController;

    final JFrame frame = new JFrame("Tetris");

    private final TetrisCubePanel[][] gridPanels;
    private final JLabel scoreLabel;
    private final JPanel gridPanel;
    private final TetrisCubePanel[][] nextPiecePanels;
    private final JPanel nextPiecePanel;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final boolean[] keys = new boolean[8];
    private final boolean[] keyProcessed = new boolean[8];
    private final long[] keyLastProcessedTime = new long[8];
    private static final long KEY_REPEAT_DELAY = 150;

    private boolean isGameOver = false;
    private long gameOverStartTime = 0;
    private JPanel gameOverPanel;
    private int remainingSeconds = 5;
    private JLabel countdownLabel;
    private boolean escape = false;

    private enum KeyAction {
        MOVE_UP(0, KeyEvent.VK_Z, true),
        MOVE_DOWN(1, KeyEvent.VK_S, true),
        DROP(2, KeyEvent.VK_SPACE, false),
        MOVE_LEFT(3, KeyEvent.VK_Q, true),
        MOVE_RIGHT(4, KeyEvent.VK_D, true),
        ROTATE(5, KeyEvent.VK_A, true, 2.0),
        RUN_AI(6, KeyEvent.VK_I, true),
        EXIT(7, KeyEvent.VK_ESCAPE, false);

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
        setupFrame();
        this.scoreLabel = createScoreLabel();
        this.gridPanels = new TetrisCubePanel[Consts.GRID_HEIGHT][Consts.GRID_WIDTH];
        this.gridPanel = new JPanel();
        this.nextPiecePanels = new TetrisCubePanel[Consts.PIECE_SIZE][Consts.PIECE_SIZE];
        this.nextPiecePanel = new JPanel();
        JPanel sidePanel = createSidePanel();

        frame.add(createScorePanel(), BorderLayout.NORTH);
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);
        frame.setVisible(true);
    }

    private void setupFrame() {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(600, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.LIGHT_GRAY);
    }

    private JLabel createScoreLabel() {
        JLabel label = new JLabel("Score: 0");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel createScorePanel() {
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scorePanel.add(scoreLabel);
        scorePanel.setBackground(Color.LIGHT_GRAY);
        return scorePanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.add(new JLabel("Next Piece", SwingConstants.CENTER), BorderLayout.NORTH);

        JPanel centeringPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centeringPanel.add(nextPiecePanel);
        centeringPanel.setBackground(Color.LIGHT_GRAY);

        sidePanel.add(centeringPanel, BorderLayout.CENTER);
        sidePanel.setBackground(Color.LIGHT_GRAY);
        return sidePanel;
    }

    @Override
    public void initialize() {
        initializeGridPanel();
        initializeNextPiecePanel();
        setupKeyboard();
        setupScheduler();
        setupGameOverPanel();
    }

    private void initializeGridPanel() {
        gridPanel.setLayout(new GridLayout(Consts.GRID_HEIGHT, Consts.GRID_WIDTH));
        for (int y = 0; y < Consts.GRID_HEIGHT; y++) {
            for (int x = 0; x < Consts.GRID_WIDTH; x++) {
                drawGrid(y, x, gridPanels, gridPanel);
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
    }

    private void drawGrid(int y, int x, JPanel[][] gridPanels, JPanel gridPanel) {
        JPanel panel = new TetrisCubePanel();
        panel.setLayout(new BorderLayout());
        gridPanels[y][x] = panel;
        gridPanel.add(panel);
    }

    private void initializeNextPiecePanel() {
        nextPiecePanel.setLayout(new GridLayout(Consts.PIECE_SIZE, Consts.PIECE_SIZE));
        for (int y = 0; y < Consts.PIECE_SIZE; y++) {
            for (int x = 0; x < Consts.PIECE_SIZE; x++) {
                drawGrid(y, x, nextPiecePanels, nextPiecePanel);
            }
        }
        nextPiecePanel.setPreferredSize(new Dimension(200, 200));
        nextPiecePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 8),
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3)
                )
        ));
    }

    private void setupKeyboard() {
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
    }

    private void setupScheduler() {
        scheduler.scheduleAtFixedRate(
                () -> {
                    long currentTime = System.currentTimeMillis();
                    for (KeyAction action : KeyAction.values()) {
                        int i = action.index;
                        if (keys[i]) {
                            boolean shouldProcess = !keyProcessed[i] ||
                                    (action.repeatable && currentTime - keyLastProcessedTime[i] > KEY_REPEAT_DELAY * action.delayFactor);
                            if (shouldProcess) {
                                performAction(action);
                                keyLastProcessedTime[i] = currentTime;
                                keyProcessed[i] = true;
                            }
                        }
                    }
                }, 0, 50, TimeUnit.MILLISECONDS
        );
    }

    private void setupGameOverPanel() {
        gameOverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(new BorderLayout());

        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 48));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);

        countdownLabel = new JLabel();
        countdownLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countdownLabel.setOpaque(false);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(countdownLabel, BorderLayout.CENTER);

        gameOverPanel.add(gameOverLabel, BorderLayout.CENTER);
        gameOverPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setGlassPane(gameOverPanel);
        gameOverPanel.setVisible(false);
    }

    private void performAction(KeyAction action) {
        switch (action) {
            case MOVE_UP -> vueController.getModel().translateCurrentPiece2D(0, 1);
            case MOVE_DOWN -> vueController.getModel().translateCurrentPiece2D(0, -1);
            case DROP -> vueController.getModel().dropCurrentPiece();
            case MOVE_LEFT -> vueController.getModel().translateCurrentPiece2D(-1, 0);
            case MOVE_RIGHT -> vueController.getModel().translateCurrentPiece2D(1, 0);
            case ROTATE -> vueController.getModel().rotateCurrentPiece2D();
            case RUN_AI -> vueController.getModel().runAi();
            case EXIT -> this.escape = true;
        }
    }

    public LoopStatus loop() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (escape) {
            escape = false;
            return LoopStatus.SHOW_MENU;
        }

        if (!frame.isDisplayable()) {
            return LoopStatus.SHOW_MENU;
        }

        if (isGameOver) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - gameOverStartTime;

            int newRemainingSeconds = 5 - (int) (elapsedTime / 1000);

            if (newRemainingSeconds != remainingSeconds && newRemainingSeconds >= 0) {
                remainingSeconds = newRemainingSeconds;
                SwingUtilities.invokeLater(this::updateCountdownLabel);
            }

            if (elapsedTime > 5000) {
                resetGameOver();
                return LoopStatus.SHOW_MENU;
            }
            return LoopStatus.GAME_OVER;
        }

        if (vueController.getModel().isGameOver() && !isGameOver) {
            showGameOver();
            return LoopStatus.GAME_OVER;
        }

        return LoopStatus.CONTINUE;
    }

    private void updateCountdownLabel() {
        if (countdownLabel != null) {
            countdownLabel.setText("Retour au menu dans " + remainingSeconds + "s...");
            gameOverPanel.validate();
            gameOverPanel.repaint();
        }
    }

    private void showGameOver() {
        isGameOver = true;
        gameOverStartTime = System.currentTimeMillis();
        remainingSeconds = 5;
        updateCountdownLabel();

        SwingUtilities.invokeLater(() -> {
            gameOverPanel.setVisible(true);
            frame.validate();
            frame.repaint();
        });
    }

    private void resetGameOver() {
        isGameOver = false;
        SwingUtilities.invokeLater(() -> {
            gameOverPanel.setVisible(false);
            frame.validate();
            frame.repaint();
        });
    }

    @Override
    public void update(Grid grid, CurrentPiece currentPiece, CurrentPiece nextPiece, int score, boolean isGameOver) {
        assert currentPiece instanceof CurrentPiece2D : "Current piece is not a 2D piece";

        try {
            SwingUtilities.invokeAndWait(() -> {
                clearGrid();
                drawFrozenPieces(grid);
                drawCurrentPiece((CurrentPiece2D) currentPiece);
                clearNextPieceGrid();
                drawNextPiece((CurrentPiece2D) nextPiece);
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
        if (frame.isDisplayable()) {
            frame.dispose();
        }
        resetGameOver();
    }

    private void clearGrid() {
        for (int y = 0; y < Consts.GRID_HEIGHT; y++) {
            for (int x = 0; x < Consts.GRID_WIDTH; x++) {
                gridPanels[y][x].setPiece(Piece.Empty);
            }
        }
    }

    private void clearNextPieceGrid() {
        for (int y = 0; y < Consts.PIECE_SIZE; y++) {
            for (int x = 0; x < Consts.PIECE_SIZE; x++) {
                nextPiecePanels[y][x].setPiece(Piece.Empty);
            }
        }
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < Consts.GRID_WIDTH && y >= 0 && y < Consts.GRID_HEIGHT;
    }

    private void drawGridCell(int x, int y, Piece piece) {
        if (isWithinBounds(x, y)) {
            gridPanels[gridPanels.length - y - 1][x].setPiece(piece);
        }
    }

    private void drawFrozenPieces(Grid grid) {
        for (int i = 0; i < grid.getWidth(); i++) {
            for (int j = 0; j < grid.getHeight(); j++) {
                drawGridCell(i, j, grid.getValue(i, j));
            }
        }
    }

    private void drawCurrentPiece(CurrentPiece2D currentPiece) {
        Piece[][] piece = currentPiece.getPiece2d();
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                drawGridCell(currentPiece.getX() + i, currentPiece.getY() + j, piece[j][i]);
            }
        }

        int droppedY = vueController.getModel().getDroppedYCurrentPiece();

        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                drawGridCell(currentPiece.getX() + i, droppedY + j, Piece.Preview);
            }
        }
    }

    private void drawNextPiece(CurrentPiece2D nextPiece) {
        Piece[][] piece = nextPiece.getPiece2d();

        int offsetX = (Consts.PIECE_SIZE - nextPiece.getWidth()) / 2;
        int offsetY = (Consts.PIECE_SIZE - nextPiece.getHeight()) / 2;

        for (int i = 0; i < nextPiece.getWidth(); i++) {
            for (int j = 0; j < nextPiece.getHeight(); j++) {
                if (piece[j][i] != Piece.Empty) {
                    nextPiecePanels[offsetY + j][offsetX + i].setPiece(piece[j][i]);
                }
            }
        }
    }
}
