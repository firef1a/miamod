package mia.miamod.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import mia.miamod.render.util.data.BufferDrawable;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RenderHelper {
    private final Tessellator tessellator;
    private ArrayList<BufferDrawable> bufferDrawList;
    private VertexConsumerProvider.Immediate vertexConsumerProvider;
    private int mouseX, mouseY;
    private float delta;

    public RenderHelper(Tessellator tessellator, VertexConsumerProvider.Immediate vertexConsumerProvider, int mouseX, int mouseY, float delta) {
        this.tessellator = tessellator;
        this.vertexConsumerProvider = vertexConsumerProvider;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
        bufferDrawList = new ArrayList<>();
    }

    //public void addDrawable(BufferDrawable object) { bufferDrawList.add(object); }
    public void contextDraw(DrawContext context, BufferDrawable bufferDrawable) { bufferDrawable.contextDraw(vertexConsumerProvider, context, mouseX, mouseY); }

    public static void setupStencil() {
        RenderSystem.stencilMask(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        clearStencil();
    }

    public static void drawPolygonMask(DrawContext drawContext, List<Vector3f> polygonVertices) {
        // Disable writing to the color and depth buffers. We only want to affect the stencil buffer.
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);

        // Get the necessary components for drawing custom geometry.
        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f positionMatrix = drawContext.getMatrices().peek().getPositionMatrix();

        // Use the default position shader for simple 2D shapes.
        RenderSystem.setShader(ShaderProgramKeys.POSITION);

        // Begin drawing. TRIANGLE_FAN is a good choice for simple convex polygons.
        // For concave or complex polygons, you might need a more advanced triangulation algorithm.
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
        for (Vector3f vertex : polygonVertices) {
            bufferBuilder.vertex(positionMatrix, vertex.x, vertex.y, 0);
        }

        // Finalize and draw the vertices.
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        // Re-enable writing to the color and depth buffers for subsequent rendering.
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
    }

    public static void configureStencilForClipping() {
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    public static void clearStencil() {
        RenderSystem.clearStencil(GL11.GL_STENCIL_BUFFER_BIT);
    }


    /*
    public void drawBuffer() {
        for (BufferDrawable bufferDrawable : bufferDrawList) {
            bufferDrawable.draw(tessellator, vertexConsumerProvider);
        }
    }

     */
}
