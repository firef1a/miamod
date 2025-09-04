package mia.miamod.features.impl.development;

import mia.miamod.core.KeyBindCategories;
import mia.miamod.core.KeyBindManager;
import mia.miamod.core.MiaKeyBind;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.listeners.impl.RegisterKeyBindEvent;
import mia.miamod.features.listeners.impl.TickEvent;
import mia.miamod.render.hud_screens.InGameHudManager;
import mia.miamod.render.hud_screens.InGameHudScreen;
import mia.miamod.render.hud_screens.ModeSwitchScreen;
import mia.miamod.render.hud_screens.NodeSwitchScreen;
import org.lwjgl.glfw.GLFW;

public final class ModeSwitcher extends Feature implements RegisterKeyBindEvent, TickEvent {
    private final MiaKeyBind openSwitcher;
    public static ModeSwitchScreen modeSwitchScreen;
    private boolean isNotPressed = true;

    public ModeSwitcher(Categories category) {
        super(category, "Mode Switcher", "modeswitch", "bot or top");
        openSwitcher = new MiaKeyBind("Mode Switcher", GLFW.GLFW_KEY_J, KeyBindCategories.GENERAL_CATEGORY);
    }

    @Override
    public void registerKeyBind() {
        KeyBindManager.registerKeyBind(openSwitcher);
    }


    @Override
    public void tickR(int tick) {
        if (ServerManager.isNotOnDiamondFire()) return;
        if (!(InGameHudManager.getInGameHudScreen() instanceof NodeSwitchScreen) && (InGameHudManager.getInGameHudScreen() == null)) {
            if (openSwitcher.isPressed() && modeSwitchScreen == null && (isNotPressed)) {
                modeSwitchScreen = new ModeSwitchScreen();
                InGameHudManager.setInGameHudScreen(modeSwitchScreen);
                isNotPressed = false;
            }
        }
        if (!openSwitcher.isPressed()) {
            if (modeSwitchScreen != null) {
                modeSwitchScreen.close();
            }
            isNotPressed = true;
        }

    }

    @Override
    public void tickF(int tick) {

    }
}
