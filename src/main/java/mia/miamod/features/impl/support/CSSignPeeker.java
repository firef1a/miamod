package mia.miamod.features.impl.support;

import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import mia.miamod.features.listeners.DFMode;
import mia.miamod.features.listeners.impl.RenderHUD;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CSSignPeeker extends Feature implements RenderHUD {
    public CSSignPeeker(Categories category) {
        super( category, "CS › Sign Peeker", "cssignpeeker", "sign that ur gay");
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {
        if (!LocationAPI.getMode().equals(DFMode.CODE_SPECTATE)) return;
        if (Mod.MC.player == null) return;
        if (Mod.MC.world == null) return;

        HitResult hitResult = Mod.MC.player.raycast(4.5, 0, false);
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();

            BlockEntity blockState = Mod.MC.world.getBlockEntity(blockPos);
            if (blockState instanceof SignBlockEntity signBlockEntity) {
                ArrayList<String> messages = new ArrayList<>(Arrays.stream(signBlockEntity.getFrontText().getMessages(true)).map(Text::getString).toList());
                ArrayList<String> validHeaders = new ArrayList<>(List.of("FUNCTION", "CALL FUNCTION", "START PROCESS", "PROCESS"));

                if (validHeaders.contains(messages.get(0))) {
                    String message = messages.get(1);
                    if (!message.isEmpty()) {
                        context.drawTooltip(Mod.MC.textRenderer, Text.literal(message).withColor(0xd6d6d6), Mod.getScaledWindowWidth() / 2, Mod.getScaledWindowHeight() / 2);
                    }
                }
            }
        }
    }
}
