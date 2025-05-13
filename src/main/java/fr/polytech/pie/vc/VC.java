package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.M;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class VC extends JFrame implements Observer {

    private final JLabel xLabel = new JLabel("x=0");
    private final JLabel yLabel = new JLabel("y=0");

    private final JPanel[] panels = new JPanel[Consts.SIZE*Consts.SIZE];


    public VC(M m) {
        super("VC");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel xPanel = new JPanel();
        xPanel.setLayout(new FlowLayout());
        JPanel yPanel = new JPanel();
        yPanel.setLayout(new FlowLayout());

        JButton xButtonMinus = new JButton("x-");
        JButton xButtonPlus = new JButton("x+");
        JButton yButtonMinus = new JButton("y-");
        JButton yButtonPlus = new JButton("y+");

        xPanel.add(xButtonMinus);
        xPanel.add(xLabel);
        xPanel.add(xButtonPlus);

        yPanel.add(yButtonMinus);
        yPanel.add(yLabel);
        yPanel.add(yButtonPlus);

        xButtonMinus.addActionListener(e -> m.setX(m.getX() - 1));
        xButtonPlus.addActionListener(e -> m.setX(m.getX() + 1));
        yButtonMinus.addActionListener(e -> m.setY(m.getY() - 1));
        yButtonPlus.addActionListener(e -> m.setY(m.getY() + 1));

        this.add(xPanel, BorderLayout.NORTH);
        this.add(yPanel, BorderLayout.SOUTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(Consts.SIZE, Consts.SIZE));
        for (int i = 0; i < Consts.SIZE*Consts.SIZE; i++) {
            panels[i] = new JPanel();
            panels[i].setPreferredSize(new Dimension(10, 10));
            panels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            gridPanel.add(panels[i]);
        }
        gridPanel.setPreferredSize(new Dimension(800, 800));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.add(gridPanel, BorderLayout.CENTER);
        updatePanel(m.getX(), m.getY());

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    System.out.println("Got key event!");
                    switch (e.getKeyChar()) {
                        case 'z' -> m.setY(m.getY() - 1);
                        case 's' -> m.setY(m.getY() + 1);
                        case 'q' -> m.setX(m.getX() - 1);
                        case 'd' -> m.setX(m.getX() + 1);
                    }
                    return false;
                });

        this.pack();
    }


    @Override
    public void update(Observable o, Object arg) {
        int x = ((M) o).getX();
        int y = ((M) o).getY();
        xLabel.setText("x=" + x);
        yLabel.setText("y=" + y);
        updatePanel(x, y);
    }

    private void updatePanel(int x, int y) {
        for (int i = 0; i < Consts.SIZE*Consts.SIZE; i++) {
            if (i == x + y * Consts.SIZE) {
                panels[i].setBackground(Color.RED);
            } else {
                panels[i].setBackground(Color.WHITE);
            }
        }
    }
}
