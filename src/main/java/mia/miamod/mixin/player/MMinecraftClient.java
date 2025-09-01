package mia.miamod.mixin.player;

import mia.miamod.features.FeatureManager;
import mia.miamod.features.listeners.impl.PlayerUseEventListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MMinecraftClient {
    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    public ClientWorld world;

    @Inject(at = @At("HEAD"), method = "doItemUse")
    private void onRightClick(CallbackInfo ci) {
        FeatureManager.implementFeatureListener(PlayerUseEventListener.class, feature -> feature.useItemCallback(player, world, Hand.MAIN_HAND));

    }
}
