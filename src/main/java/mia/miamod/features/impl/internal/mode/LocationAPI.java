package mia.miamod.features.impl.internal.mode;

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

public final class LocationAPI extends Feature implements ChatEventListener {
    public LocationAPI(Categories category) {
        super(category, "LocationAPI", "locapi", "Tracks state and location across diamondfire");
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {

        return message.pass();
    }
}
