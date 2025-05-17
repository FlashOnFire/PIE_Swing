package fr.polytech.pie.vc.render;

import org.lwjgl.opengl.GL30;

public class ElementBuffer {
    private final int id;
    private final int indicesCount;

    public ElementBuffer(int[] indices) {
        this.indicesCount = indices.length;
        this.id = GL30.glGenBuffers();

        bind();
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4L, GL30.GL_STATIC_DRAW);
        GL30.glBufferSubData(GL30.GL_ELEMENT_ARRAY_BUFFER, 0, indices);
        unbind();
    }

    public void destroy() {
        GL30.glDeleteBuffers(this.id);
    }

    public void bind() {
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.id);
    }

    public void unbind() {
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getIndicesCount() {
        return this.indicesCount;
    }
}