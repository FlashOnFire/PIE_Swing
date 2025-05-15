package fr.polytech.pie.vc.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

public class VertexArray {
    private final int id;
    private final ElementBuffer elementBuffer;
    private final List<VertexBuffer> vertexBuffers;

    public VertexArray(ElementBuffer elementBuffer) {
        this.id = GL30.glGenVertexArrays();
        this.elementBuffer = elementBuffer;
        this.vertexBuffers = new ArrayList<>();
        bind();
        bindElementArrayBuffer();
        unbind();
    }

    public void destroy() {
        GL30.glDeleteVertexArrays(this.id);
        for (VertexBuffer vertexBuffer : vertexBuffers) {
            vertexBuffer.destroy();
        }
        elementBuffer.destroy();
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
}