package mia.miamod.features.impl.support;

import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.ServerConnectionEventListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class AutoQueue extends Feature implements ServerConnectionEventListener {

    public AutoQueue(Categories category) {
        super(category, "Auto /queue", "autoqueue", "Automatically runs /queue on join.");
    }

    @Override
    public void serverConnectInit(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {

    }

    @Override
    public void serverConnectJoin(ClientPlayNetworkHandler networkHandler, PacketSender sender, MinecraftClient minecraftServer) {

    }

    @Override
    public void serverConnectDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {

    }

    @Override
    public void DFConnectJoin(ClientPlayNetworkHandler networkHandler) {
        CommandScheduler.addCommand(new ScheduledCommand("queue"));
    }

    @Override
    public void DFConnectDisconnect(ClientPlayNetworkHandler networkHandler) {

    }
}
