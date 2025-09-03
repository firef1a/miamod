package mia.miamod.features.impl.support.supporthud;

import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.RenderHUD;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SupportHUD extends Feature implements RenderHUD, ChatEventListener {
    private static final LinkedHashMap<String, SessionEntry> sessionQueue = new LinkedHashMap<>();
    private static final LinkedHashMap<String, SupportQuestion> supportQuestions = new LinkedHashMap<>();
    private static SessionEntry sessionBuilder;

    private static final String queueHeader = "» Current Queue:";

    private static SessionEntry currentSupportSession = null;

    public SupportHUD(Categories category) {
        super(category, "Support HUD", "supporthud", "get back to work intern");
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String text = message.base().getString();
        String playerName = Mod.getPlayerName();
        Matcher matcher;

        // queue command response
        if (text.startsWith(queueHeader)) { sessionQueue.clear(); }

        matcher = Pattern.compile("^#\\d* (.{3,16}) ▶ (\\d*):(\\d*):(\\d*)").matcher(text);
        if (matcher.find()) {
            sessionBuilder = new SessionEntry(matcher.group(1), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)) ,Integer.parseInt(matcher.group(4)));
        }

        matcher = Pattern.compile("^ {2}▶ Reason: ([A-Za-z0-9_]*)").matcher(text);
        if (matcher.find()) {
            sessionBuilder.setReason(matcher.group(1));
            sessionQueue.put(sessionBuilder.name, sessionBuilder);
        }


        // queue missing kill event
        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) joined the support queue\\. ▶ Reason: ([A-Za-z0-9_]*)").matcher(text);
        if (matcher.find()) {
            sessionQueue.put(matcher.group(1), new SessionEntry(matcher.group(1), matcher.group(2), System.currentTimeMillis()));
        }

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) left the support queue\\.").matcher(text);
        if (matcher.find()) { 
            sessionQueue.remove(matcher.group(1));
        }

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) entered a session with (.{3,16})\\.[A-Za-z0-9_]*").matcher(text);
        if (matcher.find()) {
            String supporteeName = matcher.group(2);
            //Mod.log(matcher.group(1) + " " + playerName);
            if (matcher.group(1).equals(playerName)) {
                currentSupportSession = sessionQueue.getOrDefault(playerName, new SessionEntry(playerName, "failed to grab reason :/", System.currentTimeMillis()));
            }

            sessionQueue.remove(supporteeName);
        }

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) terminated a session with (.{3,16})\\. ▶ [A-Za-z0-9_]*").matcher(text);
        if (matcher.find() && matcher.group(1).equals(playerName)) {currentSupportSession = null;}

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) finished a session with (.{3,16})\\. ▶ [A-Za-z0-9_]*").matcher(text);
        if (matcher.find() && matcher.group(1).equals(playerName)) {currentSupportSession = null;}

        // questions unfinished
        matcher = Pattern.compile("^» Support Question: \\(Click to answer\\)\\nAsked by (.{3,16}) (.{3,16})\\n([A-Za-z0-9_]*)").matcher(text);
        if (matcher.find()) {
            supportQuestions.put(matcher.group(1), new SupportQuestion(matcher.group(1), matcher.group(2), matcher.group(3), System.currentTimeMillis()));
        }

        matcher = Pattern.compile("^ {39}\\n» (.{3,16}) has answered (.{3,16})'(?:s|) question:\\n\\n[A-Za-z0-9_]*\\n {39}").matcher(text);
        if (matcher.find()) { supportQuestions.remove(matcher.group(2)); }
        
        return message.pass();
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {

    }
}
