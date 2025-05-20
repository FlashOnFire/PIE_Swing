package fr.polytech.pie.vc.render.threedee.text;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;

public record TextChar(int textureID, Vector2i size, Vector2i bearing, int advance) {

    public void destroy() {
        GL11.glDeleteTextures(textureID);
    }
}
