package mia.miamod.render.util.data;

import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.RenderContextHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class VertexRect extends BufferDrawable {
    public ARGB argb;
    protected float z;

    public VertexRect(MatrixStack matrixStack, float x, float y, float width, float height, float z, ARGB argb) {
        this.entry = matrixStack.peek();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.z = z;
        this.argb =  argb;
        this.drawables = new ArrayList<>();
    }

    public float getHeight() { return this.height; }
    public float getWidth() { return this.width; }
    public float getZ() { return this.z; }


    @Override
    protected void draw(VertexConsumerProvider.Immediate vertexConsumerProvider, DrawContext context, int mouseX, int mouseY) {
       drawRect(vertexConsumerProvider, context, this.argb);
    }

    protected void drawRect(VertexConsumerProvider vertexConsumerProvider, DrawContext context, ARGB argb) {
        context.fill((int)topLeft().x, (int)topLeft().y, (int)bottomRight().x, (int)bottomRight().y, (int) z, argb.getARGB());
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

    public boolean containsPoint(int x, int y, boolean inclusive) {
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

    public static boolean isPointInQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, int x, int y) {
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
    private static boolean isPointInTriangle(Vector3f v1, Vector3f v2, Vector3f v3, int x, int y) {
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
