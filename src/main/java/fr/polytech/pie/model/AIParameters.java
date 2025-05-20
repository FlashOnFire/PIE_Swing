package fr.polytech.pie.model;

public record AIParameters(double heightWeight, double linesWeight, double bumpinessWeight, double holesWeight) {
    public static final AIParameters DEFAULT = new AIParameters(
            -0.7303205229567257,
            0.6082323862482821,
            -0.22463833194827965,
            -0.21499515782089093
    );

    public static final AIParameters DEFAULT_3D = new AIParameters(
            -0.6500491536113875,
            0.5122503282774851,
            -0.1763291765999144,
            -0.5328636979081272
    );
}
