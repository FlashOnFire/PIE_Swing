package fr.polytech.pie.vc.render.cameras;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FreeCamCamera {
    public static final float fov = 70.0F;
    public static final float zNear = 0.1F;
    public static final float zFar = 50.0F;

    public static final float pitchLimit = (float) (Math.PI / 2.0 - 0.01);

    public float aspectRatio = 1.0F;
    private Vector3f pos;

    private float yaw = 0.0F;
    private float pitch = 0.0F;

    public FreeCamCamera() {
        this.pos = new Vector3f(0, 2.0F, 0);
        this.yaw = 0.0F;
        this.pitch = 0.0F;
    }

    public FreeCamCamera(float aspectRatio) {
        this();
        this.aspectRatio = aspectRatio;
    }

    public void move(float x, float y) {
        this.pos.x += x;
        this.pos.y += y;
    }

    public void rotate(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
    }

    public Vector3f getPos() {
        return pos;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getFov() {
        return fov;
    }

    public void setX(float x) {
        this.pos.x = x;
    }

    public void setY(float y) {
        this.pos.y = y;
    }

    public void setZ(float z) {
        this.pos.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        // Limit pitch to avoid flipping
        if (pitch > pitchLimit) {
            pitch = pitchLimit;
        } else if (pitch < -pitchLimit) {
            pitch = -pitchLimit;
        }

        this.pitch = pitch;
    }

    public void reset() {
        resetPosition();
        resetRotation();
    }

    public void resetPosition() {
        pos = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public void resetRotation() {
        yaw = 0.0f;
        pitch = 0.0f;
    }

    public void moveForward(float distance) {
        pos.x += distance * (float) Math.cos(yaw);
        pos.z += distance * (float) Math.sin(yaw);
    }

    public void moveBackwards(float distance) {
        moveForward(-distance);
    }

    public void moveRight(float distance) {
        pos.x += distance * (float) Math.cos(yaw + Math.PI / 2);
        pos.z += distance * (float) Math.sin(yaw + Math.PI / 2);
    }

    public void moveLeft(float distance) {
        moveRight(-distance);
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void moveUp(float distance) {
        pos.y += distance;
    }

    public void moveDown(float distance) {
        pos.y -= distance;
    }

    public void addYaw(float angle) {
        setYaw(yaw + angle);
    }

    public void addPitch(float angle) {
        setPitch(pitch + angle);
    }

    public void setPosition(Vector3f pos) {
        this.pos = pos;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void lookAt(Vector3f target) {
        Vector3f direction = new Vector3f(target).sub(pos).normalize();
        this.yaw = (float) Math.atan2(direction.z, direction.x);
        this.pitch = (float) Math.asin(direction.y);
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().perspective((float) (Math.toRadians(fov)), aspectRatio, zNear, zFar);
    }

    public Matrix4f getViewMatrix() {
        Vector3f forward = new Vector3f(
                (float) (Math.cos(yaw) * Math.cos(pitch)),
                (float) (Math.sin(pitch)),
                (float) (Math.sin(yaw) * Math.cos(pitch))
        );
        Vector3f target = new Vector3f(pos).add(forward);

        return new Matrix4f().lookAt(pos, target, new Vector3f(0.0F, 1.0F, 0.0F));
    }

}