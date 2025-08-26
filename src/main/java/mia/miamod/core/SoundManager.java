package mia.miamod.core;

import mia.miamod.Mod;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class SoundManager {
    public static void playUIButtonClick() {
        Mod.MC.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.UI_BUTTON_CLICK, // The SoundEvent to play
                1.0F // The pitch (1.0F is normal pitch)
        ));
    }
}
