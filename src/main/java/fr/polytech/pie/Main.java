package fr.polytech.pie;

import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.VueController;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use the Swing event-dispatching thread for the UI
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the look and feel to the system's native look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create the model and view-controller
            Model m = new Model();
            VueController vc = new VueController(m);

            // Register the view-controller as an observer of the model
            m.addObserver(vc);

            // Display the game window
            vc.setLocationRelativeTo(null); // Center on screen
            vc.setVisible(true);
        });
    }
}