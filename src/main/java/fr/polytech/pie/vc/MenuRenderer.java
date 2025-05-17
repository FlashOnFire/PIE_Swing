package fr.polytech.pie.vc;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MenuRenderer implements Renderer {

    private final JFrame frame = new JFrame();

    private LoopStatus nextLoopStatus = LoopStatus.CONTINUE;
    private final int highscore2D;
    private final int highscore3D;

    public MenuRenderer(int highscore2D, int highscore3D) {
        this.highscore2D = highscore2D;
        this.highscore3D = highscore3D;

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

        JPanel buttonPanel = getJPanel();

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH);

        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.add(titleLabel, BorderLayout.NORTH);
        menuPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JTextArea controlsText = getControlsTextArea();
        bottomPanel.add(controlsText, BorderLayout.CENTER);

        var highscoreText = new JTextArea();
        highscoreText.setEditable(false);
        highscoreText.setFont(new Font("Arial", Font.PLAIN, 12));
        highscoreText.setText(
                """
                    Highscore 2D: %d
                    Highscore 3D: %d""".formatted(highscore2D, highscore3D)
        );

        bottomPanel.add(highscoreText, BorderLayout.SOUTH);

        menuPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(menuPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    @NotNull
    private static JTextArea getControlsTextArea() {
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
        return controlsText;
    }

    @NotNull
    private JPanel getJPanel() {
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
        return buttonPanel;
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
    public void update(Grid grid, CurrentPiece currentPiece, CurrentPiece nextPiece, int score) {
    }

    @Override
    public void cleanup() {
        if (frame.isDisplayable()) {
            frame.dispose();
        }
    }
}
