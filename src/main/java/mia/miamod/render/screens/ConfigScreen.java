package mia.miamod.render.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.impl.general.ConfigScreenFeature;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.RenderContextHelper;
import mia.miamod.render.util.RenderHelper;
import mia.miamod.render.util.data.TextPrimative;
import mia.miamod.render.util.data.VertexButton;
import mia.miamod.render.util.data.VertexRect;
import mia.miamod.render.util.data.extedndsafsagaydsgyg;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private ConfigScreenStage configScreenStage;
    private float animation;
    private static final float animationSpeed = 0.05F;//0.05F;
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Config Screen"));
        this.parent = parent;
        openStage();
    }

    private float easeInOutCubic(float x) {
        return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
    }

    private float easeInOutCircular(float x) {
        return (float) (x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(context, Integer.MAX_VALUE, Integer.MAX_VALUE, delta);

        // create stack
        Tessellator tessellator = Tessellator.getInstance();
        VertexConsumerProvider.Immediate vertexConsumerProvider = context.vertexConsumers;
        RenderHelper renderHelper = new RenderHelper(tessellator, vertexConsumerProvider, mouseX, mouseY, delta);
        MatrixStack matrices = context.getMatrices();

        // enable settings

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // start animation layer

        matrices.push();

        float centerX = (Mod.getScaledWindowWidth() / 2F);
        float centerY = (Mod.getScaledWindowHeight() / 2F);
        float width = 600;
        float height = 400;
        float topLeftX = -width/2;
        float topLeftY = -height/2;

        matrices.translate(centerX, centerY, 0);
        matrices.push();

        // open/close animation
        matrices.scale(easeInOutCubic(animation), easeInOutCubic(animation),1);

        if (configScreenStage.hasAnimation()) {
            Vector3f axis = new Vector3f(-1, 1,0).normalize();
            float angle = (float) (Math.PI / 2);
            matrices.multiply(RotationAxis.of(axis).rotation((-angle) * (1 - easeInOutCubic(animation))));
        }

        //matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) ((float) (((double) (mouseX - (Mod.getScaledWindowWidth()) / 2)) / Mod.getScaledWindowWidth()) * (-Math.PI))));
        //matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) ((float) (((double) (mouseY - (Mod.getScaledWindowHeight()) / 2)) / Mod.getScaledWindowHeight()) * (Math.PI))));

        matrices.push();

        // render stuff
        int topBarHeight = 20;
        int sidebarWidth = 100;

        int backgroundColor = ColorBank.BLACK; //0xaf7cf2;

        VertexRect screen = new VertexRect(
                matrices,
                topLeftX,
                topLeftY,
                width,
                height,
                0,
                new ARGB(backgroundColor, 0));

        VertexRect topBar = new VertexRect(
                matrices,
                0,
                0,
                width,
                topBarHeight,
                screen.getZ()+1,
                new ARGB(backgroundColor, 0.85));
        screen.addDrawable(topBar);

        VertexRect sidebar = new VertexRect(
                matrices,
                0,
                topBar.getHeight(),
                sidebarWidth,
                height - (topBar.getHeight()),
                screen.getZ()+1,
                new ARGB(backgroundColor, 0.75));
        topBar.addDrawable(sidebar);

        VertexRect main = new VertexRect(
                matrices,
                sidebar.getWidth(),
                0,
                width - (sidebar.getWidth()),
                height - (topBar.getHeight()),
                screen.getZ()+1,
                new ARGB(backgroundColor, 0.55));
        sidebar.addDrawable(main);

        TextPrimative miamod_config_text = new TextPrimative(
                matrices,
                Text.literal("miamod config").styled((style -> style.withItalic(true))).withColor(ColorBank.MC_GRAY),
                 (topBarHeight / 2F) - (Mod.MC.textRenderer.fontHeight / 2F),
                (topBarHeight / 2F) - (Mod.MC.textRenderer.fontHeight / 2F),
                topBar.getZ() - 1F,
                new ARGB(ColorBank.WHITE, 1F),
                true
        );
        topBar.addDrawable(miamod_config_text);

        List<String> categories = new ArrayList<>(List.of(
                "general",
                "editor",
                "developer",
                "navigation",
                "interaction",
                "visual",
                "amphetamine",
                "lexapro",
                "estrogen",
                "support",
                "moderation",
                "internal"
        ));

        int i = 0;
        int categoryHeight = 15;

        for (String categoryName : categories) {
            float y = ((categoryHeight + 1) * i);
            VertexButton button = new VertexButton(
                    matrices,
                    0,
                    y,
                    sidebar.getWidth(),
                    categoryHeight,
                    sidebar.getZ()+1,
                    new ARGB(0xcc8de0, 0.65F),
                    new ARGB(0xcc8de0, 0.85F),
                    () -> {}
            );
            sidebar.addDrawable(button);

            float margin = (categoryHeight / 2) - (Mod.MC.textRenderer.fontHeight/2);

            TextPrimative textPrimative = new TextPrimative(
                    matrices,
                    Text.literal(categoryName),
                    margin,
                    margin,
                    button.getZ()+1,
                    new ARGB(ColorBank.WHITE, 1F),
                    true
            );
            button.addDrawable(textPrimative);
            i++;
        }


        // render screen objects
        renderHelper.contextDraw(context, screen);

        // end render
        matrices.pop();
        matrices.pop();
        matrices.pop();

        // disable settings
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderHelper.clearStencil();

        // advance animation
        if (configScreenStage.equals(ConfigScreenStage.OPENING)) {
            animation = Math.min(1F, animation + animationSpeed);
            if (animation >= 1F) configScreenStage = ConfigScreenStage.OPEN;
        }
        if (configScreenStage.equals(ConfigScreenStage.CLOSING)) {
            animation = Math.max(0F, animation - animationSpeed);
            if (animation <= 0F) {
                configScreenStage = ConfigScreenStage.CLOSED;
                if (parent != null) {
                    Mod.MC.setScreen((Screen) parent);
                    ConfigScreenFeature.clearConfigScreen();
                }
            }
        }
    }

    public void openStage() { configScreenStage = ConfigScreenStage.OPENING; }
    public void closeStage() { configScreenStage = ConfigScreenStage.CLOSING; }

    public ConfigScreenStage getStage() {
        return configScreenStage;
    }

    @Override
    public void close() {
        closeStage();
        if (parent == null) Mod.MC.setScreen((Screen) null);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parent == null) return;
        //parent.renderBackground(context, mouseX, mouseY, delta);
        //super.renderBackground(context, mouseX, mouseY, delta);
    }
}
