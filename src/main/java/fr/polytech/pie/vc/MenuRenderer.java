package fr.polytech.pie.vc;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.Grid;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class MenuRenderer implements Renderer {
    final VueController vueController;
    private final JFrame frame = new JFrame();

    private final long highScore2D;
    private final long highScore3D;

    private LoopStatus nextLoopStatus = LoopStatus.CONTINUE;

    public MenuRenderer(long highScore2D, long highScore3D, VueController vueController) {
        this.highScore2D = highScore2D;
        this.highScore3D = highScore3D;
        this.vueController = vueController;
    }

    @Override
    public void initialize() {
        frame.setTitle("Tetris");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        frame.setLayout(new BorderLayout());

        JLabel titleLabel = createTitleLabel();
        JPanel buttonPanel = createButtonPanel();
        JPanel levelPanel = createLevelPanel();
        JPanel gameControlsPanel = createGameControlsPanel(buttonPanel, levelPanel);
        JPanel centerPanel = createCenterPanel(gameControlsPanel);
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.add(titleLabel, BorderLayout.NORTH);
        menuPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel highScorePanel = createHighScorePanel();
        menuPanel.add(highScorePanel, BorderLayout.BEFORE_FIRST_LINE);

        menuPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        frame.add(menuPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("TETRIS", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLUE);
        return titleLabel;
    }

    private JPanel createHighScorePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel highScore2DLabel = new JLabel("High-score 2D : " + highScore2D, JLabel.CENTER);
        highScore2DLabel.setFont(new Font("Arial", Font.BOLD, 16));
        highScore2DLabel.setForeground(new Color(34, 139, 34));
        highScore2DLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel highScore3DLabel = new JLabel("High-score 3D : " + highScore3D, JLabel.CENTER);
        highScore3DLabel.setFont(new Font("Arial", Font.BOLD, 16));
        highScore3DLabel.setForeground(new Color(34, 139, 34));
        highScore3DLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(highScore2DLabel);
        panel.add(highScore3DLabel);

        return panel;
    }

    private JPanel createLevelPanel() {
        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel levelLabel = new JLabel("Difficulté : ");
        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));

        String[] levels = {"Facile", "Moyen", "Difficile"};
        JComboBox<String> levelSelector = new JComboBox<>(levels);
        levelSelector.setSelectedIndex(vueController.getDifficulty() - 1);
        levelSelector.addActionListener(_ -> vueController.setDifficulty(levelSelector.getSelectedIndex() + 1));

        levelPanel.add(levelLabel);
        levelPanel.add(levelSelector);
        return levelPanel;
    }

    private JPanel createGameControlsPanel(JPanel buttonPanel, JPanel levelPanel) {
        JPanel gameControlsPanel = new JPanel(new BorderLayout());
        gameControlsPanel.add(buttonPanel, BorderLayout.CENTER);
        gameControlsPanel.add(levelPanel, BorderLayout.SOUTH);
        return gameControlsPanel;
    }

    private JPanel createCenterPanel(JPanel gameControlsPanel) {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.NORTH);
        centerPanel.add(gameControlsPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH);
        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createControlsTextArea(), BorderLayout.CENTER);
        return bottomPanel;
    }

    private JTextPane createControlsTextArea() {
        JTextPane controlsText = new JTextPane();
        controlsText.setEditable(false);
        controlsText.setFont(new Font("Arial", Font.PLAIN, 12));
        controlsText.setText(
                """
                        2D Controls:
                        Z - Up, S - Down, Q - Left, D - Right
                        Q - Rotate, Space - Drop
                        Escape - Quit
                        
                        3D Controls:
                        Z - Forward, S - Backward, Q - Left, D - Right
                        SHIFT+Z, SHIFT+S - Up, SHIFT+Q, SHIFT+D - Rotations
                        SPACE - Drop
                        Escape - Quit
                        """);

        StyledDocument documentStyle = controlsText.getStyledDocument();
        SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), centerAttribute, false);

        return controlsText;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 20));

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
    public void update(Grid grid, Piece currentPiece, Piece nextPiece, int score, boolean isGameOver) {
        // No update needed for the menu
    }

    @Override
    public void cleanup() {
        if (frame.isDisplayable()) {
            frame.dispose();
        }
    }
}
