package fr.polytech.pie;

import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.VueController;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        try {
            InputStream audioSrc = Main.class.getResourceAsStream("/sounds/tetris-tek.wav");
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

        VueController vc = new VueController(m);
        vc.loop();
        vc.cleanup();
        scheduler.shutdownNow();
    }
}