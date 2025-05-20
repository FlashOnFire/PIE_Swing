package fr.polytech.pie.vc.render.threedee.opengl.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TextShader extends ShaderProgram {

    private static final String vertexShader = """
            #version 330 core
            
            layout (location = 0) in vec4 vertex; // vec2 pos vec2 tex
            out vec2 TexCoords;
            
            uniform mat4 projection;
            
            void main() {
                gl_Position = projection * vec4(vertex.xy, 0.0, 1.0);
                TexCoords = vertex.zw;
            }
            """;
    private static final String fragmentShader = """
            #version 330 core
            
            in vec2 TexCoords;
            out vec4 color;
            
            uniform sampler2D text;
            uniform vec3 textColor;
            
            void main() {
                vec4 sampled = vec4(1.0, 1.0, 1.0, texture(text, TexCoords).r);
                color = vec4(textColor, 1.0) * sampled;
            }
            """;

    private int location_projection;
    private int location_textColor;

    public void load() {
        super.loadProgram(vertexShader, fragmentShader);
    }

    @Override
    protected void getAllUniformLocations() {
        location_projection = getUniformLocation("projection");
        location_textColor = getUniformLocation("textColor");
    }

    public void setProjectionMatrix(Matrix4f mat) {
        loadMat4(location_projection, mat);
    }

    public void setColor(Vector3f color) {
        loadVector(location_textColor, color);
    }
}