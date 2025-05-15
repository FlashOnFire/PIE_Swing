package fr.polytech.pie.vc;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;

import javax.swing.*;
import java.awt.*;

public class MenuRenderer implements Renderer {

    private final JFrame frame = new JFrame();

    private LoopStatus nextLoopStatus = LoopStatus.CONTINUE;

    public MenuRenderer() {
        frame.setTitle("Tetris");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
    }

    @Override
    public void initialize() {
        frame.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("TETRIS", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLUE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 20));

        JButton play2DButton = new JButton("Play 2D");
        play2DButton.setFont(new Font("Arial", Font.BOLD, 24));
        play2DButton.addActionListener(_ -> nextLoopStatus = LoopStatus.START_GAME_2D);

        JButton play3DButton = new JButton("Play 3D");
        play3DButton.setFont(new Font("Arial", Font.BOLD, 24));
        play3DButton.addActionListener(_ -> nextLoopStatus = LoopStatus.START_GAME_3D);

        buttonPanel.add(play2DButton);
        buttonPanel.add(play3DButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH);

        JPanel menuPanel = new JPanel(new BorderLayout());
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
        frame.add(menuPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public LoopStatus loop() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!frame.isDisplayable()) {
            return LoopStatus.QUIT;
        }

        return nextLoopStatus;
    }

    @Override
    public void update(Grid grid, CurrentPiece currentPiece, int score) {
    }

    @Override
    public void cleanup() {
        if (frame.isDisplayable()) {
            frame.dispose();
        }
    }
}
