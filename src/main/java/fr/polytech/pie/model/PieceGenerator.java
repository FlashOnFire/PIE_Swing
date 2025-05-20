package fr.polytech.pie.model;

import fr.polytech.pie.model.twoD.Piece2D;
import fr.polytech.pie.model.threeD.Piece3D;

import java.util.Random;

public class PieceGenerator {
    private static final boolean[][][] PIECES_2D = {
            {{true, true, true}, {false, true, false}}, // T piece
            {{true, true}, {true, true}}, // O piece
            {{true, true, false}, {false, true, true}}, // S piece
            {{false, true, true}, {true, true, false}}, // Z piece
            {{true}, {true}, {true}, {true}}, // I piece
            {{false, false, true}, {true, true, true}}, // L piece
            {{true, false}, {true, false}, {true, true}} // J piece
    };

    private static final boolean[][][][] PIECES_3D = {
            {
                    {
                            {true, true},
                            {true, true}
                    },
                    {
                            {true, true},
                            {true, true}
                    }
            },
            {
                    {
                            {true, true, true}
                    }
            },
            {
                    {
                            {true, true, true, true}
                    }
            },
            {
                    {
                            {true}
                    },
                    {
                            {true}
                    },
                    {
                            {true}
                    }
            },
            {
                    {
                            {true, false},
                            {true, true}
                    }
            },
            {
                    {
                            {true, true, true},
                            {false, true, false}
                    }
            },
            {
                    {
                            {true}
                    },
                    {
                            {true}
                    }
            },
            {
                    {
                            {true, true},
                            {true, true}
                    }
            }
    };

    private static final Random random = new Random();

    public static Piece2D generatePiece2D(int maxX, int maxY) {
        int pieceIndex = random.nextInt(PIECES_2D.length);
        var piece = new Piece2D(PIECES_2D[pieceIndex], new Vector(new int[]{0, 0}));

        if (random.nextBoolean()) {
            rotate2DPieceRandomly(piece);
        }

        piece.getPosition().setX(random.nextInt(maxX - piece.getWidth() + 1));
        piece.getPosition().setY(maxY - piece.getHeight());

        return piece;
    }

    private static void rotate2DPieceRandomly(Piece2D piece) {
        int rotations = random.nextInt(4);

        if (rotations == 0) {
            return;
        }

        for (int i = 0; i < rotations; i++) {
            piece.rotate2d(_ -> false);
        }
    }

    public static Piece3D generate3DPiece(int maxX, int maxY, int maxZ) {
        int pieceIndex = random.nextInt(PIECES_3D.length);

        var piece = new Piece3D(PIECES_3D[pieceIndex], new Vector(new int[]{0, 0, 0}));
        if (random.nextBoolean()) {
            rotate3DPieceRandomly(piece);
        }


        piece.getPosition().setX(random.nextInt(maxX - piece.getWidth() + 1));
        piece.getPosition().setY(maxY - piece.getHeight());
        piece.getPosition().setZ(random.nextInt(maxZ - piece.getDepth() + 1));

        return piece;
    }

    private static void rotate3DPieceRandomly(Piece3D piece) {
        int rotations = random.nextInt(4);

        if (rotations == 0) {
            return;
        }


        for (int i = 0; i < rotations; i++) {
            int axis = random.nextInt(3);
            switch (axis) {
                case 0 -> piece.rotate3D(RotationAxis.X, _ -> false, false);
                case 1 -> piece.rotate3D(RotationAxis.Y, _ -> false, false);
                case 2 -> piece.rotate3D(RotationAxis.Z, _ -> false, false);
            }
        }
    }

}
