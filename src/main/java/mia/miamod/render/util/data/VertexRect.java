package mia.miamod.render.util.data;

import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.RenderContextHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VertexRect extends BufferDrawable {
    public ARGB argb;

    public VertexRect(Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb) {
        this.matrix4f = new Matrix4f(matrix4f);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.z = z;
        this.argb =  argb;
        this.drawables = new ArrayList<>();
    }

    protected record Quad(Vector3f br, Vector3f tr, Vector3f tl, Vector3f bl) {
        public void buildQuad(VertexConsumer vertices, Matrix4f matrix4f, int color) {
            vertices.vertex(matrix4f, br.x, br.y, br.z+1).color(color);
            vertices.vertex(matrix4f, tr.x, tr.y, tr.z+1).color(color);
            vertices.vertex(matrix4f, tl.x, tl.y, tl.z+1).color(color);
            vertices.vertex(matrix4f, bl.x, bl.y, bl.z+1).color(color);
        }
    }

    protected List<Quad> getBorderQuads(float borderSize) {
        return List.of(
                // right
                new VertexBorderRect.Quad(
                        bottomRight(),
                        topRight(),
                        topRight().add(-borderSize, borderSize, 0),
                        bottomRight().sub(borderSize, borderSize, 0)
                ),
                // top
                new VertexBorderRect.Quad(
                        topRight().add(-borderSize, borderSize, 0),
                        topRight(),
                        topLeft(),
                        topLeft().add(borderSize, borderSize, 0)
                ),
                // left
                new VertexBorderRect.Quad(
                        bottomLeft().add(borderSize, -borderSize, 0),
                        topLeft().add(borderSize, borderSize, 0),
                        topLeft(),
                        bottomLeft()
                ),
                // bottom
                new VertexBorderRect.Quad(
                        bottomRight(),
                        bottomRight().sub(borderSize, borderSize, 0),
                        bottomLeft().add(borderSize, -borderSize, 0),
                        bottomLeft()
                )
        );
    }

    protected void drawBorder(DrawContext context, ARGB border) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.peek().getPositionMatrix().mul(getFinalRenderMatrix4f());
        internalBorder(context, border.getARGB());
        //context.drawBorder((int)topLeft().x, (int)topLeft().y, (int)bottomRight().x, (int)bottomRight().y, argb.getARGB());
        matrices.pop();
        /*
        VertexConsumerProvider vertexConsumerProvider = context.vertexConsumers;
        VertexConsumer vertices = vertexConsumerProvider.getBuffer(RenderContextHelper.QUADS);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int color = border.getARGB();
        borderQuads.forEach(quad -> {
            vertexB(buffer, getFinalRenderMatrix4f(), new Vector3f(quad.tl).add(0,0,0)).color(color);
            vertexB(buffer, getFinalRenderMatrix4f(), new Vector3f(quad.bl).add(0,0,0)).color(color);
            vertexB(buffer, getFinalRenderMatrix4f(), new Vector3f(quad.br).add(0,0,0)).color(color);
            vertexB(buffer, getFinalRenderMatrix4f(), new Vector3f(quad.tr).add(0,0,0)).color(color);
        });


        BufferRenderer.drawWithGlobalProgram(buffer.end());

        //borderQuads.forEach(quad -> quad.buildQuad(vertices, getFinalRenderMatrix4f(), border.getARGB()));

         */
    }

    private void internalBorder(DrawContext context, int color) {
        int x = (int)topLeft().x;
        int y = (int)topLeft().y;
        int width = (int) getWidth();
        int height = (int) getWidth();
        int z = (int) this.z;

        context.fill(x, y, x + width, y + 1, z, color);
        context.fill(x, y + height - 1, x + width, y + height, z, color);
        context.fill(x, y + 1, x + 1, y + height - 1, z, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, z, color);
    }


    @Override
    protected void draw(DrawContext context, int mouseX, int mouseY) {
       drawRect(context, this.argb);
    }

    protected void drawRect(DrawContext context, ARGB argb) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.peek().getPositionMatrix().mul(getFinalRenderMatrix4f());
        context.fill((int)topLeft().x, (int)topLeft().y, (int)bottomRight().x, (int)bottomRight().y, (int) z, argb.getARGB());
        matrices.pop();
        /*
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int color = argb.getARGB();

        VertexConsumer vertices = vertexConsumerProvider.getBuffer(RenderContextHelper.QUADS);
        Matrix4f matrix4f = new Matrix4f(entry.getPositionMatrix()).translate(0,0, z);
        vertexB(buffer, matrix4f, bottomRight()).color(color);
        vertexB(buffer, matrix4f, topRight()).color(color);
        vertexB(buffer,matrix4f, topLeft()).color(color);
        vertexB(buffer, matrix4f, bottomLeft()).color(color);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

         */
    }

    private VertexConsumer vertexB(BufferBuilder vertexConsumer, Matrix4f matrix4f, Vector3f vector3f) {
        return vertexConsumer.vertex(matrix4f, vector3f.x, vector3f.y, vector3f.z);
    }

    private VertexConsumer vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Vector3f vector3f) {
        return vertexConsumer.vertex(matrix4f, vector3f.x, vector3f.y, vector3f.z);
    }

    public boolean containsPoint(float x, float y, boolean inclusive) {
        if (matrix4f == null || renderMatrix4f == null) return false;
        Vector3f normal = new Vector3f(0, 0, -1);
        Vector3f P = new Vector3f(x, y, z);

        Vector3f A = getVertexScreenPosition(new Vector3f(topLeft()));
        Vector3f B = getVertexScreenPosition(new Vector3f(bottomLeft()));
        Vector3f C = getVertexScreenPosition(new Vector3f(bottomRight()));
        Vector3f D = getVertexScreenPosition(new Vector3f(topRight()));
        return isPointInQuad(C, D, A, B, x, y);
/*
        float ab = B.sub(A).cross(P.sub(A)).normalize().dot(normal);
        float bc = C.sub(B).cross(P.sub(B)).normalize().dot(normal);
        float cd = D.sub(C).cross(P.sub(C)).normalize().dot(normal);
        float da = A.sub(D).cross(P.sub(D)).normalize().dot(normal);

        return (ab * bc * cd * da == 0 && inclusive) || ((ab * bc > 0) && (bc * cd > 0) && (cd * da > 0) && (da * ab > 0));

 */
    }

    public static boolean isPointInQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, float x, float y) {
        // Check if the point is in the first triangle (v1, v2, v4)
        boolean inTriangle1 = isPointInTriangle(v1, v2, v4, x, y);

        // Check if the point is in the second triangle (v2, v3, v4)
        boolean inTriangle2 = isPointInTriangle(v2, v3, v4, x, y);

        // The point is in the quad if it's in either of the two triangles.
        return inTriangle1 || inTriangle2;
    }

    private static float sign(float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y);
    }

    /**
     * Checks if a point (x, y) is inside a triangle defined by three vertices.
     * This method uses the barycentric coordinate concept, checking if the point
     * lies on the same side of all three triangle edges.
     *
     * @param v1 The first vertex of the triangle.
     * @param v2 The second vertex of the triangle.
     * @param v3 The third vertex of the triangle.
     * @param x  The x-coordinate of the point to check.
     * @param y  The y-coordinate of the point to check.
     * @return true if the point is inside the triangle, false otherwise.
     */
    private static boolean isPointInTriangle(Vector3f v1, Vector3f v2, Vector3f v3, float x, float y) {
        // Calculate the signs for the point with respect to each edge of the triangle.
        float d1 = sign(x, y, v1.x, v1.y, v2.x, v2.y);
        float d2 = sign(x, y, v2.x, v2.y, v3.x, v3.y);
        float d3 = sign(x, y, v3.x, v3.y, v1.x, v1.y);

        // Check if the signs are all non-negative or all non-positive.
        // This indicates that the point is on the same side of all edges.
        boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        // If the point is inside, all signs will be the same (or zero if on an edge).
        // So, we won't have a mix of positive and negative signs.
        return !(has_neg && has_pos);
    }

}
