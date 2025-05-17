package fr.polytech.pie.vc.render.cameras;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FreeCamCamera {
    public static final float fov = 70.0F;
    public static final float zNear = 0.1F;
    public static final float zFar = 50.0F;

    public static final float pitchLimit = (float) (Math.PI / 2.0 - 0.01);

    public float aspectRatio = 1.0F;
    private Vector3f pos;

    private float yaw;
    private float pitch;

    @Nullable
    private Matrix4f projectionMatrix;
    @Nullable
    private Matrix4f viewMatrix;

    public FreeCamCamera() {
        this.pos = new Vector3f(0, 2.0F, 0);
        this.yaw = 0.0F;
        this.pitch = 0.0F;
    }

    public FreeCamCamera(float aspectRatio) {
        this();
        this.aspectRatio = aspectRatio;
    }

    public void setPos(Vector3f pos) {
        this.pos = pos;
        this.viewMatrix = null;
    }

    public void setYaw(float yaw) {
        if (yaw > 2 * Math.PI) {
            yaw -= (float) (2 * Math.PI);
        } else if (yaw < 0.0F) {
            yaw += (float) (2 * Math.PI);
        }
        this.yaw = yaw;

        this.viewMatrix = null;
    }

    public void setPitch(float pitch) {
        // Limit pitch to avoid flipping
        this.pitch = Math.clamp(pitch, -pitchLimit, pitchLimit);
        this.viewMatrix = null;
    }

    public void moveForward(float distance) {
        pos.x += distance * (float) Math.cos(yaw);
        pos.z += distance * (float) Math.sin(yaw);

        if (distance != 0.0F) {
            this.viewMatrix = null;
        }
    }

    public void moveBackwards(float distance) {
        moveForward(-distance);
    }

    public void moveRight(float distance) {
        pos.x += distance * (float) Math.cos(yaw + Math.PI / 2);
        pos.z += distance * (float) Math.sin(yaw + Math.PI / 2);

        if (distance != 0.0F) {
            this.viewMatrix = null;
        }
    }

    public void moveLeft(float distance) {
        moveRight(-distance);
    }

    public void setAspectRatio(float aspectRatio) {
        if (this.aspectRatio != aspectRatio) {
            this.projectionMatrix = null;
        }

        this.aspectRatio = aspectRatio;
    }

    public void moveUp(float distance) {
        pos.y += distance;

        if (distance != 0.0F) {
            this.viewMatrix = null;
        }
    }

    public void moveDown(float distance) {
        moveUp(-distance);
    }

    public void addYaw(float angle) {
        setYaw(yaw + angle);
    }

    public void addPitch(float angle) {
        setPitch(pitch + angle);
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void lookAt(Vector3f target) {
        Vector3f direction = new Vector3f(target).sub(pos).normalize();

        setYaw((float) Math.atan2(direction.z, direction.x));
        setPitch((float) Math.asin(direction.y));

        this.viewMatrix = null;
    }

    public Matrix4f getProjectionMatrix() {
        if (projectionMatrix == null) {
            projectionMatrix = new Matrix4f().perspective((float) (Math.toRadians(fov)), aspectRatio, zNear, zFar);
        }

        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        if (viewMatrix == null) {
            Vector3f forward = new Vector3f(
                    (float) (Math.cos(yaw) * Math.cos(pitch)),
                    (float) (Math.sin(pitch)),
                    (float) (Math.sin(yaw) * Math.cos(pitch))
            );
            Vector3f target = new Vector3f(pos).add(forward);

            viewMatrix = new Matrix4f().lookAt(pos, target, new Vector3f(0.0F, 1.0F, 0.0F));
        }

        return viewMatrix;
    }

}
