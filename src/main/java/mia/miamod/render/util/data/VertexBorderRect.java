package mia.miamod.render.util.data;

import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.RenderContextHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class VertexBorderRect extends VertexRect {
    public float borderSize;
    public ARGB border;
    protected List<Quad> borderQuads;

    public VertexBorderRect(Matrix4f matrix4f, float x, float y, float width, float height, float borderSize, float z, ARGB argb, ARGB border) {
        super(matrix4f, x, y, width, height, z, argb);
        this.borderSize = borderSize;
        this.border = border;
        borderQuads = List.of(
                // right
                new Quad(
                        bottomRight(),
                        topRight(),
                        topRight().add(-borderSize, borderSize, 0),
                        bottomRight().sub(borderSize, borderSize, 0)
                ),
                // top
                new Quad(
                        topRight().add(-borderSize, borderSize, 0),
                        topRight(),
                        topLeft(),
                        topLeft().add(borderSize, borderSize, 0)
                ),
                // left
                new Quad(
                        bottomLeft().add(borderSize, -borderSize, 0),
                        topLeft().add(borderSize, borderSize, 0),
                        topLeft(),
                        bottomLeft()
                ),
                // bottom
                new Quad(
                        bottomRight(),
                        bottomRight().sub(borderSize, borderSize, 0),
                        bottomLeft().add(borderSize, -borderSize, 0),
                        bottomLeft()
                )
        );
    }



    /*
    @Override
    public void draw(Matrix4f renderMatrix4f, DrawContext context, int mouseY, int mouseX) {
        super.draw(renderMatrix4f, context, mouseY, mouseX);
        drawBorder(renderMatrix4f, this.borderQuads, this.border);
    }

    protected void drawBorder(Matrix4f renderMatrix4f, List<Quad> borderQuads, ARGB border) {
        VertexConsumer vertices = vertexConsumerProvider.getBuffer(RenderContextHelper.QUADS);

        int color = border.getARGB();
        borderQuads.forEach(quad -> quad.buildQuad(vertices, matrix4f, color));
    }

     */


}
