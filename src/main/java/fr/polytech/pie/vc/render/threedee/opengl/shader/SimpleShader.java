package fr.polytech.pie.vc.render.threedee.opengl.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SimpleShader extends ShaderProgram {

    private static final String vertexShader = """
            #version 330
            
            layout(location = 0) in vec3 position;
            
            uniform vec3 pos;
            uniform mat4 projectionMatrix;
            uniform mat4 viewMatrix;
            
            void main(void) {
                vec3 worldPosition = position + pos;
                gl_Position = projectionMatrix * viewMatrix * vec4(worldPosition, 1.0);
            }
            """;
    private static final String fragmentShader = """
            #version 330
            
            uniform vec3 color;
            
            out vec4 out_color;
            
            void main(void) {
                out_color = vec4(color, 1.0);
            }
            """;

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_pos;
    private int location_color;

    @Override
    public void load() {
        super.loadProgram(vertexShader, fragmentShader);
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_pos = getUniformLocation("pos");
        location_color = getUniformLocation("color");
    }

    public void setProjectionMatrix(Matrix4f mat) {
        loadMat4(location_projectionMatrix, mat);
    }

    public void setViewMatrix(Matrix4f mat) {
        loadMat4(location_viewMatrix, mat);
    }

    public void setPos(Vector3f pos) {
        loadVector(location_pos, pos);
    }

    public void setColor(Vector3f color) {
        loadVector(location_color, color);
    }
}