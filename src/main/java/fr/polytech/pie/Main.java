package fr.polytech.pie;

import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.VueController;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        try {
            InputStream audioSrc = Main.class.getResourceAsStream("/sounds/tetris-theme.wav");
            if (audioSrc != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(audioSrc));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Tetris music started successfully");
            } else {
                System.err.println("Could not find tetris theme audio file");
            }
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
            Logger.getLogger(Main.class.getName()).severe("Error playing music: " + e.getMessage());
        }


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Model m = new Model(scheduler);

        // Use the Swing event-dispatching thread for the UI

        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    // Set the look and feel to the system's native look and feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }


        VueController vc = new VueController(scheduler, m);

        // Register the view-controller as an observer of the model
        m.addObserver(vc);

        //VueController3D vc3d = new VueController3D(scheduler, m);
        //m.addObserver(vc3d);
        //vc3d.loop();
        //vc3d.shutdown();

        //scheduler.shutdown();
    }
}