package fr.polytech.pie.vc.render;

import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class VertexArray {
    private final int id;
    private ElementBuffer elementBuffer;
    private final List<VertexBuffer> vertexBuffers = new ArrayList<>();

    public VertexArray() {
        this.id = GL30.glGenVertexArrays();
        this.elementBuffer = null;
    }

    public VertexArray(ElementBuffer elementBuffer) {
        this();

        this.elementBuffer = elementBuffer;

        bind();
        bindElementArrayBuffer();
        unbind();
    }

    public void destroy() {
        GL30.glDeleteVertexArrays(this.id);

        for (VertexBuffer vertexBuffer : vertexBuffers) {
            vertexBuffer.destroy();
        }

        if (this.elementBuffer != null) {
            elementBuffer.destroy();
        }
    }

    public int getVertexCount() {
        return this.elementBuffer.getIndicesCount();
    }

    public void bindVertexBuffer(VertexBuffer buffer) {
        buffer.bind();
        buffer.bindVertexAttribs();
        this.vertexBuffers.add(buffer);
    }

    public void bind() {
        GL30.glBindVertexArray(this.id);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    private void bindElementArrayBuffer() {
        this.elementBuffer.bind();
    }

    public VertexBuffer getVertexBuffer(int i) {
        if (i < 0 || i >= vertexBuffers.size()) {
            throw new IndexOutOfBoundsException("VertexBuffer index out of bounds: " + i);
        }
        return vertexBuffers.get(i);
    }
}