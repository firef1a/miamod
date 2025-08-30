package mia.miamod.render.util.data.impl;

import mia.miamod.Mod;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.data.TextBufferDrawable;
import mia.miamod.render.util.data.VertexButton;
import mia.miamod.render.util.data.VertexRect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;

public class StringInputField extends TextBufferDrawable {
    protected static final int maxChars = 65;
    protected final ParameterDataField<?> stringDataField;
    protected int cursor;
    protected boolean isFocused;
    protected boolean ctrlA;
    protected Runnable runnable = () -> {};

    public StringInputField(Matrix4f matrix4f, ParameterDataField<?> stringDataField, float x, float y, float z, ARGB argb, boolean shadow) {
        super(matrix4f, Text.empty(), x, y, z, argb, shadow);
        this.stringDataField = stringDataField;
        this.isFocused = false;
        this.cursor = getString().length();
        this.ctrlA = false;
    }

    protected void setString(String value) {
        if (stringDataField != null) ((StringDataField) stringDataField).setValue(value);
    }

    @Contract(pure = true)
    private String getString() {
        return stringDataField == null ? "" : String.valueOf(stringDataField.getValue());
    }

    @Contract(pure = true)
    protected Text getText() {
        return stringDataField == null ? Text.empty() : Text.literal(String.valueOf(stringDataField.getValue())).withColor(argb.getARGB());
    }

    @Override
    public float getWidth() {
        return Mod.MC.textRenderer.getWidth(getText());
    }

    public void setFocused(boolean focused) {
        this.isFocused = focused;
    }

    public boolean getFocused() {
        return this.isFocused;
    }

    public void setCallback(Runnable runnable) {
        this.runnable = runnable;
    }


    public boolean mouseDownEvent(int mouseX, int mouseY) {
        if (stringDataField == null) return false;
        isFocused = parent != null && parent.containsPoint(mouseX, mouseY, true);
        ctrlA = false;
        if (isFocused) {
            runnable.run();
            int x = (int) (mouseX - getVertexScreenPosition(new Vector3f(topLeft())).x);
            cursor = getString().length();
            if (x <= 0) cursor = 0;
            else if (x >= getWidth()) cursor = getString().length();
            else {
                for (int i = 0; i < getString().length(); i++) {
                    String base = getString().substring(i);
                    int baseX = Mod.MC.textRenderer.getWidth(base);
                    int charSize = Mod.MC.textRenderer.getWidth(getString().substring(i, i + 1));
                    int midX = baseX + (charSize / 2);
                    if (x >= baseX && x < midX) {
                        cursor = i;
                        break;
                    }
                    if (x >= midX && x <= baseX + charSize) {
                        cursor = i + 1;
                        break;
                    }

                }
                // idk why but the above gives the inverse of the correct position
                cursor = getString().length() - cursor;
            }




        }
        return isFocused;
    }


    public boolean mouseUpEvent(int mouseX, int mouseY) {
        if (stringDataField == null) return false;
        return containsPoint(mouseX, mouseY, true);
    }


    protected boolean characterFilter(char chr) {
        return true;
    }
    public boolean charTyped(char chr, int modifiers) {
        if (stringDataField == null) return false;
        if (isFocused && getString().length() < maxChars && characterFilter(chr)) {
            try {
                if (ctrlA) {
                    setString("");
                    cursor = 0;
                }
                ctrlA = false;
                setString(getString().substring(0, cursor) + chr + getString().substring(cursor));
                cursor = Math.clamp(cursor+1, 0, getString().length());
            } catch (Exception e) {

            }
        }
        return isFocused;
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (stringDataField == null) return false;
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            isFocused = false;
            return true;
        }
        if (!getString().isEmpty() && isFocused) {
            if (modifiers == GLFW_MOD_CONTROL && keyCode == GLFW.GLFW_KEY_A) {
                ctrlA = true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (cursor > 0) {
                    setString(getString().substring(0, cursor - 1) + getString().substring(cursor));
                    cursor = Math.clamp(cursor-1, 0, getString().length());
                }

                if (ctrlA) {
                    setString("");
                    cursor = 0;
                }
                ctrlA = false;
                return isFocused;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT && cursor > 0) {
                cursor -= 1;

                ctrlA = false;
                return isFocused;
            }
            if (cursor < getString().length()) {
                if (keyCode == GLFW.GLFW_KEY_DELETE) {
                    setString(getString().substring(0, cursor) + getString().substring(cursor+1));

                    if (ctrlA) {
                        setString("");
                        cursor = 0;
                    }
                    ctrlA = false;
                    return isFocused;
                }
                if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                    cursor = Math.clamp(cursor+1, 0, getString().length());

                    ctrlA = false;
                    return isFocused;
                }
            }
        }
        return isFocused;
    }

    @Override
    protected void draw(DrawContext context, int mouseX, int mouseY) {
        if (stringDataField == null) return;
        drawText(context, topLeft().x ,topLeft().y);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.peek().getPositionMatrix().mul(getFinalRenderMatrix4f());
        int x = (int) (topLeft().x + Mod.MC.textRenderer.getWidth(getString().substring(0, cursor)));
        int y = (int) topLeft().y;
        if (isFocused) {
            if ((Mod.tick / 10 % 2) == 0) {
                context.fill(x, y - 1, x + 1, y + Mod.MC.textRenderer.fontHeight + 1, (int) z + 1, argb.getARGB());
            }
            context.fill((int) bottomLeft().x, (int) bottomLeft().y + 1, (int) (bottomLeft().x + getWidth()), (int) bottomLeft().y + 2, (int) z + 3, ctrlA ? new ARGB(0x94bdff, 1f).getARGB() : argb.getARGB());
        }
        matrices.pop();
        //topLeft().x,topLeft().y,
    }
}

