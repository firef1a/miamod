package mia.miamod.features.impl.general.chat;

import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.text.Text.literal;

public final class BetterSCTags extends Feature implements ChatEventListener {
    public static final Text SUPPORT = getPrefix(0x55aaff);
    public static final Text SR_HELPER = getPrefix(0x7fffd4);
    public static final Text MOD = getPrefix(0x2ad42a);
    public static final Text ADMIN = getPrefix(0x2a70d4);

    private static Text getPrefix(int color) { return Text.empty().append(literal("›").withColor(color)); }

    public BetterSCTags(Categories category) {
        super(category, "Better Staff Tags", "bsct", "Sleek staff chat tags ");
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        Text modified = message.modified();

        // regular stuff
        ArrayList<SCR> scrs = new ArrayList<>(List.of(
                new SCR(Pattern.compile("\\[SUPPORT] "), SUPPORT),
                new SCR(Pattern.compile("\\[MOD] "), MOD),
                new SCR(Pattern.compile("\\[ADMIN] "), ADMIN)
        ));
        for (SCR scr : scrs) modified = replaceTextNew(modified, scr.pattern(), scr.replacement());

        // session peek
        Matcher matcher;
        matcher = Pattern.compile("^\\* (\\[.*])*([a-zA-Z0-9_]{3,16}): (.*)").matcher(message.base().getString());
        if (matcher.find()) {
            String ranks = matcher.group(1);
            String name = matcher.group(2);
            String text = matcher.group(3);
            modified = Text.empty()
                    .append(SR_HELPER.copy().append(" "))
                    .append(Text.literal(name + ": ").withColor(0x2affaa))
                    .append(Text.literal(text).withColor(0x9cffde));
        }

        return message.modified(modified);
    }

    private record SCR(Pattern pattern, Text replacement) {
        @Override
        public Text replacement() {
            return withSpace(replacement);
        }
    };

    private static Text withSpace(Text text) { return text.copy().append(" "); }

    private static Text replaceTextNew(Text text, Pattern pattern, Text replace) {
        MutableText newText = MutableText.of(text.getContent()).setStyle(text.getStyle());
        List<Text> siblings = text.copy().getSiblings();

        if (pattern.matcher(newText.getString()).find()) newText = replace.copy();
        for (Text sibling : siblings) {
            newText.append(replaceTextNew(sibling, pattern, replace));
        }
        return newText;
    }

}
