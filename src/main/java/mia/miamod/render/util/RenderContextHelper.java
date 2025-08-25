package mia.miamod.render.util;

import mia.miamod.Mod;
import mia.miamod.render.util.data.VertexRect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES;

public class RenderContextHelper {
    public static final RenderLayer.MultiPhase LINES = makeLayer(DEBUG_LINES);
    public static final RenderLayer.MultiPhase QUADS = makeLayer(VertexFormat.DrawMode.QUADS);

    public static RenderLayer.MultiPhase makeLayer(VertexFormat.DrawMode mode) {
        String name = Mod.MOD_ID + "_" + mode.name().toLowerCase(Locale.ROOT);

        return
                RenderLayer.of(name, VertexFormats.POSITION_COLOR, mode, 1536, false, true,
                        RenderLayer.MultiPhaseParameters.builder()
                                .program(RenderPhase.POSITION_COLOR_PROGRAM)
                                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                                .cull(RenderPhase.ENABLE_CULLING)
                                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                                .writeMaskState(RenderPhase.COLOR_MASK)
                                .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                                .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                                .build(false)
                );
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int z, int color) {
        context.fill(x, y, x + width, y + 1, z, color);
        context.fill(x, y + height - 1, x + width, y + height, z, color);
        context.fill(x, y + 1, x + 1, y + height - 1, z, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, z, color);
    }

    public static Vector2f projectCameraRelativePoint(Vec3d cameraRelativePos) {
        // Return null if the matrices haven't been captured yet
        if (HudMatrixRegistry.modelViewMatrix == null || HudMatrixRegistry.projectionMatrix == null) {
            return null;
        }

        MinecraftClient client = Mod.MC;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // 1. Transform the camera-relative coordinates to a 4D vector
        Vector4f pos = new Vector4f((float) cameraRelativePos.x, (float) cameraRelativePos.y, (float) cameraRelativePos.z, 1.0f);

        // 2. Apply the cached Model-View and Projection matrices
        pos.mul(HudMatrixRegistry.modelViewMatrix);
        pos.mul(HudMatrixRegistry.projectionMatrix);

        // 3. Perform the perspective divide
        if (pos.w <= 0.0f) {
            return null; // The point is behind the camera's near clipping plane
        }
        pos.div(pos.w); // pos is now in Normalized Device Coordinates (NDC) [-1, 1]

        // 4. Map NDC to screen coordinates
        float screenX = screenWidth * (pos.x + 1.0f) / 2.0f;
        float screenY = screenHeight * (1.0f - pos.y) / 2.0f; // Y is inverted in screen space

        return new Vector2f(screenX, screenY);
    }


    public static Vec3d worldToScreen(Vec3d pos) {
        Camera camera = Mod.MC.getEntityRenderDispatcher().camera;
        int displayHeight = Mod.MC.getWindow().getHeight();
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(HudMatrixRegistry.positionMatrix);

        Matrix4f matrixProj = new Matrix4f(HudMatrixRegistry.projectionMatrix);
        Matrix4f matrixModel = new Matrix4f(HudMatrixRegistry.modelViewMatrix);

        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), HudMatrixRegistry.lastViewport, target);

        return new Vec3d(target.x / Mod.MC.getWindow().getScaleFactor(), (displayHeight - target.y) / Mod.MC.getWindow().getScaleFactor(), target.z);
    }


    public static List<Vec3d> getBoundingBoxCorners(PlayerEntity player) {
        List<Vec3d> corners = new ArrayList<>();
        Box box = player.getBoundingBox();

        corners.add(new Vec3d(box.minX, box.minY, box.minZ));
        corners.add(new Vec3d(box.maxX, box.minY, box.minZ));
        corners.add(new Vec3d(box.maxX, box.maxY, box.minZ));
        corners.add(new Vec3d(box.minX, box.maxY, box.minZ));
        corners.add(new Vec3d(box.minX, box.minY, box.maxZ));
        corners.add(new Vec3d(box.maxX, box.minY, box.maxZ));
        corners.add(new Vec3d(box.maxX, box.maxY, box.maxZ));
        corners.add(new Vec3d(box.minX, box.maxY, box.maxZ));

        return corners;
    }
}
