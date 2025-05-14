package fr.polytech.pie;

import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.VueController3D;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Model m = new Model(scheduler);

        // Use the Swing event-dispatching thread for the UI
//        SwingUtilities.invokeLater(() -> {
//            try {
//                // Set the look and feel to the system's native look and feel
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            VueController vc = new VueController(m);
//
//            // Register the view-controller as an observer of the model
//            m.addObserver(vc);
//
//            // Display the game window
//            vc.setLocationRelativeTo(null); // Center on screen
//            vc.setVisible(true);
//        });

        VueController3D vc3d = new VueController3D(scheduler, m);
        m.addObserver(vc3d);
        vc3d.loop();
        vc3d.shutdown();

        scheduler.shutdown();
    }
}