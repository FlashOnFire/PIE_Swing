package fr.polytech.pie.vc.render.threedee.opengl.shader;


import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class ShaderProgram {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() {
        this.programId = 0;
        this.vertexShaderId = 0;
        this.fragmentShaderId = 0;
    }

    public void start() {
        GL30.glUseProgram(this.programId);
    }

    public void stop() {
        GL30.glUseProgram(0);
    }

    public void loadProgram(String vertexSource, String fragmentSource) {
        this.vertexShaderId = loadShader(vertexSource, GL30.GL_VERTEX_SHADER);
        this.fragmentShaderId = loadShader(fragmentSource, GL30.GL_FRAGMENT_SHADER);

        this.programId = GL30.glCreateProgram();
        GL30.glAttachShader(this.programId, vertexShaderId);
        GL30.glAttachShader(this.programId, this.fragmentShaderId);
        GL30.glLinkProgram(this.programId);
        GL30.glValidateProgram(this.programId);

        if (GL30.glGetProgrami(programId, GL30.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL30.glGetProgramInfoLog(programId, 1024));
        }

        getAllUniformLocations();
    }

    private int loadShader(String source, int type) {
        int shaderId = GL30.glCreateShader(type);

        GL30.glShaderSource(shaderId, source);
        GL30.glCompileShader(shaderId);

        IntBuffer compileSuccessful = BufferUtils.createIntBuffer(1);
        GL30.glGetShaderiv(shaderId, GL30.GL_COMPILE_STATUS, compileSuccessful);

        if (compileSuccessful.get() != 1) {
            System.out.println("Shader did not compile !");
            return -1;

        }

        return shaderId;
    }

    public abstract void load();

    public void destroy() {
        GL30.glDetachShader(this.programId, this.vertexShaderId);
        GL30.glDetachShader(this.programId, this.fragmentShaderId);
        GL30.glDeleteShader(this.vertexShaderId);
        GL30.glDeleteShader(this.fragmentShaderId);
        GL30.glDeleteProgram(this.programId);
    }

    protected abstract void getAllUniformLocations();

    protected int getUniformLocation(String uniformName) {
        int location = GL30.glGetUniformLocation(programId, uniformName);
        if (location == -1) {
            System.out.println("Uniform value not found !");
        }
        return location;
    }

    public void loadFloat(int location, float value) {
        GL30.glUniform1f(location, value);
    }

    @SuppressWarnings("unused")
    public void loadInt(int location, int value) {
        GL30.glUniform1i(location, value);
    }

    public void loadVector(int location, Vector3f vector) {
        GL30.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    public void loadMat4(int location, Matrix4f mat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = mat.get(stack.mallocFloat(16));
            GL30.glUniformMatrix4fv(location, false, buffer);
        }
    }
}