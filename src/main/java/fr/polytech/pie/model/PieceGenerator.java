package fr.polytech.pie.model;

import java.util.Random;

public class PieceGenerator {
private static final boolean[][][] PIECES = {
    {{true, true, true}, {false, true, false}}, // T piece
    {{true, true}, {true, true}},              // O piece
    {{true, true, false}, {false, true, true}}, // S piece
    {{false, true, true}, {true, true, false}}, // Z piece
    {{true}, {true}, {true}, {true}},          // I piece
    {{false, false, true}, {true, true, true}}, // L piece
    {{true, false}, {true, false}, {true, true}} // J piece
};

    private static final Random random = new Random();

    public static CurrentPiece generatePiece(int maxX) {
        int pieceIndex = new Random().nextInt(PIECES.length);
        boolean[][] selectedPiece = PIECES[pieceIndex];

        int pieceWidth = selectedPiece[0].length;
        int x = random.nextInt(maxX - pieceWidth + 1);
        int y = 0;// random.nextInt(maxY - pieceHeight - 3, maxY - pieceHeight + 1);

        return new CurrentPiece(selectedPiece, x, y);
    }
}
