package fr.polytech.pie.vc.render.cameras;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {
    private static final float speed = 1.0F;
    private static final float mouseSensitivity = 0.1F;
    private static final float mouseScrollSensitivity = 1.2F;

    private DirectedCamera directedCamera;
    private FreeCamCamera freeCam;
    private boolean isFreeCam = false;

    private double lastX = 0.0;
    private double lastY = 0.0;
    private boolean firstMouse = true;

    private Vector3f directedCamTarget = new Vector3f(0, 0, 0);


    public CameraController(boolean isFreeCam, float aspectRatio) {
        this.isFreeCam = isFreeCam;
        if (isFreeCam) {
            this.freeCam = new FreeCamCamera(aspectRatio);
        } else {
            this.directedCamera = new DirectedCamera(aspectRatio);
        }
    }

    public boolean isFreeCam() {
        return isFreeCam;
    }

    public void switchToFreeCam() {
        isFreeCam = true;
        freeCam = new FreeCamCamera();
        if (directedCamera != null) {
            freeCam.setAspectRatio(directedCamera.getAspectRatio());
            freeCam.setX(directedCamera.getPos().x);
            freeCam.setY(directedCamera.getPos().y);
            freeCam.setZ(directedCamera.getPos().z);
            directedCamera = null;
        }
    }

    public void switchToDirectedCam() {
        isFreeCam = false;
        directedCamera = new DirectedCamera();
        if (freeCam != null) {
            directedCamera.setAspectRatio(freeCam.getAspectRatio());
            directedCamera.setTarget(directedCamTarget);
            freeCam = null;
        }
    }

    public void setAspectRatio(float aspectRatio) {
        if (isFreeCam) {
            freeCam.setAspectRatio(aspectRatio);
        } else {
            directedCamera.setAspectRatio(aspectRatio);
        }
    }

    public void setDirectedCamTarget(Vector3f directedCamTarget) {
        this.directedCamTarget = directedCamTarget;
        if (!isFreeCam) {
            directedCamera.setTarget(directedCamTarget);
        }
    }

    public void handleMouseInput(float deltaTime, double mouseX, double mouseY) {
        if (firstMouse) {
            lastX = mouseX;
            lastY = mouseY;
            firstMouse = false;
        }

        double xOffset = mouseX - lastX;
        double yOffset = lastY - mouseY; // Reversed: y ranges bottom to top in glfw

        lastX = mouseX;
        lastY = mouseY;

        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        if (isFreeCam) {
            freeCam.addYaw((float) Math.toRadians(xOffset));
            freeCam.addPitch((float) Math.toRadians(yOffset));
        } else {
            directedCamera.addHorizontalAngle((float) Math.toRadians(-xOffset)); // Reversed for more intuitive control
            directedCamera.addVerticalAngle((float) Math.toRadians(yOffset));
        }
    }

    public void handleMouseWheel(float deltaTime, double yOffset) {
        if (!isFreeCam) {
            directedCamera.addDistanceFromTarget((float) yOffset * mouseScrollSensitivity * deltaTime * -1.0F); // Reversed for more intuitive control
        }
    }

    public void handleKeyboardInput(float deltaTime, boolean[] keys) {
        if (isFreeCam) {
            if (keys[GLFW_KEY_W]) {
                freeCam.moveForward(speed * deltaTime);
            }
            if (keys[GLFW_KEY_S]) {
                freeCam.moveBackwards(speed * deltaTime);
            }
            if (keys[GLFW_KEY_A]) {
                freeCam.moveLeft(speed * deltaTime);
            }
            if (keys[GLFW_KEY_D]) {
                freeCam.moveRight(speed * deltaTime);
            }
            if (keys[GLFW_KEY_LEFT_SHIFT]) {
                freeCam.moveDown(speed * deltaTime);
            }
            if (keys[GLFW_KEY_SPACE]) {
                freeCam.moveUp(speed * deltaTime);
            }
        } else {
            if (keys[GLFW_KEY_LEFT_SHIFT]) {
                directedCamera.addVerticalAngle(-speed * 0.15F * deltaTime);
            }
            if (keys[GLFW_KEY_SPACE]) {
                directedCamera.addVerticalAngle(speed * 0.15F * deltaTime);
            }
        }

        if (keys[GLFW_KEY_RIGHT_SHIFT]) {
            if (isFreeCam) {
                switchToDirectedCam();
            } else {
                switchToFreeCam();
            }
        }
    }

    public Matrix4f getCurrentProjectionMatrix() {
        if (isFreeCam) {
            return freeCam.getPerspectiveMatrix();
        } else {
            return directedCamera.getProjectionMatrix();
        }
    }

    public Matrix4f getCurrentViewMatrix() {
        if (isFreeCam) {
            return freeCam.getViewMatrix();
        } else {
            return directedCamera.getViewMatrix();
        }
    }
}
