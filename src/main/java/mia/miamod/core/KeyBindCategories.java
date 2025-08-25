package mia.miamod.core;

import mia.miamod.Mod;

public enum KeyBindCategories {
    GENERAL_CATEGORY(Mod.MOD_ID);

    private final String name;
    KeyBindCategories(String name) { this.name = name; }

    public final String displayName() { return this.name; }
}
