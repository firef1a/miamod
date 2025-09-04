package mia.miamod.render.hud_screens;

import mia.miamod.Mod;
import mia.miamod.features.impl.general.NodeSwitcher;
import mia.miamod.render.screens.AnimationStage;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.EasingFunctions;
import mia.miamod.render.util.RenderHelper;
import mia.miamod.render.util.elements.AxisBinding;
import mia.miamod.render.util.elements.DrawableBinding;
import mia.miamod.render.util.elements.TextBufferDrawable;
import mia.miamod.render.util.elements.VertexButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NodeSwitchScreen extends InGameHudScreen {
    private ArrayList<ModeButton> buttons;

    private AnimationStage animationStage;
    private static final float animationSpeed = 0.135f;
    private float animation;

    public NodeSwitchScreen() {
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
        buttons = new ArrayList<>();

        ArrayList<String> mainNodes = NodeSwitcher.getServerIds().stream().filter(string -> Pattern.matches("^node\\d*$", string)).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(mainNodes);

        ArrayList<String> privateNodes = NodeSwitcher.getServerIds().stream().filter(string -> Pattern.matches("^private\\d*$", string)).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(privateNodes);

        ArrayList<String> miscNodes = NodeSwitcher.getServerIds().stream().filter(string -> !(privateNodes.contains(string) || mainNodes.contains(string))).collect(Collectors.toCollection(ArrayList::new));

        int i = 0;
        for (String node : NodeSwitcher.getServerIds()) {
            double theta = (((i+1) / ( NodeSwitcher.getServerIds().size() * 1f)) * (2f * Math.PI)) + (Math.PI / 6);
            ModeButton button = new ModeButton(
                    node,
                    new Matrix4f().identity(),
                    (float) (Math.cos(theta)) * 200f,
                    (float) (Math.sin(theta)) * 200f,
                    60,
                    15,
                    100,
                    new ARGB(0x8ae3ff, 1f),
                    new ARGB(0xebfaff, 1f),
                    this::close
            );
            TextBufferDrawable nodeText = new TextBufferDrawable(
                    new Matrix4f().identity(),
                    Text.literal(node.substring(0,1).toUpperCase() + node.substring(1)),
                    0,0,
                    101,
                    new ARGB(0xFFFFFF, 1f),
                    true
            );
            nodeText.setParentBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
            nodeText.setSelfBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

            button.addDrawable(nodeText);
            button.setSelfBinding(new DrawableBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
            buttons.add(button);
            i++;
        }
    }

    @Override
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        ci.cancel();
        if (buttons != null) {
            for (VertexButton nodeButton : buttons) {
                nodeButton.mouseDownEvent((int) getMouseX(), (int) getMouseY());
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

        if (buttons != null) {
            for (ModeButton modeButton : buttons) {
                renderHelper.contextDraw(renderMatrix4f, context, modeButton);
            }
        }

        animation = Math.clamp(animation + (animationSpeed * animationStage.direction()), 0, 1f);
        if (animationStage.equals(AnimationStage.OPENING) && animation >= 1f) {
            animationStage = AnimationStage.OPEN;
        }
        if (animationStage.equals(AnimationStage.CLOSING) && animation <= 0) {
            NodeSwitcher.nodeSwitchScreen = null;
            super.close();
        }

    }

    @Override
    public void close() {
        if (animationStage.direction() == 1) {
            if (buttons != null) {
                for (ModeButton modeButton : buttons) {
                    if (modeButton.containsPoint((int) getMouseX(), (int) getMouseY(), true)) {
                        Mod.sendCommand("/server " + modeButton.modeName);
                        break;
                    }
                }
            }
            animationStage = AnimationStage.CLOSING;
        }
    }
}
