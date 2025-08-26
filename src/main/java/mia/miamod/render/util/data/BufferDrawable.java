package mia.miamod.render.util.data;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
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
                : tl.add(0,0,z);
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
        return new Matrix4f(matrix4f).mul(new Matrix4f(renderMatrix4f));
    }
}
