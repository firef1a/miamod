package mia.miamod.render.util.data;

import mia.miamod.features.Category;
import mia.miamod.render.util.ARGB;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class VertexButton extends VertexRect {
    private boolean enabled;
    private ARGB enabledARGB, highlighEnabledARGB;
    protected ARGB highlightARGB;
    protected Runnable callback;

    public VertexButton(Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, ARGB enabledARGB, ARGB highlighEnabledARGB, Runnable callback) {
        super(matrix4f, x, y, width, height, z, argb);
        this.highlightARGB = highlightARGB;
        this.enabledARGB = enabledARGB;
        this.highlighEnabledARGB = highlighEnabledARGB;
        this.callback = callback;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean getEnabled() { return this.enabled; }

    public void setCallback(Runnable callback) { this.callback = callback; }

    public void onClick(int mouseX, int mouseY) { if (containsPoint(mouseX, mouseY, true)) callback.run(); }

    @Override
    protected void draw(DrawContext context, int mouseX, int mouseY) {
        boolean contains = containsPoint(mouseX, mouseY, true);
        drawRect(context, contains ? (getEnabled() ? this.highlighEnabledARGB : this.highlightARGB) : (getEnabled() ? this.enabledARGB : this.argb));
    }

}
