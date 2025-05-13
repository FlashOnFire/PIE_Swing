package fr.polytech.pie.vc;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class VueController extends JFrame implements Observer {

    private final JPanel[] panels = new JPanel[Consts.SIZE * Consts.SIZE];


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
        gridPanel.setLayout(new GridLayout(Consts.SIZE, Consts.SIZE));
        for (int i = 0; i < Consts.SIZE * Consts.SIZE; i++) {
            panels[i] = new JPanel();
            panels[i].setPreferredSize(new Dimension(10, 10));
            panels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            gridPanel.add(panels[i]);
        }
        gridPanel.setPreferredSize(new Dimension(800, 800));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.add(gridPanel, BorderLayout.CENTER);

        updatePanel(m);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED) {
                        return false;
                    }

            boolean isKeyPressed = e.getID() == KeyEvent.KEY_PRESSED;
            System.out.println(isKeyPressed);
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

    private void updatePanel(Model model) {
        for (JPanel panel : panels) {
            panel.setBackground(Color.WHITE);
        }

       for (int i = 0; i < model.getCurrentPiece().getHeight(); i++) {
           for (int j = 0; j < model.getCurrentPiece().getWidth(); j++) {
               if (model.getCurrentPiece().getPiece()[i][j]) {
                   int x = model.getCurrentPiece().getX() + j;
                   int y = model.getCurrentPiece().getY() + i;
                   if (x >= 0 && x < Consts.SIZE && y >= 0 && y < Consts.SIZE) {
                       panels[y * Consts.SIZE + x].setBackground(Color.RED);
                   }
               }
           }
       }

        for (int i = 0; i < model.getGrid().getSize(); i++) {
            for (int j = 0; j < model.getGrid().getSize(); j++) {
                if (model.getGrid().getValue(i, j)) {
                    panels[j * Consts.SIZE + i].setBackground(Color.BLUE);
                }
            }
        }

        repaint();
    }
}
