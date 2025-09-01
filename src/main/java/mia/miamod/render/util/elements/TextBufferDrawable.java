package mia.miamod.render.util.elements;

import mia.miamod.Mod;
import mia.miamod.render.util.ARGB;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class TextBufferDrawable extends BufferDrawable {
    Text text;
    protected ARGB argb;
    boolean shadow;
    TextRenderer.TextLayerType textLayerType;
    int backgroundColor;
    int light;

    public TextBufferDrawable(Matrix4f matrix4f, Text text, float x, float y, float z, ARGB argb, boolean shadow) {
        this.matrix4f = matrix4f;
        this.text = text;
        this.x = x;
        this.y = y;
        this.z = z;
        this.argb = argb;
        this.textLayerType = TextRenderer.TextLayerType.SEE_THROUGH;
        this.shadow = shadow;
        this.backgroundColor = 0;
        this.light = 15728880;

        this.width = getWidth();
        this.height = Mod.MC.textRenderer.fontHeight - 2;
        this.drawables = new ArrayList<>();
    }
    //new Matrix4f(entry.getPositionMatrix()).translate(topLeft()),

    @Override
    protected void draw(DrawContext context, int mouseX, int mouseY) {
        drawText(context, topLeft().x,topLeft().y);
    }

    protected void drawText(DrawContext context, float x, float y) {
        Mod.MC.textRenderer.draw(
                getText(),
                x, y,
                argb.getARGB(),
                shadow,
                getFinalRenderMatrix4f(),
                context.vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                15728880
        );
    }

    @Override
    public float getWidth() {
        return Mod.MC.textRenderer.getWidth(getText());
    }

    protected Text getText() {
        return text;
    }
}
