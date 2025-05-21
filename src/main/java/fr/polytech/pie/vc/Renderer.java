package fr.polytech.pie.vc;

import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Piece;

public interface Renderer {
    void initialize();

    LoopStatus loop();

    void update(Grid grid, Piece piece, Piece nextPiece, long score, boolean isGameOver);

    void cleanup();
}