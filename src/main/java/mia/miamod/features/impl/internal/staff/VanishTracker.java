package mia.miamod.features.impl.internal.staff;

import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.AlwaysEnabled;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VanishTracker extends Feature implements ChatEventListener, AlwaysEnabled {
    private final InternalBooleanDataField modVanishEnabledField;

    public VanishTracker(Categories category) {
        super(category, "Vanish Tracker", "vstatetracker", "Tracks vanish state");
        modVanishEnabledField = new InternalBooleanDataField("Mod Vanish", ParameterIdentifier.of(this, "mod_vanish"), false, true);
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String text = message.base().getString();
        Matcher vMatcher, pMatcher;

        vMatcher = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.").matcher(text);
        pMatcher = Pattern.compile("^» The preference Mod Vanish has been set to true\\.").matcher(text);
        if (vMatcher.find() || pMatcher.find()) {
            modVanishEnabledField.setValue(true);
        }

        vMatcher = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.").matcher(text);
        pMatcher = Pattern.compile("^» The preference Mod Vanish has been set to false\\.").matcher(text);
        if (vMatcher.find() || pMatcher.find()) {
            modVanishEnabledField.setValue(false);
        }

        return message.pass();
    }

    public boolean isInModVanish() { return modVanishEnabledField.getValue(); }
}
