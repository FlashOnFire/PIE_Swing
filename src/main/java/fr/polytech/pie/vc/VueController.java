package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
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
    private static final Color EMPTY_CELL_COLOR = Color.WHITE;
    private static final Color CURRENT_PIECE_COLOR = Color.RED;
    private static final Color FROZEN_PIECE_COLOR = Color.BLUE;


    private final JPanel[][] panels = new JPanel[Consts.GRID_HEIGHT][Consts.GRID_WIDTH];

    public VueController(Model m) {
        super("VC");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel xPanel = new JPanel();
        xPanel.setLayout(new FlowLayout());
        JPanel yPanel = new JPanel();
        yPanel.setLayout(new FlowLayout());

        this.add(xPanel, BorderLayout.NORTH);
        this.add(yPanel, BorderLayout.SOUTH);

        JPanel gridPanel = new JPanel();
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

        this.add(gridPanel, BorderLayout.CENTER);

        updatePanel(m);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED) {
                        return false;
                    }

                    boolean isKeyPressed = e.getID() == KeyEvent.KEY_PRESSED;
                    switch (e.getKeyChar()) {
                        case 'z' -> m.setKey(0, isKeyPressed);
                        case 's' -> m.setKey(1, isKeyPressed);
                        case 'q' -> m.setKey(2, isKeyPressed);
                        case 'd' -> m.setKey(3, isKeyPressed);
                        case 'a' -> m.setKey(4, isKeyPressed);
                    }
                    return false;
                });
        this.pack();
    }


    @Override
    public void update(Observable o, Object arg) {
        updatePanel((Model) o);
    }

    private synchronized void updatePanel(Model model) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                clearGrid();
                drawCurrentPiece(model.getCurrentPiece());
                drawFrozenPieces(model.getGrid());
                repaint();
            });
        } catch (Exception e) {
            Logger.getLogger(VueController.class.getName()).log(Level.SEVERE, "Error updating panel", e);
        }
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

    private void drawCurrentPiece(CurrentPiece currentPiece) {
        boolean[][] piece = currentPiece.getPiece();
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (piece[i][j]) {
                    drawCell(
                            currentPiece.getX() + j,
                            currentPiece.getY() + i,
                            CURRENT_PIECE_COLOR
                    );
                }
            }
        }
    }
}
