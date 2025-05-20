package fr.polytech.pie.vc;

import fr.polytech.pie.model.*;

public interface Renderer {
    void initialize();
    LoopStatus loop();
    void update(Grid grid, Piece piece, Piece nextPiece, int score, boolean isGameOver);
    void cleanup();
}