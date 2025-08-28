package mia.miamod.features.impl.internal;

import mia.miamod.Mod;
import mia.miamod.core.KeyBindCategories;
import mia.miamod.core.KeyBindManager;
import mia.miamod.core.MiaKeyBind;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.listeners.impl.AlwaysEnabled;
import mia.miamod.features.listeners.impl.RegisterKeyBindEvent;
import mia.miamod.features.listeners.impl.RenderHUD;
import mia.miamod.features.listeners.impl.TickEvent;
import mia.miamod.render.screens.ConfigScreen;
import mia.miamod.render.screens.AnimationStage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;

public final class ConfigScreenFeature extends Feature implements RegisterKeyBindEvent, TickEvent, RenderHUD, AlwaysEnabled {
    private static MiaKeyBind openConfig;
    private static ConfigScreen configScreen;
    public ConfigScreenFeature(Categories category) {
        super(category, "conifg screen", "congifscreen", "description");
        openConfig = new MiaKeyBind("Open Config", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBindCategories.GENERAL_CATEGORY);
    }

    @Override
    public void registerKeyBind() {
        KeyBindManager.registerKeyBind(openConfig);
    }

    @Override
    public void tickR(int tick) {

    }

    @Override
    public void tickF(int tick) {
        if (openConfig.isPressed())  {
            openConfigScreen(null);
        }
    }

    public static Screen openConfigScreen(Screen parent) {
        if (!(Mod.getCurrentScreen() instanceof ConfigScreen) && (configScreen == null)) {
            Mod.setCurrentScreen(configScreen = new ConfigScreen(parent));
        }
        return configScreen;
    }
    public static void clearConfigScreen() {
        configScreen = null;
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {
        if (configScreen == null) return;
        if (configScreen.getStage().equals(AnimationStage.CLOSING)) configScreen.render(context, 0, 0, tickCounter.getTickDelta(false));
        if (configScreen.getStage().equals(AnimationStage.CLOSED)) clearConfigScreen();
    }
}
