package mia.miamod.render.util.elements;

import mia.miamod.render.util.ARGB;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VertexRect extends BufferDrawable {
    public final ARGB argb;

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

    protected ARGB colorWrapper(ARGB c) { return c; }

    public ARGB argb() { return colorWrapper(this.argb); }

    protected record Quad(Vector3f br, Vector3f tr, Vector3f tl, Vector3f bl) { }
    protected List<Quad> getBorderQuads(float borderSize) {
        return List.of(
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

    protected void drawBorder(DrawContext context, ARGB border) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.peek().getPositionMatrix().mul(getFinalRenderMatrix4f());
        internalDrawBorder(context, border.getARGB());
        matrices.pop();
    }

    private void internalDrawBorder(DrawContext context, int color) {
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
       drawRect(context, this.argb());
    }

    protected void drawRect(DrawContext context, ARGB argb) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.peek().getPositionMatrix().mul(getFinalRenderMatrix4f());
        context.fill((int)topLeft().x, (int)topLeft().y, (int)bottomRight().x, (int)bottomRight().y, (int) z, argb.getARGB());
        matrices.pop();
    }

    public boolean mouseDownEvent(int mouseX, int mouseY) {
        return false;
    }

    public boolean mouseUpEvent(int mouseX, int mouseY) {
        return false;
    }

    public boolean mouseDragEvent(int mouseX, int mouseY) {
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}
