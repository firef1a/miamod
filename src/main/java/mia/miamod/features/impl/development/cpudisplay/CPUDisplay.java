package mia.miamod.features.impl.development.cpudisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.core.MathUtils;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.general.title.DFIcons;
import mia.miamod.features.listeners.impl.PacketListener;
import mia.miamod.features.listeners.impl.RenderHUD;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.EnumDataField;
import mia.miamod.render.screens.AnimationStage;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.EasingFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CPUDisplay extends Feature implements RenderHUD, PacketListener {
    private double currentCPU = 0;
    private double displayCPU = 0;
    private long overlayTimeoutTimestamp = 0L;

    private static final double animationSpeed = 0.075;
    private double animation;

    private final EnumDataField<ColorStyle> colorStyleEnumDataField;

    public CPUDisplay(Categories category) {
        super(category, "CPU Display", "cpuwheel", "its a wheel");
        animation = 0;
        colorStyleEnumDataField = new EnumDataField<>("Color Style", ParameterIdentifier.of(this, "color_style"), ColorStyle.SOLID, true);
    }

    private enum ColorStyle {
        SOLID,
        GRADIENT
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {
        if (Mod.MC.options.hudHidden) return;
        if (overlayTimeoutTimestamp >= System.currentTimeMillis()) {
            animation = Math.min(1, animation + animationSpeed);
        } else {
            animation = Math.max(0, animation - animationSpeed);
        }
        if (animation <= 0) return;

        MatrixStack matrices = context.getMatrices();

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int innerRadius = 6;
        int outerRadius = 15;
        int segments = 100;

        int centerX = 65;
        int centerY = 35;

        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.push();
        matrices.scale(EasingFunctions.easeInOutCubic((float) animation), EasingFunctions.easeInOutCubic((float) animation),1f);
        matrices.push();
        Matrix4f transformationMatrix = matrices.peek().getPositionMatrix();

        for (double i = 0; i <= segments; i++) {
            int color;
            double percentage = (i / segments);
            if ((1 - displayCPU) >= percentage) {
                color = 0x424242;
            } else {
                color = ARGB.lerpColor(0x85ff59, 0xff4242, colorStyleEnumDataField.getValue().equals(ColorStyle.SOLID) ? (float) displayCPU : (float) (1-percentage));
            }
            double angle = (percentage * Math.PI * 2);

            float innerX = (float) (Math.cos(angle) * innerRadius);
            float innerY = (float) (Math.sin(angle) * innerRadius * -1);

            float outerX = (float) (Math.cos(angle) * outerRadius);
            float outerY = (float) (Math.sin(angle) * outerRadius * -1);

            buffer.vertex(transformationMatrix, innerX, innerY, 5).color(ARGB.getARGB(color));
            buffer.vertex(transformationMatrix, outerX, outerY, 5).color(ARGB.getARGB(color));
        }


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();


        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        double cpu = (double )((int) ((displayCPU * 100.0) * 100.0)) / 100.0;
        String cpuString = String.valueOf(cpu).split("\\.")[1].length() <= 1 ? cpu + "0" : String.valueOf(cpu);

        Mod.MC.textRenderer.draw(
                cpuString + "%",
                + outerRadius + 4,
                - (Mod.MC.textRenderer.fontHeight / 2),
                ARGB.getARGB(ColorBank.WHITE),
                true,
                transformationMatrix,
                context.vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                15728880
        );

        matrices.pop();
        matrices.pop();
        matrices.pop();

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        displayCPU = displayCPU + ((currentCPU - displayCPU) / 4);
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (Mod.MC.player == null) return;
        Matcher matcher;
        boolean found = false;

        if (packet instanceof OverlayMessageS2CPacket(Text text)) {
            Pattern pattern = Pattern.compile("CPU Usage: \\[▮{20}] \\((\\d+\\.\\d+)%\\)");
            matcher = pattern.matcher(text.getString());

            if (matcher.find()) {
                currentCPU = Double.parseDouble(matcher.group(1)) / 100.0;
                ci.cancel();
                found = true;
            }
        }

        if (found) {
            overlayTimeoutTimestamp = System.currentTimeMillis() + 1500L;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }
}
