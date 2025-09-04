package mia.miamod.features.impl.general;

import mia.miamod.Mod;
import mia.miamod.core.KeyBindCategories;
import mia.miamod.core.KeyBindManager;
import mia.miamod.core.MiaKeyBind;
import mia.miamod.core.NetworkManager;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.listeners.impl.PacketListener;
import mia.miamod.features.listeners.impl.RegisterKeyBindEvent;
import mia.miamod.features.listeners.impl.ServerConnectionEventListener;
import mia.miamod.features.listeners.impl.TickEvent;
import mia.miamod.render.hud_screens.InGameHudManager;
import mia.miamod.render.hud_screens.ModeSwitchScreen;
import mia.miamod.render.hud_screens.NodeSwitchScreen;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class NodeSwitcher extends Feature implements RegisterKeyBindEvent, TickEvent, ServerConnectionEventListener, PacketListener {
    private final MiaKeyBind openSwitcher;
    public static NodeSwitchScreen nodeSwitchScreen;
    private boolean isNotPressed = true;

    private static final int completionPacketID = 49385;
    private static List<String> serverIds;

    public NodeSwitcher(Categories category) {
        super(category, "Node Switcher", "nodeswitch", "leftrightleft");
        openSwitcher = new MiaKeyBind("Node Switcher", GLFW.GLFW_KEY_H, KeyBindCategories.GENERAL_CATEGORY);
    }

    @Override
    public void registerKeyBind() {
        KeyBindManager.registerKeyBind(openSwitcher);
    }


    @Override
    public void tickR(int tick) {
        if (ServerManager.isNotOnDiamondFire()) return;
        if (!(InGameHudManager.getInGameHudScreen() instanceof NodeSwitchScreen) && (InGameHudManager.getInGameHudScreen() == null)) {
            if (openSwitcher.isPressed() && nodeSwitchScreen == null && (isNotPressed)) {
                nodeSwitchScreen = new NodeSwitchScreen();
                InGameHudManager.setInGameHudScreen(nodeSwitchScreen);
                isNotPressed = false;
            }
        }
        if (serverIds == null && isNotPressed) {
            Mod.messageError("CommandSuggestionsS2CPacket id=" + completionPacketID + " has not been received yet.");
            isNotPressed = false;
        }
        if (!openSwitcher.isPressed()) {
            if (nodeSwitchScreen != null) {
                nodeSwitchScreen.close();
            }
            isNotPressed = true;
        }

    }

    @Override
    public void serverConnectJoin(ClientPlayNetworkHandler networkHandler, PacketSender sender, MinecraftClient minecraftServer) {
        NetworkManager.sendPacket(
                new RequestCommandCompletionsC2SPacket(completionPacketID, "/server ")
        );
    }


    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof CommandSuggestionsS2CPacket(int id, int start, int length, List<CommandSuggestionsS2CPacket.Suggestion> suggestions)) {
            if (id == completionPacketID) {
                serverIds = suggestions.stream().map(CommandSuggestionsS2CPacket.Suggestion::text).collect(Collectors.toCollection(ArrayList::new));
            }
        }
    }

    public static List<String> getServerIds() {
        return serverIds;
    }

    @Override
    public void tickF(int tick) {

    }

    @Override
    public void serverConnectInit(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {

    }

    @Override
    public void serverConnectDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {

    }

    @Override
    public void DFConnectJoin(ClientPlayNetworkHandler networkHandler) {

    }

    @Override
    public void DFConnectDisconnect(ClientPlayNetworkHandler networkHandler) {

    }



    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }
}
