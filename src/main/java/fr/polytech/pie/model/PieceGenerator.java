package fr.polytech.pie.model;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.DD.CurrentPiece2D;
import fr.polytech.pie.model.DDD.CurrentPiece3D;

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

    public static CurrentPiece generatePiece(int maxX, boolean is3D) {
        if (is3D) {
            return generateTrue3DPiece(maxX);
        } else {
            int pieceIndex = random.nextInt(PIECES_2D.length);
            boolean[][] selectedPiece = PIECES_2D[pieceIndex];

            int pieceWidth = selectedPiece[0].length;
            int x = random.nextInt(maxX - pieceWidth + 1);
            int y = 0;

            return new CurrentPiece2D(selectedPiece, x, y);
        }
    }

    public static CurrentPiece3D generateTrue3DPiece(int maxX) {
        int pieceIndex = random.nextInt(PIECES_3D.length);
        boolean[][][] selected3DPiece = PIECES_3D[pieceIndex];

        // Apply random rotation before placing the piece
        if (random.nextBoolean()) {
            selected3DPiece = rotate3DPieceRandomly(selected3DPiece);
        }

        int width = selected3DPiece[0].length;
        int depth = selected3DPiece[0][0].length;

        int x = random.nextInt(maxX - width + 1);
        int y = 0;
        int z = random.nextInt(Consts.GRID_DEPTH - depth + 1); // Random z-position

        return new CurrentPiece3D(selected3DPiece, x, y, z);
    }

    private static boolean[][][] rotate3DPieceRandomly(boolean[][][] piece) {
        int axis = random.nextInt(3); // 0=X, 1=Y, 2=Z

        int rotations = random.nextInt(4); // 0, 90, 180, or 270 degrees

        if (rotations == 0) {
            return piece; // No rotation
        }

        boolean[][][] rotatedPiece = piece;

        for (int i = 0; i < rotations; i++) {
            rotatedPiece = switch (axis) {
                case 0 -> rotateAroundX(rotatedPiece);
                case 1 -> rotateAroundY(rotatedPiece);
                case 2 -> rotateAroundZ(rotatedPiece);
                default -> rotatedPiece;
            };
        }

        return rotatedPiece;
    }

    private static boolean[][][] rotateAroundX(boolean[][][] piece) {
        int height = piece.length;
        int width = piece[0].length;
        int depth = piece[0][0].length;

        boolean[][][] rotated = new boolean[depth][width][height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < depth; k++) {
                    rotated[depth - 1 - k][j][i] = piece[i][j][k];
                }
            }
        }

        return rotated;
    }

    private static boolean[][][] rotateAroundY(boolean[][][] piece) {
        int height = piece.length;
        int width = piece[0].length;
        int depth = piece[0][0].length;

        boolean[][][] rotated = new boolean[height][depth][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < depth; k++) {
                    rotated[i][k][width - 1 - j] = piece[i][j][k];
                }
            }
        }

        return rotated;
    }

    private static boolean[][][] rotateAroundZ(boolean[][][] piece) {
        int height = piece.length;
        int width = piece[0].length;
        int depth = piece[0][0].length;

        boolean[][][] rotated = new boolean[width][height][depth];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.arraycopy(piece[i][j], 0, rotated[j][height - 1 - i], 0, depth);
            }
        }

        return rotated;
    }
}
