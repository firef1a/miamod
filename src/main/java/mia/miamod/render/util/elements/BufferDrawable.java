package mia.miamod.render.util.elements;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public abstract class BufferDrawable {
    protected Matrix4f matrix4f, renderMatrix4f;
    protected float x, y, z, width, height;
    protected BufferDrawable parent;
    protected ArrayList<BufferDrawable> drawables;

    protected DrawableBinding parentBinding;
    protected DrawableBinding selfBinding;

    public DrawableBinding getParentBinding() { return parentBinding == null ? new DrawableBinding(AxisBinding.NONE, AxisBinding.NONE) : parentBinding; }
    public DrawableBinding getSelfBinding() { return selfBinding == null ? new DrawableBinding(AxisBinding.NONE, AxisBinding.NONE) : selfBinding; }
    public void setParentBinding(DrawableBinding binding) { this.parentBinding = binding; }
    public void setSelfBinding(DrawableBinding binding) { this.selfBinding = binding; }

    protected abstract void draw(DrawContext context, int mouseX, int mouseY);

    public void contextDraw(Matrix4f renderMatrix4f, DrawContext context, int mouseX, int mouseY) {
        //enableScissorsVertexRect(context);
        this.renderMatrix4f = new Matrix4f(renderMatrix4f);
        draw(context, mouseX, mouseY);
        this.drawables.forEach(object -> object.contextDraw(renderMatrix4f, context, mouseX, mouseY));
        //context.disableScissor();
    };

    public void setParent(BufferDrawable parent) { this.parent = parent; };
    public void addDrawable(BufferDrawable child) { drawables.add(child); child.setParent(this); };
    public void clearDrawables() { drawables.clear(); };

    public float rawX1() { return x; }
    public float rawY1() { return y; }
    public float rawX2() { return x+this.width; }
    public float rawY2() { return y+this.height; }

    public Vector3f topLeft() {
        Vector3f tl = new Vector3f(x,y,0);
        return (parent != null) ?
                tl      .add(parent.topLeft().add(getParentBinding().getXBinding().getScale() * parent.getWidth(), getParentBinding().getYBinding().getScale() * parent.getHeight(), 0))
                        .add(-getSelfBinding().getXBinding().getScale() * getWidth(),-getSelfBinding().getYBinding().getScale() * getHeight(),0)
                : tl.add(0,0,z)
                .add(-getSelfBinding().getXBinding().getScale() * getWidth(),-getSelfBinding().getYBinding().getScale() * getHeight(),0);
    }

    public Vector3f topRight() { return topLeft().add(width,0,0); }
    public Vector3f bottomLeft() { return topLeft().add(0,height,0); }
    public Vector3f bottomRight() { return topLeft().add(width,height,0); }

    public Vector3f getVertexScreenPosition(Vector3f vertex) {
        Vector4f vertex4f = new Vector4f(vertex, 1f);
        getFinalRenderMatrix4f().transform(vertex4f);
        return new Vector3f(vertex4f.x, vertex4f.y, vertex4f.z);
    }

    public float getHeight() { return this.height; }
    public float getWidth() { return this.width; }
    public float getZ() { return this.z; }

    public void enableScissorsVertexRect(DrawContext context) {
        context.enableScissor(
                (int) getVertexScreenPosition(this.topLeft()).x,
                (int) getVertexScreenPosition(this.topLeft()).y,
                (int) getVertexScreenPosition(this.bottomRight()).x,
                (int) getVertexScreenPosition(this.bottomRight()).y
        );
    }

    public Matrix4f getFinalRenderMatrix4f() {
        if (matrix4f == null || renderMatrix4f == null) {
            return new Matrix4f().identity();
        }
        return new Matrix4f(matrix4f).mul(new Matrix4f(renderMatrix4f));
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
