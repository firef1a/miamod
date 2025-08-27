package mia.miamod.features.impl.internal.commands;

import com.mojang.brigadier.CommandDispatcher;
import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.listeners.impl.AlwaysEnabled;
import mia.miamod.features.listeners.impl.RegisterCommandListener;
import mia.miamod.features.listeners.impl.ServerConnectionEventListener;
import mia.miamod.features.listeners.impl.TickEvent;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;

import net.minecraft.client.gui.screen.Screen;
import java.util.ArrayList;

public class CommandScheduler extends Feature implements TickEvent, ServerConnectionEventListener, AlwaysEnabled {
    private static ArrayList<ScheduledCommand> scheduledCommands;
    private static long nextTimestamp;
    public CommandScheduler(Categories category) {
        super(category, "cmd scheduler", "cmd_scheduler", "Schedules non-player executed commands.");
        scheduledCommands = new ArrayList<>();
        nextTimestamp = 0L;
    }

    public static void addCommand(ScheduledCommand scheduledCommand) {
        scheduledCommands.add(scheduledCommand);
    }

    @Override
    public void tickR(int tick) {
        if (!ServerManager.isOnDiamondFire()) return;

        long currentTimestamp = System.currentTimeMillis();
        if (nextTimestamp > currentTimestamp) return;

        if (!scheduledCommands.isEmpty() && Mod.MC.getNetworkHandler() != null && Mod.MC.world != null && Mod.MC.player != null) {
            ScheduledCommand scheduledCommand = scheduledCommands.removeFirst();
            Mod.sendCommand("/" + scheduledCommand.command());
            nextTimestamp = currentTimestamp + scheduledCommand.getDelay();
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
