package mia.miamod.render.util.data;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public abstract class BufferDrawable {
    protected MatrixStack.Entry entry;
    protected float x, y, z, width, height;
    protected BufferDrawable parent;
    protected ArrayList<BufferDrawable> drawables;

    protected DrawableBinding parentBinding = new DrawableBinding(AxisBinding.NONE, AxisBinding.NONE);
    protected DrawableBinding selfBinding = new DrawableBinding(AxisBinding.NONE, AxisBinding.NONE);

    public DrawableBinding getParentBinding() { return parentBinding; }
    public DrawableBinding getSelfBinding() { return selfBinding; }
    public void setParentBinding(DrawableBinding binding) { this.parentBinding = binding; }
    public void setSelfBinding(DrawableBinding binding) { this.selfBinding = binding; }

    protected abstract void draw(VertexConsumerProvider.Immediate vertexConsumerProvider, DrawContext context, int mouseX, int mouseY);

    public void contextDraw(VertexConsumerProvider.Immediate vertexConsumerProvider, DrawContext context, int mouseX, int mouseY) {
        //enableScissorsVertexRect(context);
        draw(vertexConsumerProvider, context, mouseX, mouseY);
        this.drawables.forEach(object -> object.contextDraw(vertexConsumerProvider, context, mouseX, mouseY));
        //context.disableScissor();
    };

    public void setParent(BufferDrawable parent) { this.parent = parent; };
    public void addDrawable(BufferDrawable child) { drawables.add(child); child.setParent(this); };

    public Vector3f topLeft() {
        Vector3f tl = new Vector3f(x,y,0);
        return (parent != null) ? tl.add(parent.topLeft()) : tl.add(0,0,z);
    }

    public Vector3f topRight() { return topLeft().add(width,0,0); }
    public Vector3f bottomLeft() { return topLeft().add(0,height,0); }
    public Vector3f bottomRight() { return topLeft().add(width,height,0); }

    public Vector3f getVertexScreenPosition(Vector3f vertex) {
        Matrix4f matrix4f = new Matrix4f(entry.getPositionMatrix());
        Vector4f vertex4f = new Vector4f(vertex, 1f);
        matrix4f.transform(vertex4f);

        return new Vector3f(vertex4f.x, vertex4f.y, vertex4f.z);
    }

    public void enableScissorsVertexRect(DrawContext context) {
        context.enableScissor(
                (int) getVertexScreenPosition(this.topLeft()).x,
                (int) getVertexScreenPosition(this.topLeft()).y,
                (int) getVertexScreenPosition(this.bottomRight()).x,
                (int) getVertexScreenPosition(this.bottomRight()).y
        );
    }

}
