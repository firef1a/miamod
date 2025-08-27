package mia.miamod.features.impl.general;

import mia.miamod.core.KeyBindCategories;
import mia.miamod.core.MiaKeyBind;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.TickEvent;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public class ModeSwitcher extends Feature implements TickEvent {
    private final MiaKeyBind openSwitcher;
    public ModeSwitcher(Categories category) {
        super(category, "Mode Switcher", "modeswitch", "idk press button to switch ur mode or smth");
        new BooleanDataField("testing", ParameterIdentifier.of(this, "testing"), true);
        new StringDataField("testing", ParameterIdentifier.of(this, "testing"), "hi");
        new IntegerDataField("testing", ParameterIdentifier.of(this, "testing"), 23424);

        openSwitcher = new MiaKeyBind("Mode Switcher", GLFW.GLFW_KEY_J, KeyBindCategories.GENERAL_CATEGORY);
    }


    @Override
    public void tickR(int tick) {

    }

    @Override
    public void tickF(int tick) {

    }
}
