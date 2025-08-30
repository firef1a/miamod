package mia.miamod.render.util.data;

import mia.miamod.render.util.ARGB;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class VertexButton extends VertexRect {
    private boolean enabled;
    public final ARGB enabledARGB;
    public final ARGB highlighEnabledARGB;
    public final ARGB highlightARGB;
    protected Runnable callback;

    public VertexButton(Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, ARGB enabledARGB, ARGB highlighEnabledARGB, Runnable callback) {
        super(matrix4f, x, y, width, height, z, argb);
        this.highlightARGB = highlightARGB;
        this.enabledARGB = enabledARGB;
        this.highlighEnabledARGB = highlighEnabledARGB;
        this.callback = callback;
    }

    public ARGB enabledARGB() { return colorWrapper(this.enabledARGB); }
    public ARGB highlightARGB() { return colorWrapper(this.highlightARGB); }
    public ARGB highlighEnabledARGB() { return colorWrapper(this.highlighEnabledARGB); }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean getEnabled() { return this.enabled; }

    public void setCallback(Runnable callback) { this.callback = callback; }

    @Override
    public boolean mouseDownEvent(int mouseX, int mouseY) {
        if (containsPoint(mouseX, mouseY, true)) {
            return onMouseDown(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean mouseUpEvent(int mouseX, int mouseY) {
        if (containsPoint(mouseX, mouseY, true)) {
            return onMouseUp(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean mouseDragEvent(int mouseX, int mouseY) {
        if (containsPoint(mouseX, mouseY, true)) {
            return onMouseDrag(mouseX, mouseY);
        }
        return false;
    }

    protected boolean onMouseDown(int mouseX, int mouseY) {
        callback.run();
        return true;
    }

    protected boolean onMouseUp(int mouseX, int mouseY) {
        return true;
    }

    protected boolean onMouseDrag(int mouseX, int mouseY) {
        return true;
    }

    @Override
    protected void draw(DrawContext context, int mouseX, int mouseY) {
        boolean contains = containsPoint(mouseX, mouseY, true);
        drawRect(context, contains ? (getEnabled() ? this.highlighEnabledARGB() : this.highlightARGB()) : (getEnabled() ? this.enabledARGB() : this.argb()));
    }

}
