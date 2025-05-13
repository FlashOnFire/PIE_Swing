package fr.polytech.pie;

import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.VueController;

public class Main {
    public static void main(String[] args) {
        Model m = new Model();
        VueController vc = new VueController(m);
        m.addObserver(vc);

        vc.setVisible(true);
    }
}