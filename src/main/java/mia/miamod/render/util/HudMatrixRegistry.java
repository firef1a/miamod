package mia.miamod.render.util;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public abstract class HudMatrixRegistry {
    public static Matrix4f modelViewMatrix;
    public static Matrix4f projectionMatrix;
    public static Matrix4f positionMatrix;
    public static final int[] lastViewport = new int[4];

    public static void register() {
        WorldRenderEvents.END.register(context -> {
            // It's crucial to copy the matrices here as they are managed by the rendering engine
            // and can be modified or popped from the stack after this event.
            modelViewMatrix = new Matrix4f(context.matrixStack().peek().getPositionMatrix());
            projectionMatrix = new Matrix4f(context.projectionMatrix());
            positionMatrix = new Matrix4f(context.positionMatrix());
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, lastViewport);
        });
    }

    public static Vec3d project(Vec3d pos) {
        if (modelViewMatrix == null || projectionMatrix == null) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return null;
        }

        // The camera's position is needed to translate the world position relative to the camera
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        Vec3d relativePos = new Vec3d(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);

        Vector4f screenCoords = new Vector4f((float)relativePos.x, (float)relativePos.y, (float)relativePos.z, 1.0f);

        // Apply the model-view and projection transformations
        screenCoords.mul(modelViewMatrix);
        screenCoords.mul(projectionMatrix);

        // Perform the perspective divide
        if (screenCoords.w == 0.0f) {
            return null;
        }
        screenCoords.x /= screenCoords.w;
        screenCoords.y /= screenCoords.w;
        screenCoords.z /= screenCoords.w;

        // Map to screen coordinates
        double x = (screenCoords.x * 0.5 + 0.5) * client.getWindow().getScaledWidth();
        client.getWindow().getScaleFactor();
        double y = (-screenCoords.y * 0.5 + 0.5) * client.getWindow().getScaledHeight();
        double z = screenCoords.w; // The 'w' component can be used to check if the point is in front of or behind the camera

        if (z > 0) {
            return new Vec3d(x, y, z);
        } else {
            return null; // Don't render if it's behind the camera
        }
    }
}