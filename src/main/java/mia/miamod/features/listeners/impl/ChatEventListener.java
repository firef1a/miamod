package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface ChatEventListener extends AbstractEventListener {
    ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci);
}
