package mia.miamod.features.impl.moderation;

import mia.miamod.ColorBank;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.general.chat.BetterSCTags;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BetterVanishMSG extends Feature implements ChatEventListener {
    public BetterVanishMSG(Categories category) {
        super(category, "Better Vanish MSG", "bettervanishmsgs", "Changes vanish messages.");
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String text = message.base().getString();
        Matcher matcher;
        boolean betterSCTags = FeatureManager.getFeature(BetterSCTags.class).getEnabled();

        matcher = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.").matcher(text);
        if (matcher.find()) {
            return message.modified(Text.empty()
                    .append(betterSCTags ? BetterSCTags.MOD : Text.literal("[MOD]").withColor(ColorBank.MC_DARK_GREEN))
                    .append(Text.literal(" Mod Vanish Enabled").withColor(ColorBank.MC_GRAY))
                    .append(Text.literal(" ✔").withColor(ColorBank.MC_GREEN)));
        }

        matcher = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.").matcher(text);
        if (matcher.find()) {
            return message.modified(Text.empty()
                    .append(betterSCTags ? BetterSCTags.MOD : Text.literal("[MOD]").withColor(ColorBank.MC_DARK_GREEN))
                    .append(Text.literal(" Mod Vanish Disabled").withColor(ColorBank.MC_GRAY))
                    .append(Text.literal(" ❌").withColor(ColorBank.MC_RED)));
        }

        return message.pass();
    }
}
