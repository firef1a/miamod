package mia.miamod.features.impl.support;

import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import mia.miamod.features.listeners.DFMode;
import mia.miamod.features.listeners.impl.PlayerUseEventListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public final class CSFunctionTeleporter extends Feature implements PlayerUseEventListener {
    public CSFunctionTeleporter(Categories category) {
        super(category, "CS › Function Teleporter", "signcolors", "Right click function and process call signs in code spectate to teleport to them.");
    }

    @Override
    public void useBlockCallback(PlayerEntity player, World world, Hand hand, HitResult hitResult) {

    }

    @Override
    public void useItemCallback(PlayerEntity player, World world, Hand hand) {
        if (LocationAPI.getMode().equals(DFMode.CODE_SPECTATE)) {
            if (Mod.MC.player != null && world != null) {
                HitResult hitResult = Mod.MC.player.raycast(4.5, 0, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockEntity blockState = world.getBlockEntity(blockHitResult.getBlockPos());

                    if (blockState instanceof SignBlockEntity signBlockEntity) {
                        Text[] frontText = signBlockEntity.getFrontText().getMessages(true);
                        String funcName = frontText[1].getString();
                        if (!funcName.isEmpty()) {
                            for (Text text : frontText) {
                                String content = text.getString();
                                if (content.equals("CALL FUNCTION")) {
                                    Mod.message(Text.literal("Teleported to function: ").withColor(ColorBank.WHITE).append(Text.literal(funcName).withColor(ColorBank.WHITE_GRAY)));
                                    Mod.sendCommand("/ctp " + funcName);
                                    break;
                                }
                                if (content.equals("START PROCESS")) {
                                    Mod.message(Text.literal("Teleported to process: ").withColor(ColorBank.WHITE).append(Text.literal(funcName).withColor(ColorBank.WHITE_GRAY)));
                                    Mod.sendCommand("/ctp process " + funcName);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void useEntityCallback(PlayerEntity player, World world, Hand hand, Entity entity, HitResult hitResult) {

    }
}
