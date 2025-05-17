package fr.polytech.pie.vc.render;

import fr.polytech.pie.Consts;
import fr.polytech.pie.vc.render.shader.CubeShader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

public class OpenGLRenderer {
    private final CubeShader cubeShader = new CubeShader();
    private VertexArray cubeVao;

    List<Cube> cubes = new ArrayList<>();

    public void init() {
        cubeShader.load();
        cubeVao = RenderUtils.initCubeVAO();
    }

    public void destroy() {
        this.cubeShader.destroy();
        this.cubeVao.destroy();
    }

    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        drawCubes(projectionMatrix, viewMatrix, cubes);
    }

    public void drawCubes(Matrix4f projectionMatrix, Matrix4f viewMatrix, List<Cube> cubes) {
        this.cubeShader.start();
        this.cubeShader.setProjectionMatrix(projectionMatrix);
        this.cubeShader.setViewMatrix(viewMatrix);
        this.cubeShader.setLightPosition(new Vector3f(Consts.GRID_WIDTH / 2.0F, Consts.GRID_HEIGHT + 15.0F, Consts.GRID_DEPTH / 2.0F));

        cubeVao.bind();
        for (Cube cube : cubes) {
            this.cubeShader.setPos(cube.getPosition());
            this.cubeShader.setColor(cube.getColor());
            GL30.glDrawElements(GL30.GL_TRIANGLES, cubeVao.getVertexCount(), GL_UNSIGNED_INT, 0);
        }
        cubeVao.unbind();

        this.cubeShader.stop();
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

        this.cubeShader.start();
        this.cubeShader.setPos(pos);
        this.cubeShader.setColor(color);

        cubeVao.bind();
        // this.shader.setModelTransform(new Vector3f(width, height, depth));

        GL30.glDrawElements(GL30.GL_TRIANGLES, cubeVao.getVertexCount(), GL_UNSIGNED_INT, 0);
        cubeVao.unbind();

        // Reset model matrix
        //this.shader.setModelTransform(new Vector3f(1.0f, 1.0f, 1.0f));

        // Restore the original polygon mode
        GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, currentMode[0]);
    }

    public void updateCubes(List<Cube> cubes) {
        this.cubes = cubes;
    }
}