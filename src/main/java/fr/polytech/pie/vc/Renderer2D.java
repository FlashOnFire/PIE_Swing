package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.CurrentPiece2D;
import fr.polytech.pie.model.Grid;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Renderer2D implements Renderer {
    JFrame frame = new JFrame("Tetris");

    private static final Color EMPTY_CELL_COLOR = Color.WHITE;
    private static final Color CURRENT_PIECE_COLOR = Color.RED;
    private static final Color FROZEN_PIECE_COLOR = Color.BLUE;

    private final JPanel[][] panels;
    private final JLabel scoreLabel;
    private final JPanel gridPanel;

    public Renderer2D() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
        // No resources to clean up in the 2D renderer
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