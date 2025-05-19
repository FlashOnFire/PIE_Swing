package fr.polytech.pie.model;

public interface Ai {
    /**
     * Makes the best move for the current piece based on the AI's evaluation
     * strategy
     *
     * @param currentPiece The piece to place
     * @param nextPiece  The next piece to place
     */
    void makeMove(CurrentPiece currentPiece, CurrentPiece nextPiece);
}
