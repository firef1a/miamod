package mia.miamod.features.impl.general.chat;

import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.impl.RenderHUD;
import mia.miamod.features.listeners.impl.TickEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Pattern;

public final class MessageChatHudFeature extends Feature implements RenderHUD, TickEvent {
    private MessageChatHud messageChatHud;
    private boolean isLog = false;

    private static final Pattern MOD_LOG = Pattern.compile("^--------------\\[ Mod Log \\| .* ]--------------");
    private static final Pattern ADMIN_LOG = Pattern.compile("^--------------\\[ Admin Log \\| .* ]--------------");
    private static final List<Pattern> patterns = List.of(
            Pattern.compile("^» Support Question: \\(Click to answer\\)\\nAsked by ([a-zA-Z0-9_]{3,16}) \\[.*]\\n(.*)"),
            Pattern.compile("^ {39}\\n» ([a-zA-Z0-9_]{3,16}) has answered ([a-zA-Z0-9_]{3,16})'(?:s|) question:\\n\\n.*\\n {39}"),
            Pattern.compile("^#\\d* ([a-zA-Z0-9_]{3,16}) ▶ (\\d*):(\\d*):(\\d*)"),
            Pattern.compile("^ {2}▶ Reason: (.*)"),
            Pattern.compile("^\\[SUPPORT]"),
            Pattern.compile("^» Current Queue:"),
            Pattern.compile("^» \\d*\\. [a-zA-Z0-9_]{3,16}\\(\\d* sessions\\)"),
            Pattern.compile("^\\[[a-zA-Z0-9_]{3,16} → You] "),
            Pattern.compile("^\\[You → [a-zA-Z0-9_]{3,16}] "),
            Pattern.compile("^\\* (\\[.*])*([a-zA-Z0-9_]{3,16}): (.*)"),

            Pattern.compile("^\\[MOD] "),
            MOD_LOG,
            Pattern.compile("^! Incoming Report \\(([a-zA-Z0-9_]{3,16})\\)"),
            Pattern.compile("^(?:\\[Silent] | ||)([a-zA-Z0-9_]{3,16}) (?:tempmuted|muted|banned|tempbanned|warned|unwarned|unbanned|unmuted) ([a-zA-Z0-9_]{3,16})"),
            Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\."),
            Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\."),
            Pattern.compile("^IP report for user [a-zA-Z0-9_]{3,16}:"),


            Pattern.compile("^\\[ADMIN] "),
            ADMIN_LOG
    );


    public MessageChatHudFeature(Categories category) {
        super(category, "Message Chat Hud", "mch", "Separate chat hud for important stuff");
    }

    public boolean addMessage(ModifiableEventData<Text> eventData) {
        Text base = eventData.base();
        Text modified = eventData.modified();
        String baseString = base.getString();
        if (FeatureManager.getFeature(MessageChatHudFeature.class) == null || (!FeatureManager.getFeature(MessageChatHudFeature.class).getEnabled())) return false;

        boolean matches = false;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(baseString).find()) {
                matches = true;
                break;
            }
        }
        if (MOD_LOG.matcher(baseString).find() || ADMIN_LOG.matcher(baseString).find()) isLog = !isLog;
        matches = matches || isLog;

        if (matches) {
            messageChatHud.addMessage(modified);
        }
        return matches;
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {
        //if (!this.getEnabled()) return;
        //Mod.MC.inGameHud.getChatHud().isChatFocused()
        if (!(Mod.getCurrentScreen() instanceof ChatScreen chatScreen) && !Mod.MC.options.hudHidden) {
            messageChatHud.render(context, Mod.tick, 0,0, Mod.getCurrentScreen() instanceof ChatScreen);
        }
    }

    public MessageChatHud getMessageChatHud() { return messageChatHud; }

    @Override
    public void tickR(int tick) {
        if (messageChatHud == null)  messageChatHud = new MessageChatHud(Mod.MC);
        //messageChatHud.tickRemovalQueueIfExists();

    }

    @Override
    public void tickF(int tick) {

    }
}
