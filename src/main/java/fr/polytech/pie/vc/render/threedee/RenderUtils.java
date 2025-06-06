package fr.polytech.pie.vc.render.threedee;

import fr.polytech.pie.vc.render.threedee.opengl.ElementBuffer;
import fr.polytech.pie.vc.render.threedee.opengl.VertexArray;
import fr.polytech.pie.vc.render.threedee.opengl.VertexBuffer;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class RenderUtils {
    public static VertexArray initCubeVAO() {
        ElementBuffer elementBuffer = new ElementBuffer(getCubeIndices());
        VertexArray cubeVao = new VertexArray(elementBuffer);

        VertexBuffer positionBuffer = new VertexBuffer(getBoxPositions(new Vector3f(1.0F, 1.0F, 1.0F)), GL30.GL_STATIC_DRAW);
        positionBuffer.addVertexAttribPointer(0, 3, GL_FLOAT, 3, 0);
        VertexBuffer normalsBuffer = new VertexBuffer(getCubeNormals(), GL30.GL_STATIC_DRAW);
        normalsBuffer.addVertexAttribPointer(1, 3, GL_FLOAT, 3, 0);

        cubeVao.bind();
        cubeVao.bindVertexBuffer(positionBuffer);
        cubeVao.bindVertexBuffer(normalsBuffer);
        cubeVao.unbind();

        return cubeVao;
    }

    public static VertexArray initWireframeBoxVAO(Vector3f size) {
        ElementBuffer elementBuffer = new ElementBuffer(getBoxWireframeIndices());
        VertexArray boxVAO = new VertexArray(elementBuffer);

        VertexBuffer positionBuffer = new VertexBuffer(getBoxPositions(size), GL30.GL_STATIC_DRAW);
        positionBuffer.addVertexAttribPointer(0, 3, GL_FLOAT, 3, 0);

        boxVAO.bind();
        boxVAO.bindVertexBuffer(positionBuffer);
        boxVAO.unbind();

        return boxVAO;
    }

    private static float[] getBoxPositions(Vector3f size) {
        float[] positions = new float[8 * 3]; // 8 vertices, 3 coordinates each

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

    private static int[] getCubeIndices() {
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
                // Left face (CCW winding when viewed from the left)
                0, 4, 2,
                2, 4, 6,
                // Right face (CCW winding when viewed from right)
                1, 3, 5,
                3, 7, 5
        };
    }

    private static float[] getCubeNormals() {
        float[] normals = new float[8 * 3]; // 8 vertices, 3 coordinates each

        // vertex 0 (bottom-left-front)
        normals[0] = -1.0f;
        normals[1] = -1.0f;
        normals[2] = -1.0f;

        // vertex 1 (bottom-right-front)
        normals[3] = 1.0f;
        normals[4] = -1.0f;
        normals[5] = -1.0f;

        // vertex 2 (top-left-front)
        normals[6] = -1.0f;
        normals[7] = 1.0f;
        normals[8] = -1.0f;

        // vertex 3 (top-right-front)
        normals[9] = 1.0f;
        normals[10] = 1.0f;
        normals[11] = -1.0f;

        // vertex 4 (bottom-left-back)
        normals[12] = -1.0f;
        normals[13] = -1.0f;
        normals[14] = 1.0f;

        // vertex 5 (bottom-right-back)
        normals[15] = 1.0f;
        normals[16] = -1.0f;
        normals[17] = 1.0f;

        // vertex 6 (top-left-back)
        normals[18] = -1.0f;
        normals[19] = 1.0f;
        normals[20] = 1.0f;

        // vertex 7 (top-right-back)
        normals[21] = 1.0f;
        normals[22] = 1.0f;
        normals[23] = 1.0f;

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

    public static int[] getBoxWireframeIndices() {
        return new int[]{
                0, 1,
                1, 3,
                3, 2,
                2, 0,
                4, 5,
                5, 7,
                7, 6,
                6, 4,
                0, 4,
                1, 5,
                2, 6,
                3, 7
        };
    }
}
