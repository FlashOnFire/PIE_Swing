package fr.polytech.pie.vc.render;

import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class VertexBuffer {
    private final int id;
    private final int dataStride;
    private final List<VertexAttribPointer> vertexAttribs = new ArrayList<>();

    public VertexBuffer(float[] data, int stride) {
        this.id = GL30.glGenBuffers();
        this.dataStride = stride;
        bind();
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data.length * 4L, GL30.GL_STATIC_DRAW);
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

    public void addVertexAttribPointer(int index, int coordinateSize, int offset) {
        VertexAttribPointer pointer = new VertexAttribPointer(index, coordinateSize, offset);
        this.vertexAttribs.add(pointer);
    }

    public void bindVertexAttribs() {
        for (VertexAttribPointer vertexAttribPointer : vertexAttribs) {
            GL30.glEnableVertexAttribArray(vertexAttribPointer.index());
            GL30.glVertexAttribPointer(
                    vertexAttribPointer.index(), vertexAttribPointer.size(), GL_FLOAT, false,
                    this.dataStride * 4, vertexAttribPointer.offset()
            );
        }
    }
}