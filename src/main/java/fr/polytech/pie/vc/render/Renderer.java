package fr.polytech.pie.vc.render;

import fr.polytech.pie.vc.render.shader.BaseShader;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

public class Renderer {
    private final BaseShader shader;
    private VertexArray cubeVao;

    public Renderer() {
        this.shader = new BaseShader();
    }

    public void init() {
        shader.load();
        initCube();
    }

    public void destroy() {
        this.shader.destroy();
        this.cubeVao.destroy();
    }

    private float[] getCubePositions() {
        // A cube has 8 vertices
        float[] positions = new float[8 * 3]; // 8 vertices, 3 coordinates each

        // Use fixed size
        Vector3f size = new Vector3f(1.0F, 1.0F, 1.0F);

        // Calculate the corner positions directly
        float minX = 0.0F;
        float maxX = size.x;
        float minY = 0.0F;
        float maxY = size.y;
        float minZ = 0.0F;
        float maxZ = size.z;

        // Front face (z = minZ)
        positions[0] = minX;
        positions[1] = minY;
        positions[2] = minZ; // bottom-left
        positions[3] = maxX;
        positions[4] = minY;
        positions[5] = minZ; // bottom-right
        positions[6] = minX;
        positions[7] = maxY;
        positions[8] = minZ; // top-left
        positions[9] = maxX;
        positions[10] = maxY;
        positions[11] = minZ; // top-right

        // Back face (z = maxZ)
        positions[12] = minX;
        positions[13] = minY;
        positions[14] = maxZ; // bottom-left
        positions[15] = maxX;
        positions[16] = minY;
        positions[17] = maxZ; // bottom-right
        positions[18] = minX;
        positions[19] = maxY;
        positions[20] = maxZ; // top-left
        positions[21] = maxX;
        positions[22] = maxY;
        positions[23] = maxZ; // top-right

        return positions;
    }

    private int[] getCubeIndices() {
        return new int[]{
                // Front face (CCW winding)
                0, 2, 1,
                1, 2, 3,
                // Back face (CCW winding when viewed from back)
                5, 7, 4,
                4, 7, 6,
                // Bottom face (CCW winding when viewed from below)
                0, 1, 4,
                4, 1, 5,
                // Top face (CCW winding when viewed from above)
                2, 6, 3,
                3, 6, 7,
                // Left face (CCW winding when viewed from left)
                0, 4, 2,
                2, 4, 6,
                // Right face (CCW winding when viewed from right)
                1, 3, 5,
                3, 7, 5
        };
    }

    private float[] getCubeNormals() {
        float[] normals = new float[8 * 3]; // 8 vertices, 3 coordinates each

        // vertex 0 (bottom-left-front): average of -x, -y, -z normals
        normals[0] = -1.0f; normals[1] = -1.0f; normals[2] = -1.0f;

        // vertex 1 (bottom-right-front): average of +x, -y, -z normals
        normals[3] = 1.0f; normals[4] = -1.0f; normals[5] = -1.0f;

        // vertex 2 (top-left-front): average of -x, +y, -z normals
        normals[6] = -1.0f; normals[7] = 1.0f; normals[8] = -1.0f;

        // vertex 3 (top-right-front): average of +x, +y, -z normals
        normals[9] = 1.0f; normals[10] = 1.0f; normals[11] = -1.0f;

        // vertex 4 (bottom-left-back): average of -x, -y, +z normals
        normals[12] = -1.0f; normals[13] = -1.0f; normals[14] = 1.0f;

        // vertex 5 (bottom-right-back): average of +x, -y, +z normals
        normals[15] = 1.0f; normals[16] = -1.0f; normals[17] = 1.0f;

        // vertex 6 (top-left-back): average of -x, +y, +z normals
        normals[18] = -1.0f; normals[19] = 1.0f; normals[20] = 1.0f;

        // vertex 7 (top-right-back): average of +x, +y, +z normals
        normals[21] = 1.0f; normals[22] = 1.0f; normals[23] = 1.0f;

        // Normalize the normals
        for (int i = 0; i < 8; i++) {
            int idx = i * 3;
            float x = normals[idx];
            float y = normals[idx + 1];
            float z = normals[idx + 2];
            float length = (float) Math.sqrt(x * x + y * y + z * z);
            normals[idx] = x / length;
            normals[idx + 1] = y / length;
            normals[idx + 2] = z / length;
        }

        return normals;
    }

    private void initCube() {
        ElementBuffer eBuffer = new ElementBuffer(getCubeIndices());
        this.cubeVao = new VertexArray(eBuffer);

        VertexBuffer positionBuffer = new VertexBuffer(getCubePositions(), 3);
        positionBuffer.addVertexAttribPointer(0, 3, 0);
        VertexBuffer normalsBuffer = new VertexBuffer(getCubeNormals(), 3);
        normalsBuffer.addVertexAttribPointer(1, 3, 0);

        this.cubeVao.bind();
        this.cubeVao.bindVertexBuffer(positionBuffer);
        this.cubeVao.bindVertexBuffer(normalsBuffer);
        this.cubeVao.unbind();
    }

    public void render(Camera cam) {
        Vector3f[] cubes = {
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Vector3f(1.0F, 0.0F, 0.0F),
                new Vector3f(0.0F, 1.0F, 0.0F),
                new Vector3f(0.0F, 0.0F, 1.0F)
        };

        Vector3f[] colors = {
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Vector3f(1.0F, 0.0F, 0.0F),
                new Vector3f(0.0F, 1.0F, 0.0F),
                new Vector3f(0.0F, 0.0F, 1.0F)
        };

        this.shader.start();
        this.shader.setProjectionMatrix(cam.getPerspectiveMatrix());
        this.shader.setViewMatrix(cam.getViewMatrix());

        drawCubes(cubes, colors);
    }

    public void renderVao(VertexArray vertexArray) {
        this.shader.start();
        vertexArray.bind();
        GL30.glDrawElements(GL30.GL_TRIANGLES, vertexArray.getVertexCount(), GL_UNSIGNED_INT, 0);
        vertexArray.unbind();
    }

    public void drawCubes(Vector3f[] pos, Vector3f[] colors) {
        cubeVao.bind();
        for (int i = 0; i < pos.length; i++) {
            Vector3f p = pos[i];
            this.shader.setPos(p);
            this.shader.setColor(colors[i]);
            GL30.glDrawElements(GL30.GL_TRIANGLES, cubeVao.getVertexCount(), GL_UNSIGNED_INT, 0);
        }
        cubeVao.unbind();
    }

    public void renderCube(Vector3f pos, Vector3f color) {
        this.shader.setPos(pos);
        this.shader.setColor(color);
        renderVao(cubeVao);
    }

    public void renderPlayingBox(Vector3f pos, Vector3f color) {
        float width = 180.0f;
        float depth = 180.0f;
        float height = 200.0f;
        float lineThickness = 1.0f;

        // Save the current polygon mode
        int[] currentMode = new int[1];
        GL30.glGetIntegerv(GL30.GL_POLYGON_MODE, currentMode);

        // Set wireframe mode
        GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);

        this.shader.start();
        this.shader.setPos(pos);
        this.shader.setColor(color);

        cubeVao.bind();
        // this.shader.setModelTransform(new Vector3f(width, height, depth));

        GL30.glDrawElements(GL30.GL_TRIANGLES, cubeVao.getVertexCount(), GL_UNSIGNED_INT, 0);
        cubeVao.unbind();

        // Reset model matrix
        //this.shader.setModelTransform(new Vector3f(1.0f, 1.0f, 1.0f));

        // Restore the original polygon mode
        GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, currentMode[0]);
    }
}