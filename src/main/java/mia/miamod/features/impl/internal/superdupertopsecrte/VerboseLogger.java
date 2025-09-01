package mia.miamod.features.impl.internal.superdupertopsecrte;

import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.IntegerSliderDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class VerboseLogger extends Feature {
    public VerboseLogger(Categories category) {
        super(category, "Verbose Chat Logging", "verbosechatlogger", "Adds verbose chat logging.");
    }
}
