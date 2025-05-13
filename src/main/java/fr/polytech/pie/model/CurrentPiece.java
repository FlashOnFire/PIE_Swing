package fr.polytech.pie.model;

import java.util.function.Predicate;

public class CurrentPiece {
    private boolean[][] piece;
    private int x;
    private int y;

    public CurrentPiece(boolean[][] piece, int x, int y) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }

    public boolean[][] getPiece() {
        return piece;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return piece[0].length;
    }

    public int getHeight() {
        return piece.length;
    }

    public void rotate(Predicate<CurrentPiece> collisionCheck) {
            int width = getWidth();
            int height = getHeight();
            boolean[][] rotatedPiece = new boolean[width][height];

            // Rotation de 90 degrés dans le sens horaire
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    rotatedPiece[j][height-1-i] = piece[i][j];
                }
            }

            // Sauvegarde de la pièce originale au cas où
            boolean[][] originalPiece = piece;
            int originalX = x;
            int originalY = y;

            // Appliquer la rotation
            this.piece = rotatedPiece;

            // Ajustement de position pour la pièce I (barre)
            // La pièce I a un point de rotation différent des autres pièces
            if ((width == 4 && height == 1) || (width == 1 && height == 4)) {
                // Ajustement de la position après rotation de la barre I
                if (width == 4 && height == 1) {
                    // Horizontal -> Vertical : décaler vers la gauche et vers le haut
                    x += 1;
                    y -= 2;
                } else {
                    // Vertical -> Horizontal : décaler vers la droite et vers le bas
                    x -= 1;
                    y += 2;
                }
            }

            // Points de test SRS pour les ajustements de position
            int[][] testPoints;

            // Test spécifique pour la pièce I (barre)
            if ((width == 4 && height == 1) || (width == 1 && height == 4)) {
                // Points de test supplémentaires pour la barre
                testPoints = new int[][] {
                    {0, 0},     // Position ajustée
                    {-1, 0},    // Gauche
                    {2, 0},     // Droite (plus loin)
                    {-2, 0},    // Gauche (plus loin)
                    {1, 0},     // Droite
                    {0, 1},     // Bas
                    {0, -1},    // Haut
                    {0, 2},     // Bas (plus loin)
                    {0, -2}     // Haut (plus loin)
                };
            } else {
                // Points de test standard pour les autres pièces
                testPoints = new int[][] {
                    {0, 0},     // Position originale
                    {1, 0},     // Décalage à droite
                    {-1, 0},    // Décalage à gauche
                    {0, -1},    // Décalage vers le haut
                    {1, -1},    // Droite et haut
                    {-1, -1},   // Gauche et haut
                    {0, 1}      // Décalage vers le bas
                };
            }

            // Si la rotation cause une collision, essayer les points de test
            boolean rotationSuccessful = false;
            for (int[] offset : testPoints) {
                // Appliquer le décalage temporairement
                int tempX = x + offset[0];
                int tempY = y + offset[1];
                x = tempX;
                y = tempY;

                // Vérifier si cette position est valide
                if (!collisionCheck.test(this)) {
                    rotationSuccessful = true;
                    break;
                }

                // Restaurer la position originale pour le prochain test
                x = originalX;
                y = originalY;
            }

            // Si aucun test ne fonctionne, annuler la rotation
            if (!rotationSuccessful) {
                this.piece = originalPiece;
                this.x = originalX;
                this.y = originalY;
            }
        }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}
