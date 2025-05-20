package fr.polytech.pie.vc;

import javax.swing.*;
import java.awt.*;

public class TetrisCubePanel extends JPanel {
    TetrisCubePanel(Color color) {
        this.setBackground(color);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);

        int outerBorderSize = getHeight() / 10;
        int innerBorderSize = getHeight() / 8;
        int bottomBorderSize = getHeight() / 8;
        int cornerBorderSize = getHeight() / 4;

        Color outerColor = Color.BLACK;
        Color innerColor = (color == Color.BLACK) ? Color.BLACK : color.darker().darker();
        Color bottomColor = (color == Color.BLACK) ? Color.BLACK : color.darker().darker().darker();
        Color cornerColor = (color == Color.BLACK) ? Color.BLACK : color.darker();

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(outerColor, outerBorderSize),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(innerColor, innerBorderSize),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, bottomBorderSize, 0, bottomColor),
                                BorderFactory.createMatteBorder(innerBorderSize, innerBorderSize, cornerBorderSize, cornerBorderSize, cornerColor)
                        )
                )
        ));
    }
}