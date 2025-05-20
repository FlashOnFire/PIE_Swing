package fr.polytech.pie.vc.render.twodee;

import fr.polytech.pie.model.PieceColor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;

public class TetrisCubePanel extends JPanel {

    private static final int BLOCK_SIZE = 25;

    private static final HashMap<PieceColor, Border> borders = new HashMap<>();

    static {
        int outerBorderSize = BLOCK_SIZE / 10;
        int innerBorderSize = BLOCK_SIZE / 8;
        int bottomBorderSize = BLOCK_SIZE / 8;
        int cornerBorderSize = BLOCK_SIZE / 4;

        for (PieceColor piece : PieceColor.values()) {
            if (piece == PieceColor.Empty) {
                continue;
            }

            Color color = piece.getColor();

            Color outerColor = Color.BLACK;
            Color innerColor = (color == Color.BLACK) ? Color.BLACK : color.darker().darker();
            Color bottomColor = (color == Color.BLACK) ? Color.BLACK : color.darker().darker().darker();
            Color cornerColor = (color == Color.BLACK) ? Color.BLACK : color.darker();

            Border border = (BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(outerColor, outerBorderSize),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(innerColor, innerBorderSize),
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, bottomBorderSize, 0, bottomColor),
                                    BorderFactory.createMatteBorder(
                                            innerBorderSize,
                                            innerBorderSize,
                                            cornerBorderSize,
                                            cornerBorderSize,
                                            cornerColor
                                    )
                            )
                    )
            ));

            borders.put(piece, border);
        }

        borders.put(PieceColor.Empty, null);
    }

    TetrisCubePanel() {
        setPreferredSize(new Dimension(BLOCK_SIZE, BLOCK_SIZE));
        setPiece(PieceColor.Empty);
    }

    public void setPiece(PieceColor piece) {
        if (piece == PieceColor.Empty) {
            setBackground(Color.BLACK);
        } else {
            setBackground(piece.getColor());
        }

        setBorder(borders.get(piece));
    }
}