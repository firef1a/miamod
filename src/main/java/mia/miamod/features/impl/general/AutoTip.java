package mia.miamod.features.impl.general;

import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.ModifiableEventResultType;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.IntegerSliderDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class AutoTip extends Feature implements ChatEventListener {
    public AutoTip(Categories category) {
        super(category, "AutoTip", "autotip", "Automatically tips boosters");
        new BooleanDataField("testing", ParameterIdentifier.of(this, "testing"), true, true);
        new StringDataField("testing", ParameterIdentifier.of(this, "abc"), "hi", true);
        new IntegerDataField("testing", ParameterIdentifier.of(this, "dc"), 23424, true);
        new IntegerSliderDataField("testing", ParameterIdentifier.of(this, "dc"), 50, 0, 100, true);
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        if (Pattern.compile("⏵⏵⏵ Use /tip to show your appreciation and receive a □ token notch!").matcher(message.base().getString()).find()) {
            CommandScheduler.addCommand(new ScheduledCommand("tip", 500L));
        }
        return message.pass();
    }
}
