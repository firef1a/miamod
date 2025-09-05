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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NodeSwitchScreen extends InGameHudScreen {
    private ArrayList<NodeButton> buttons;

    private AnimationStage animationStage;
    private static final float animationSpeed = 0.135f;
    private float animation;

    private List<Float> renderLineBreaks;

    static final int bwidth = 65;
    static final int bheight = 30;

    static final int xmargin = 6, ymargin = 4;
    static final int maxix = 7;

    private float ylines;

    public NodeSwitchScreen() {
        super();
        animationStage = AnimationStage.OPENING;
    }

    private static final class NodeButton extends VertexButton {
        public final String modeName;
        public NodeButton(String modeName, Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, Runnable callback) {
            super(matrix4f, x, y, width, height, z, argb, highlightARGB, argb, highlightARGB, callback);
            this.modeName = modeName;
        }

        @Override
        protected void draw(DrawContext context, int mouseX, int mouseY) {
            boolean contains = containsPoint(mouseX, mouseY, true);
            drawRect(context, new ARGB(0x000000, 0.25f));
            drawBorder(context, contains ? (getEnabled() ? this.highlighEnabledARGB() : this.highlightARGB()) : (getEnabled() ? this.enabledARGB() : this.argb()));
        }
    }

    @Override
    protected void init() {
        renderLineBreaks = new ArrayList<>();
        buttons = new ArrayList<>();

        ArrayList<String> mainNodes = NodeSwitcher.getServerIds().stream()
                .filter(string -> Pattern.matches("^node\\d*$", string))
                .sorted(Comparator.comparingInt(a -> Integer.parseInt(a.substring("node".length()))))
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<String> privateNodes = NodeSwitcher.getServerIds().stream()
                .filter(string -> Pattern.matches("^private\\d*$", string))
                .sorted(Comparator.comparingInt(a -> Integer.parseInt(a.substring("private".length()))))
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<String> miscNodes = NodeSwitcher.getServerIds().stream()
                .filter(string -> !(privateNodes.contains(string) || mainNodes.contains(string))
                ).sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        final ArrayList<ArrayList<String>> nodeList = new ArrayList<>(List.of(privateNodes, mainNodes, miscNodes));

        int iy = 0;
        float iblocky = 0;

        ylines = 0;
        for (ArrayList<String> nodes : nodeList) {
            int ix = 0;
            for (String node : nodes) {
                if (ix >= maxix) {
                    ix = 0;
                    ylines++;
                }
                ix++;
            }
            ylines += 1.5f;
        }
        int i = 0;
        for (ArrayList<String> nodes : nodeList) {
            int ix = 0;
            for (String node : nodes) {
                if (ix >= maxix) {
                    ix = 0;
                    iy++;
                }
                float x = ((ix * (bwidth + xmargin)) - ((maxix * (bwidth) + (maxix - 1) * (xmargin)) * 0.5f)) + (bwidth / 2f);
                float y = (((iy * (bheight + ymargin)) + (iblocky * bheight)) - (((ylines) * (bheight) + (ylines - 1) * (ymargin)) * 0.5f)) + (bheight / 2f);

                NodeButton button = new NodeButton(
                    node,
                    new Matrix4f().identity(),
                    x, y,
                    bwidth, bheight,
                    100,
                    new ARGB(0x8ae3ff, 1f),
                    new ARGB(0xebfaff, 1f),
                    this::close
                );

                String nodeString = node.substring(0,1).toUpperCase() + node.substring(1);

                Matcher matcher;
                matcher = Pattern.compile("^(\\D+)(\\d+)$").matcher(node);
                if (matcher.find()) {
                    String main = matcher.group(1);
                    String digits = matcher.group(2);
                    nodeString = main.substring(0,1).toUpperCase() + main.substring(1) + " " + digits;
                }
                TextBufferDrawable nodeText = new TextBufferDrawable(
                        new Matrix4f().identity(),
                        Text.literal(nodeString),
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
                ix++;
            }
            iy += 1;
            iblocky += 0.5f;
            i++;
            if (i <= nodeList.size()-1) {
                renderLineBreaks.add(
                        (((((iy * (bheight + ymargin)) + (iblocky * bheight)) - (((ylines) * (bheight) + (ylines - 1) * (ymargin)) * 0.5f)) - (bheight / 2f)) - (ymargin)) + (bheight/4)
                );
            }
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
            for (NodeButton modeButton : buttons) {
                renderHelper.contextDraw(renderMatrix4f, context, modeButton);
            }
            float menuWidth = (maxix * bwidth) + ((maxix-1) * xmargin);
            matrices.push();
            matrices.multiplyPositionMatrix(renderMatrix4f);
            for (float breaky : renderLineBreaks) {
                for (int lx = (int) (-menuWidth / 2f); lx <= (int)(menuWidth / 2f); lx++) {
                    context.fill(
                            lx, (int) (breaky + ymargin / 2f),
                            lx+1, (int) ((breaky+1)+ ymargin / 2f),
                            100,
                            new ARGB(0xFFFFFF, 1f - (Math.abs(lx) / (menuWidth / 2f))).getARGB()
                    );
                }
            }

            matrices.pop();
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
                for (NodeButton modeButton : buttons) {
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
