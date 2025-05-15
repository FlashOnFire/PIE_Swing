package fr.polytech.pie.model;

import java.util.HashSet;
import java.util.Set;

public abstract class Ai {
    protected double heightWeight = -0.7303205229567257;
    protected double linesWeight = 0.6082323862482821;
    protected double bumpinessWeight = -0.22463833194827965;
    protected double holesWeight = -0.21499515782089093;

    public Ai() {}

    public Ai(double[] parameters) {
        if (parameters.length != 4) {
            throw new IllegalArgumentException("Parameters must be of length 4");
        }
        this.heightWeight = parameters[0];
        this.linesWeight = parameters[1];
        this.bumpinessWeight = parameters[2];
        this.holesWeight = parameters[3];
    }

    /**
     * Makes the best move for the current piece based on the AI's evaluation
     * strategy
     *
     * @param currentPiece The piece to place
     */
    public abstract void makeMove(CurrentPiece currentPiece);
}
