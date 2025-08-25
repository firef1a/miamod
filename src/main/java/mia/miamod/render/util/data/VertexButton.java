package mia.miamod.render.util.data;

import mia.miamod.render.util.ARGB;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

public class VertexButton extends VertexRect {
    protected ARGB highlightARGB;
    protected Runnable callback;


    public VertexButton(MatrixStack matrixStack, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, Runnable callback) {
        super(matrixStack, x, y, width, height, z, argb);

        this.highlightARGB = highlightARGB;
        this.callback = callback;
    }

    public void onClick(int mouseX, int mouseY) { if (containsPoint(mouseX, mouseY, true)) callback.run(); }

    @Override
    protected void draw(VertexConsumerProvider.Immediate vertexConsumerProvider, DrawContext context, int mouseX, int mouseY) {
        boolean contains = containsPoint(mouseX, mouseY, true);
        drawRect(vertexConsumerProvider, context, contains ? this.highlightARGB : this.argb);
        //drawRect(vertexConsumerProvider, this.bottomRight, this.topRight, this.topLeft, this.bottomLeft,this.argb);//,);
    }

}
