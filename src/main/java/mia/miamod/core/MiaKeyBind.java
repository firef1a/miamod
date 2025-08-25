package mia.miamod.core;

import net.minecraft.client.option.KeyBinding;

public class MiaKeyBind extends KeyBinding {
    private static long lastPressed;

    public MiaKeyBind(String translationKey, int code, KeyBindCategories category) {
        super(translationKey, code, category.displayName());
        lastPressed = 0L;
    }

    public boolean isPressed() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastPressed;
        if (diff < 100L) return true;
        if (super.isPressed()) {
            lastPressed = currentTime;
            return true;
        };
        return false;
    }
}
