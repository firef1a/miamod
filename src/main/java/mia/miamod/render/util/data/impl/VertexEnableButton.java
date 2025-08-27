package mia.miamod.render.util.data.impl;

import mia.miamod.ColorBank;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.data.VertexButton;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class VertexEnableButton extends VertexButton {
    public static final ARGB disabledARGB = new ARGB(0xff4230,1f);
    public static final ARGB disabledHighlightARGB = new ARGB(0xff9187,1f);
    public static final ARGB enabledARGB = new ARGB(0x66ff40,1f);
    public static final ARGB enabledHighlightARGB = new ARGB(0xafff9c,1f);
    public VertexEnableButton(Matrix4f matrix4f, float x, float y, float width, float height, float z, Runnable callback) {
        super(
                matrix4f,
                x, y,
                width, height,
                z,
                disabledARGB,
                disabledHighlightARGB,
                enabledARGB,
                enabledHighlightARGB,
                callback
        );
    }

    @Override
    protected void draw(DrawContext context, int mouseX, int mouseY) {
        super.draw(context, mouseX, mouseY);
        drawBorder(context, new ARGB(ColorBank.BLACK, 1F));
    }

    @Override
    public boolean containsPoint(float x, float y, boolean inclusive) {
        return parent == null ? super.containsPoint(x, y, inclusive) : parent.containsPoint(x, y, inclusive);
    }
}
