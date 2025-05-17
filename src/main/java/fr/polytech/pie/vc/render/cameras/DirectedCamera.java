package fr.polytech.pie.vc.render.cameras;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DirectedCamera {
    public static final float fov = 70.0F;
    public static final float zNear = 0.1F;
    public static final float zFar = 100.0F;

    private float aspectRatio = 1.0F;
    private Vector3f target = new Vector3f(0.0F, 0.0F, 0.0F);

    private float horizontalAngle = 0.0F;
    private float verticalAngle = 0.0F;
    private float distanceFromTarget = 30.0F;

    public DirectedCamera() {
    }

    public DirectedCamera(float aspectRatio) {
        this();
        this.aspectRatio = aspectRatio;
    }

    public DirectedCamera(Vector3f target) {
        this();
        this.target = target;
    }

    public Vector3f computePos() {
        float x = target.x + distanceFromTarget * (float) Math.cos(verticalAngle) * (float) Math.sin(horizontalAngle);
        float y = target.y + distanceFromTarget * (float) Math.sin(verticalAngle);
        float z = target.z + distanceFromTarget * (float) Math.cos(verticalAngle) * (float) Math.cos(horizontalAngle);

        return new Vector3f(x, y, z);
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    @SuppressWarnings("unused")
    public Vector3f getTarget() {
        return target;
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    @SuppressWarnings("unused")
    public float getDistanceFromTarget() {
        return distanceFromTarget;
    }

    public void setDistanceFromTarget(float distanceFromTarget) {
        this.distanceFromTarget = distanceFromTarget;
        if (distanceFromTarget < 0.0F) {
            this.distanceFromTarget = 0.1F;
        }
    }

    public void addDistanceFromTarget(float distance) {
        setDistanceFromTarget(distanceFromTarget + distance);
    }

    public float getHorizontalAngle() {
        return horizontalAngle;
    }

    public void setHorizontalAngle(float horizontalAngle) {
        if (horizontalAngle > 2 * Math.PI) {
            horizontalAngle -= (float) (2 * Math.PI);
        } else if (horizontalAngle < 0.0F) {
            horizontalAngle += (float) (2 * Math.PI);
        }
        this.horizontalAngle = horizontalAngle;
    }

    public void addHorizontalAngle(float angle) {
        setHorizontalAngle(horizontalAngle + angle);
    }

    @SuppressWarnings("unused")
    public float getVerticalAngle() {
        return verticalAngle;
    }

    public void setVerticalAngle(float angle) {
        final float MAX_ANGLE = (float) (Math.PI / 2 - 0.01f); // Slightly less than 90° to prevent exact perpendicular viewing
        this.verticalAngle = Math.clamp(angle, -MAX_ANGLE, MAX_ANGLE);
    }

    public void addVerticalAngle(float v) {
        setVerticalAngle(verticalAngle + v);
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().perspective((float) (Math.toRadians(fov)), aspectRatio, zNear, zFar);
    }

    public Matrix4f getViewMatrix() {
        Vector3f pos = computePos();
        return new Matrix4f().lookAt(pos, target, new Vector3f(0.0F, 1.0F, 0.0F));
    }
}