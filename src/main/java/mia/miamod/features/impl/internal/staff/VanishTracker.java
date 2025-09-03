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
    private final InternalBooleanDataField adminVanishEnabledField;
    private final InternalBooleanDataField ytVanishEnabledField;

    public static final Pattern VANISH_ENABLED = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.$");
    public static final Pattern VANISH_DISABLED = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.$");

    public static final Pattern VANISH_PREFERENCE_ENABLED = Pattern.compile("^» The preference Mod Vanish has been set to true\\.$");
    public static final Pattern VANISH_PREFERENCE_DISABLED = Pattern.compile("^» The preference Mod Vanish has been set to false\\.$");


    public static final Pattern ADMINV_ENABLED = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.");
    public static final Pattern ADMIN_DISABLED = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.");

    public VanishTracker(Categories category) {
        super(category, "Vanish Tracker", "vstatetracker", "Tracks vanish state");
        modVanishEnabledField = new InternalBooleanDataField("Mod Vanish", ParameterIdentifier.of(this, "mod_vanish"), false, true);
        adminVanishEnabledField = new InternalBooleanDataField("Admin Vanish", ParameterIdentifier.of(this, "admin_vanish"), false, true);
        ytVanishEnabledField = new InternalBooleanDataField("Youtuber Vanish", ParameterIdentifier.of(this, "yt_vanish"), false, true);
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String text = message.base().getString();
        Matcher vMatcher, pMatcher;

        vMatcher = VANISH_ENABLED.matcher(text);
        pMatcher = VANISH_PREFERENCE_ENABLED.matcher(text);
        if (vMatcher.find() || pMatcher.find()) {
            modVanishEnabledField.setValue(true);
        }

        vMatcher = VANISH_DISABLED.matcher(text);
        pMatcher = VANISH_PREFERENCE_DISABLED.matcher(text);
        if (vMatcher.find() || pMatcher.find()) {
            modVanishEnabledField.setValue(false);
        }

        return message.pass();
    }

    public boolean isInModVanish() { return modVanishEnabledField.getValue(); }
}
