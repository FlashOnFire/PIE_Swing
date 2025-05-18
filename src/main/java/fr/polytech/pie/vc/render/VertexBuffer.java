package fr.polytech.pie.vc.render;

import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class VertexBuffer {
    private final int id;
    private final List<VertexAttribPointer> vertexAttribs = new ArrayList<>();

    private VertexBuffer() {
        this.id = GL30.glGenBuffers();
    }

    public VertexBuffer(int dataLength, int usage) {
        this();
        bind();
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, dataLength, usage);
        unbind();
    }

    public VertexBuffer(float[] data, int usage) {
        this();
        bind();
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data.length * 4L, usage);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, data);
        unbind();
    }

    public void storeData(float[] data) {
        bind();
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, data);
        unbind();
    }

    public void destroy() {
        GL30.glDeleteBuffers(id);
    }

    public void bind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.id);
    }

    public void unbind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    public void addVertexAttribPointer(int index, int size, int type, int stride, int offset) {
        VertexAttribPointer pointer = new VertexAttribPointer(index, size, type, stride, offset);
        this.vertexAttribs.add(pointer);
    }

    public void bindVertexAttribs() {
        for (VertexAttribPointer vertexAttribPointer : vertexAttribs) {
            GL30.glEnableVertexAttribArray(vertexAttribPointer.index());
            GL30.glVertexAttribPointer(
                    vertexAttribPointer.index(), vertexAttribPointer.size(), vertexAttribPointer.type(), false,
                    vertexAttribPointer.stride() * 4, vertexAttribPointer.offset()
            );
        }
    }
}