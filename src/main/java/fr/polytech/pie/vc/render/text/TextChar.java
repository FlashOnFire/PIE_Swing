package fr.polytech.pie.vc.render.text;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;

public class TextChar {
    private final int textureID;
    private final Vector2i size;
    private final Vector2i bearing;
    private final int advance;

    public TextChar(int textureID, Vector2i size, Vector2i bearing, int advance) {
        this.textureID = textureID;
        this.size = size;
        this.bearing = bearing;
        this.advance = advance;
    }

    public void destroy() {
        GL11.glDeleteTextures(textureID);
    }

    public int getTextureID() {
        return textureID;
    }

    public Vector2i getSize() {
        return size;
    }

    public Vector2i getBearing() {
        return bearing;
    }

    public int getAdvance() {
        return advance;
    }
}
