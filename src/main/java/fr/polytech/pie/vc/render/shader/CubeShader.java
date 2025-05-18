package fr.polytech.pie.vc.render.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CubeShader extends ShaderProgram {

    private static final String vertexShader = """
            #version 330
            
            layout(location = 0) in vec3 position;
            layout(location = 1) in vec3 normal;
            
            uniform vec3 pos;
            uniform mat4 projectionMatrix;
            uniform mat4 viewMatrix;
            
            uniform vec3 lightPosition = vec3(0, 0, 0);
            
            out vec3 toLightVector;
            out vec3 toCameraVector;
            out vec3 surfaceNormal;
            
            void main(void) {
                vec3 worldPosition = position + pos;
                gl_Position = projectionMatrix * viewMatrix * vec4(worldPosition, 1.0);
            
                vec3 camPos = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
                toLightVector = lightPosition - worldPosition;
            
                toCameraVector = camPos - worldPosition;
                surfaceNormal = normal;
            }
            """;
    private static final String fragmentShader = """
            #version 330
            
            in vec3 toLightVector;
            in vec3 toCameraVector;
            in vec3 surfaceNormal;
            
            uniform vec3 color;
            uniform float brightness;
            
            out vec4 out_color;
            
            void main(void) {
                const float ambientStrength = 0.5;
                const float diffuseStrength = 0.5;
                const float specularStrength = 0.9;
                const float shininess = 16.0;
            
                vec3 unitNormal = normalize(surfaceNormal);
                vec3 unitLightVector = normalize(toLightVector);
                vec3 unitCameraVector = normalize(toCameraVector);
            
                float distance = length(toLightVector);
                float attenuation = 1.0 / (1.2 + 0.01 * distance + 0.001 * distance * distance);
            
                float ambient = ambientStrength;
            
                float diffuseFactor = max(dot(unitNormal, unitLightVector), 0.0);
                float diffuse = diffuseFactor * diffuseStrength;
            
                vec3 halfwayDir = normalize(unitLightVector + unitCameraVector);
                float specularFactor = pow(max(dot(unitNormal, halfwayDir), 0.0), shininess);
                float specular = specularFactor * specularStrength;
            
                float lighting = (ambient + (diffuse + specular) * attenuation) * brightness;
            
                vec3 result = color * lighting;
                out_color = vec4(result, 1.0);
            }
            """;

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_pos;
    private int location_color;
    private int location_lightPosition;
    private int location_brightness;

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
        location_lightPosition = getUniformLocation("lightPosition");
        location_brightness = getUniformLocation("brightness");
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

    public void setBrightness(float brightness) {
        loadFloat(location_brightness, brightness);
    }

    public void setLightPosition(Vector3f lightPos) {
        loadVector(location_lightPosition, lightPos);
    }
}