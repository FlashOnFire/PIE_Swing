package fr.polytech.pie.vc;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;

public interface Renderer {
    void initialize();
    LoopStatus loop();
    void update(Grid grid, CurrentPiece currentPiece, int score);
    void cleanup();
}