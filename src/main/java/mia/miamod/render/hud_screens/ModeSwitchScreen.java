package mia.miamod.render.hud_screens;

import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.impl.development.ModeSwitcher;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import mia.miamod.render.screens.AnimationStage;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.EasingFunctions;
import mia.miamod.render.util.RenderHelper;
import mia.miamod.render.util.elements.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

public class ModeSwitchScreen extends InGameHudScreen {
    private ArrayList<ModeButton> modeButtons;

    private AnimationStage animationStage;
    private static final float animationSpeed = 0.135f;
    private float animation;

    public ModeSwitchScreen() {
        super();
        animationStage = AnimationStage.OPENING;
    }

    private static final class ModeButton extends VertexButton {
        public final String modeName;
        public ModeButton(String modeName, Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, Runnable callback) {
            super(matrix4f, x, y, width, height, z, argb, highlightARGB, argb, highlightARGB, callback);
            this.modeName = modeName;
        }

        @Override
        protected void draw(DrawContext context, int mouseX, int mouseY) {
            boolean contains = containsPoint(mouseX, mouseY, true);
            drawRect(context, new ARGB(0x000000, 0.15f));
            drawBorder(context, contains ? (getEnabled() ? this.highlighEnabledARGB() : this.highlightARGB()) : (getEnabled() ? this.enabledARGB() : this.argb()));
        }
    }

    @Override
    protected void init() {
        String[] modes = { "dev", "play", "build" };

        modeButtons = new ArrayList<>();

        int i = 0;
        for (String mode : modes) {
            double theta = (((i+1) / (modes.length * 1f)) * (2f * Math.PI)) + (Math.PI / 6);
            ModeButton button = new ModeButton(
                    mode,
                    new Matrix4f().identity(),
                    (float) (Math.cos(theta)) * 26f,
                    (float) (Math.sin(theta)) * 26f,
                    32,
                    32,
                    100,
                    new ARGB(0x8ae3ff, 1f),
                    new ARGB(0xebfaff, 1f),
                    this::close
            );
            TextBufferDrawable modeText = new TextBufferDrawable(
                    new Matrix4f().identity(),
                    Text.literal(mode.substring(0,1).toUpperCase() + mode.substring(1)),
                    0,0,
                    101,
                    new ARGB(0xFFFFFF, 1f),
                    true
            );
            modeText.setParentBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
            modeText.setSelfBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

            button.addDrawable(modeText);
            button.setSelfBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
            modeButtons.add(button);
            i++;
        }
    }

    @Override
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        ci.cancel();
        if (modeButtons != null) {
            for (VertexButton modeButton : modeButtons) {
                modeButton.mouseDownEvent((int) getMouseX(), (int) getMouseY());
            }
        }
    }

    @Override
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MatrixStack matrices = context.getMatrices();
        RenderHelper renderHelper = new RenderHelper(Tessellator.getInstance(), (int) getMouseX(), (int) getMouseY(), tickCounter.getTickDelta(true));

        matrices.push();
        matrices.translate(context.getScaledWindowWidth() / 2F, context.getScaledWindowHeight() / 2F, 0);
        matrices.push();
        matrices.scale(EasingFunctions.easeInOutCubic(animation), EasingFunctions.easeInOutCubic(animation), 1f);
        matrices.push();

        Matrix4f renderMatrix4f = new Matrix4f(matrices.peek().getPositionMatrix());

        matrices.pop();
        matrices.pop();
        matrices.pop();

        if (LocationAPI.getMode().isOnPlot()) {
            if (modeButtons != null) {
                for (ModeButton modeButton : modeButtons) {
                    renderHelper.contextDraw(renderMatrix4f, context, modeButton);
                }
            }
        } else {
            TextBufferDrawable errorText = new TextBufferDrawable(
                    new Matrix4f().identity(),
                    Text.literal("Must be on a plot to use this.").withColor(0xff6052),
                    0,0,
                    100,
                    new ARGB(ColorBank.MC_RED, 1f),
                    true
            );
            errorText.setSelfBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
            renderHelper.contextDraw(renderMatrix4f, context, errorText);
        }

        animation = Math.clamp(animation + (animationSpeed * animationStage.direction()), 0, 1f);
        if (animationStage.equals(AnimationStage.OPENING) && animation >= 1f) {
            animationStage = AnimationStage.OPEN;
        }
        if (animationStage.equals(AnimationStage.CLOSING) && animation <= 0) {
            ModeSwitcher.modeSwitchScreen = null;
            super.close();
        }

    }

    @Override
    public void close() {
        if (animationStage.direction() == 1) {
            if (modeButtons != null && LocationAPI.getMode().isOnPlot()) {
                for (ModeButton modeButton : modeButtons) {
                    if (modeButton.containsPoint((int) getMouseX(), (int) getMouseY(), true)) {
                        Mod.sendCommand("/" + modeButton.modeName);
                        break;
                    }
                }
            }
            animationStage = AnimationStage.CLOSING;
        }
    }
}
