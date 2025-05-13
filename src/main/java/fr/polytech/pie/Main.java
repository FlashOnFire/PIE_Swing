package fr.polytech.pie;

import fr.polytech.pie.model.M;
import fr.polytech.pie.vc.VC;

public class Main {
    public static void main(String[] args) {
        M m = new M();
        VC vc = new VC(m);
        m.addObserver(vc);

        vc.setVisible(true);
    }
}