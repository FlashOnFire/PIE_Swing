package fr.polytech.pie.vc.render;

import fr.polytech.pie.Consts;
import fr.polytech.pie.vc.render.shader.CubeShader;
import fr.polytech.pie.vc.render.shader.SimpleShader;
import fr.polytech.pie.vc.render.text.TextRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class OpenGLRenderer {
    private final CubeShader cubeShader = new CubeShader();
    private final SimpleShader simpleShader = new SimpleShader();
    private final TextRenderer textRenderer = new TextRenderer();

    private VertexArray cubeVao;
    private VertexArray boxVAO;

    private int fontID;

    List<Cube> cubes = new ArrayList<>();

    public void init() {
        cubeShader.load();
        simpleShader.load();
        cubeVao = RenderUtils.initCubeVAO();
        boxVAO = RenderUtils.initWireframeBoxVAO(new Vector3f(Consts.GRID_WIDTH, Consts.GRID_HEIGHT, Consts.GRID_DEPTH));
        textRenderer.init();

        fontID = textRenderer.loadFont(TextRenderer.getResourceFontPath("arial.ttf"), 48);
    }

    public void destroy() {
        this.cubeShader.destroy();
        this.cubeVao.destroy();
        this.simpleShader.destroy();
        this.boxVAO.destroy();
        this.textRenderer.destroy();
    }

    public void render(long window, Vector2i screenSize, Matrix4f projectionMatrix, Matrix4f viewMatrix, int score, boolean isGameOver, float elapsedTimeSinceGameOver) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        drawCubes(projectionMatrix, viewMatrix, cubes, isGameOver);
        renderPlayingBox(projectionMatrix, viewMatrix, new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F));

        renderScore(screenSize, score);

        if (isGameOver) {
            textRenderer.renderText(fontID, new Vector2i(screenSize.x / 2 - (248) / 2, screenSize.y - 100), screenSize, 1.0F, new Vector3f(1.0F, 0.0F, 0.0F), "Game Over");
            textRenderer.renderText(fontID, new Vector2i(screenSize.x / 2 - (477) / 2, 100), screenSize, 0.6F, new Vector3f(0.0F, 0.0F, 0.0F), "Returning to main menu in " + (int) Math.max(5 - elapsedTimeSinceGameOver, 0) + " seconds...");
        }

        glfwSwapBuffers(window);
    }

    public void drawCubes(Matrix4f projectionMatrix, Matrix4f viewMatrix, List<Cube> cubes, boolean isGameOver) {
        this.cubeShader.start();

        this.cubeShader.setProjectionMatrix(projectionMatrix);
        this.cubeShader.setViewMatrix(viewMatrix);
        this.cubeShader.setLightPosition(new Vector3f(Consts.GRID_WIDTH / 2.0F, Consts.GRID_HEIGHT + 15.0F, Consts.GRID_DEPTH / 2.0F));
        this.cubeShader.setBrightness(isGameOver ? 0.4F : 1.0F);

        cubeVao.bind();
        for (Cube cube : cubes) {
            this.cubeShader.setPos(cube.getPosition());
            this.cubeShader.setColor(cube.getColor());
            GL30.glDrawElements(GL30.GL_TRIANGLES, cubeVao.getVertexCount(), GL_UNSIGNED_INT, 0);
        }
        cubeVao.unbind();

        this.cubeShader.stop();
    }

    public void renderPlayingBox(Matrix4f projectionMatrix, Matrix4f viewMatrix, Vector3f pos, Vector3f color) {
        this.simpleShader.start();

        this.simpleShader.setProjectionMatrix(projectionMatrix);
        this.simpleShader.setViewMatrix(viewMatrix);
        this.simpleShader.setPos(pos);
        this.simpleShader.setColor(color);

        boxVAO.bind();
        GL30.glDrawElements(GL30.GL_LINES, boxVAO.getVertexCount(), GL_UNSIGNED_INT, 0);
        boxVAO.unbind();

        this.simpleShader.stop();
    }

    public void renderScore(Vector2i screenSize, int score) {
        textRenderer.renderText(fontID, new Vector2i(10, screenSize.y - 30), screenSize, 0.5F, new Vector3f(0.0F, 0.0F, 0.0F), "Score: " + score);
    }

    public void updateCubes(List<Cube> cubes) {
        this.cubes = cubes;
    }
}