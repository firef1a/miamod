package mia.miamod.features.impl.internal.commands;

import com.mojang.brigadier.CommandDispatcher;
import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.*;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandScheduler extends Feature implements TickEvent, ServerConnectionEventListener, ChatEventListener, AlwaysEnabled {
    private static ArrayList<ScheduledCommand> scheduledCommands;
    private static ArrayList<ChatConsumer> chatConsumers;
    private static long nextTimestamp;

    public CommandScheduler(Categories category) {
        super(category, "cmd scheduler", "cmd_scheduler", "Schedules non-player executed commands.");
        scheduledCommands = new ArrayList<>();
        chatConsumers = new ArrayList<>();
        nextTimestamp = 0L;
    }

    public static void addCommand(ScheduledCommand scheduledCommand) {
        scheduledCommands.add(scheduledCommand);
    }

    public static void addChatConsumer(ChatConsumer chatConsumer) {
        chatConsumers.add(chatConsumer);
    }

    public static void removeChatConsumer(ChatConsumer chatConsumer) {
        chatConsumers.remove(chatConsumer);
    }

    public static long getMaxCommandDelay() {
        long delay = 0L;

        for (ScheduledCommand scheduledCommand : scheduledCommands) {
            delay += scheduledCommand.getDelay();
        }

        return delay;
    }

    public static ArrayList<ScheduledCommand> getScheduledCommands() { return scheduledCommands; }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String content = message.base().getString();

        ArrayList<ChatConsumer> removeConsumers = new ArrayList<>();
        for (ChatConsumer chatConsumer : chatConsumers) {
            Matcher matcher = chatConsumer.pattern().matcher(content);

            if (matcher.find()) {
                chatConsumer.successfulMatch().accept(matcher);
                if (chatConsumer.cancelMessage()) ci.cancel();
                removeConsumers.add(chatConsumer);
                break;
            }
        }
        chatConsumers.removeAll(removeConsumers);
        return message.pass();
    }

    @Override
    public void tickR(int tick) {
        if (!ServerManager.isOnDiamondFire()) return;

        long currentTimestamp = System.currentTimeMillis();

        if (!scheduledCommands.isEmpty() && Mod.MC.getNetworkHandler() != null && Mod.MC.world != null && Mod.MC.player != null) {
            ScheduledCommand scheduledCommand = scheduledCommands.getFirst();
            if (nextTimestamp + scheduledCommand.delay() > currentTimestamp) return;
            scheduledCommands.removeFirst();

            for (ChatConsumer chatConsumer : scheduledCommand.commandConsumers()) {
                chatConsumer.setTimestamp();
                addChatConsumer(chatConsumer);
            }
            Mod.sendCommand("/" + scheduledCommand.command());

            nextTimestamp = currentTimestamp + scheduledCommand.getDelay();
        }

        if (!chatConsumers.isEmpty()) {
            ArrayList<ChatConsumer> removeConsumers = new ArrayList<>();
            for (ChatConsumer chatConsumer : chatConsumers) {
                if (System.currentTimeMillis() > chatConsumer.timestamp()) {
                    chatConsumer.timeoutEvent.run();
                    removeConsumers.add(chatConsumer);
                }
            }
            chatConsumers.removeAll(removeConsumers);
        }
    }

    @Override
    public void tickF(int tick) {

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
        nextTimestamp = System.currentTimeMillis() + 250L;
    }

    @Override
    public void DFConnectDisconnect(ClientPlayNetworkHandler networkHandler) {

    }
}
