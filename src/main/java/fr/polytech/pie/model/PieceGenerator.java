package fr.polytech.pie.model;

import fr.polytech.pie.model.TwoD.CurrentPiece2D;
import fr.polytech.pie.model.ThreeD.CurrentPiece3D;

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
                            {true, true, true},
                            {false, true, false}
                    },
                    {
                            {false, true, false},
                            {false, true, false}
                    }
            },

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
                            {true, true, false},
                            {false, true, true}
                    },
                    {
                            {false, true, false},
                            {true, true, false}
                    }
            },

            {
                    {
                            {false, true, true},
                            {true, true, false}
                    },
                    {
                            {true, false, false},
                            {false, true, false}
                    }
            },

            {
                    {
                            {true, true, true, true}
                    },
                    {
                            {true, true, true, true}
                    }
            },

            {
                    {
                            {true, false, false},
                            {true, true, true}
                    },
                    {
                            {true, false, false},
                            {false, false, false}
                    }
            },

            {
                    {
                            {false, false, true},
                            {true, true, true}
                    },
                    {
                            {false, false, true},
                            {false, false, false}
                    }
            },

            {
                    {
                            {false, true, false},
                            {true, true, true},
                            {false, true, false}
                    },
                    {
                            {false, true, false},
                            {false, true, false},
                            {false, true, false}
                    }
            },

            {
                    {
                            {false, false, false},
                            {false, true, false},
                            {false, false, false}
                    },
                    {
                            {false, true, false},
                            {true, true, true},
                            {false, true, false}
                    },
                    {
                            {false, false, false},
                            {false, true, false},
                            {false, false, false}
                    }
            }
    };

    private static final Random random = new Random();

    public static CurrentPiece2D generatePiece2D(int maxX, int maxY) {
            int pieceIndex = random.nextInt(PIECES_2D.length);
            boolean[][] selectedPiece = PIECES_2D[pieceIndex];

            int pieceWidth = selectedPiece[0].length;
            int x = random.nextInt(maxX - pieceWidth + 1);
            int y = maxY - selectedPiece.length;

            return new CurrentPiece2D(selectedPiece, x, y);
    }

    public static CurrentPiece3D generate3DPiece(int maxX, int maxY, int maxZ) {
        int pieceIndex = random.nextInt(PIECES_3D.length);
        boolean[][][] selected3DPiece = PIECES_3D[pieceIndex];

        int width = selected3DPiece[0][0].length;
        int height = selected3DPiece[0].length;
        int depth = selected3DPiece.length;

        int x = random.nextInt(maxX - width + 1);
        int y = maxY - height;
        int z = random.nextInt(maxZ - depth + 1); // Random z-position

        // Apply random rotation before placing the piece
        var piece = new CurrentPiece3D(selected3DPiece, x, y, z);
        if (random.nextBoolean()) {
            rotate3DPieceRandomly(piece);
        }

        return piece;
    }

    private static void rotate3DPieceRandomly(CurrentPiece3D piece) {
        int rotations = random.nextInt(4); // 0, 90, 180, or 270 degrees

        if (rotations == 0) {
            return ; // No rotation
        }


        for (int i = 0; i < rotations; i++) {
            int axis = random.nextInt(3); // 0=X, 1=Y, 2=Z
            switch (axis) {
                case 0 -> piece.rotate3D(RotationAxis.X, _ ->false);
                case 1 -> piece.rotate3D(RotationAxis.Y, _ ->false);
                case 2 -> piece.rotate3D(RotationAxis.Z, _ ->false);
            }
        }
    }

}
