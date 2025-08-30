package mia.miamod.features.impl.general.chat;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import mia.miamod.Mod;
import mia.miamod.config.ConfigStore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.collection.ArrayListDeque;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Responsible for rendering various game messages such as chat messages or
 * join/leave messages.
 *
 * @see ChatScreen
 */
@Environment(value= EnvType.CLIENT)
public final class MessageChatHud {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_MESSAGES = Integer.MAX_VALUE;
    private static final int MISSING_MESSAGE_INDEX = -1;
    private static final int field_39772 = 4;
    private static final int field_39773 = 4;
    private static final int OFFSET_FROM_BOTTOM = 40;
    private static final int REMOVAL_QUEUE_TICKS = 60;
    private static final double maxVisibleTime = 600.0;
    private static final Text DELETED_MARKER_TEXT = Text.translatable("chat.deleted_marker").formatted(Formatting.GRAY, Formatting.ITALIC);
    private final MinecraftClient client;
    private final ArrayListDeque<String> messageHistory = new ArrayListDeque<>(1000);
    private final List<ChatHudLine> messages = Lists.newArrayList();
    private final List<ChatHudLine.Visible> visibleMessages = Lists.newArrayList();
    private int scrolledLines;
    private boolean hasUnreadNewMessages;
    private final List<RemovalQueuedMessage> removalQueue = new ArrayList<RemovalQueuedMessage>();
    public int xShift;

    public MessageChatHud(MinecraftClient client) {
        this.client = client;
        this.messageHistory.addAll(client.getCommandHistoryManager().getHistory());
    }

    public void tickRemovalQueueIfExists() {
        if (!this.removalQueue.isEmpty()) {
            this.tickRemovalQueue();
        }
    }

    public void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused) {
        int x;
        int w;
        int v;
        int u;
        int t;
        if (this.isChatHidden()) {
            return;
        }
        int i = this.getVisibleLineCount();
        int j = this.visibleMessages.size();
        if (j <= 0) {
            return;
        }
        Profiler profiler = Profilers.get();
        profiler.push("chat");
        float f = (float)this.getChatScale();
        int k = MathHelper.ceil((float)this.getWidth() / f);
        xShift = context.getScaledWindowWidth()-(k+9+4);
        int l = context.getScaledWindowHeight();
        context.getMatrices().push();
        context.getMatrices().scale(f, f, 1.0f);
        context.getMatrices().translate(4.0f, 0.0f, 0.0f);
        int m = MathHelper.floor((float)(l - 40) / f);
        int n = this.getMessageIndex(this.toChatLineX(mouseX), this.toChatLineY(mouseY));
        double d = this.client.options.getChatOpacity().getValue() * (double)0.9f + (double)0.1f;
        double e = this.client.options.getTextBackgroundOpacity().getValue();
        double g = this.client.options.getChatLineSpacing().getValue();
        int o = this.getLineHeight();
        int p = (int)Math.round(-8.0 * (g + 1.0) + 4.0 * g);
        int q = 0;
        for (int r = 0; r + this.scrolledLines < this.visibleMessages.size() && r < i; ++r) {
            int s = r + this.scrolledLines;
            ChatHudLine.Visible visible = this.visibleMessages.get(s);
            t = currentTick - visible.addedTime();
            if (t >= maxVisibleTime && !focused) continue;
            double alpha = focused ? 1.0 : getMessageOpacityMultiplier(t);
            u = (int)(255.0 * alpha * d);
            v = (int)(255.0 * alpha * e);
            ++q;
            if (u <= 3) continue;
            w = 0;
            x = m - r * o;
            int y = x + p;
            context.fill(-4+xShift, x - o, 0 + k + 4 + 4+xShift, x, v << 24);
            MessageIndicator messageIndicator = visible.indicator();
            if (messageIndicator != null) {
                int z = messageIndicator.indicatorColor() | u << 24;
                context.fill(-4+xShift, x - o, -2+xShift, x, z);
                if (s == n && messageIndicator.icon() != null) {
                    int aa = this.getIndicatorX(visible);
                    int ab = y + this.client.textRenderer.fontHeight;
                    this.drawIndicatorIcon(context, aa, ab, messageIndicator.icon());
                }
            }
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 50.0f);
            context.drawTextWithShadow(this.client.textRenderer, visible.content(), xShift, y, 0xFFFFFF + (u << 24));
            context.getMatrices().pop();
        }

        long ac = this.client.getMessageHandler().getUnprocessedMessageCount();
        if (ac > 0L) {
            int ad = (int)(128.0 * d);
            t = (int)(255.0 * e);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, m, 0.0f);
            context.fill(-2+xShift, 0, k + 4+xShift, 9, t << 24);
            context.getMatrices().translate(0.0f, 0.0f, 50.0f);
            context.drawTextWithShadow(this.client.textRenderer, Text.translatable("chat.queue", ac), 0, 1, 0xFFFFFF + (ad << 24));
            context.getMatrices().pop();
        }
        if (focused) {
            int ad = this.getLineHeight();
            t = j * ad;
            int ae = q * ad;
            int af = this.scrolledLines * ae / j - m;
            u = ae * ae / t;
            if (t != ae) {
                v = af > 0 ? 170 : 96;
                w = this.hasUnreadNewMessages ? 0xCC3333 : 0x3333AA;
                x = k + 4;
                context.fill(x+xShift, -af, x + 2+xShift, -af - u, 100, w + (v << 24));
                context.fill(x + 2+xShift, -af, x + 1+xShift, -af - u, 100, 0xCCCCCC + (v << 24));
            }
        }
        context.getMatrices().pop();
        profiler.pop();
    }

    private void drawIndicatorIcon(DrawContext context, int x, int y, MessageIndicator.Icon icon) {
        int i = y - icon.height - 1;
        icon.draw(context, x, i);
    }

    private int getIndicatorX(ChatHudLine.Visible line) {
        return this.client.textRenderer.getWidth(line.content()) + 4;
    }

    private boolean isChatHidden() {
        return this.client.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN;
    }

    private static double getMessageOpacityMultiplier(int age) {
        double d = (double) age / maxVisibleTime;
        d = 1.0 - d;
        d *= 10.0;
        d = MathHelper.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

    public void clear(boolean clearHistory) {
        this.client.getMessageHandler().processAll();
        this.removalQueue.clear();
        this.visibleMessages.clear();
        this.messages.clear();
        if (clearHistory) {
            this.messageHistory.clear();
            this.messageHistory.addAll(this.client.getCommandHistoryManager().getHistory());
        }
    }

    public void addMessage(Text message) {
        this.addMessage(message, (MessageSignatureData) null, this.client.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system());
    }

    public void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator) {
        ChatHudLine chatHudLine = new ChatHudLine(Mod.tick, message, signatureData, indicator);
        this.logChatMessage(chatHudLine);
        this.addVisibleMessage(chatHudLine);
        this.addMessage(chatHudLine);
    }

    private void logChatMessage(ChatHudLine message) {
        String string = message.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String string2 = Nullables.map(message.indicator(), MessageIndicator::loggedName);
        if (string2 != null) {
            LOGGER.info("[{}] [MCHAT] {}", (Object)string2, (Object)string);
        } else {
            LOGGER.info("[MCHAT] {}", (Object)string);
        }
    }

    private void addVisibleMessage(ChatHudLine message) {
        int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
        MessageIndicator.Icon icon = message.getIcon();
        if (icon != null) {
            i -= icon.width + 4 + 2;
        }
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message.content(), i, this.client.textRenderer);
        boolean bl = this.isChatFocused();
        for (int j = 0; j < list.size(); ++j) {
            OrderedText orderedText = list.get(j);
            if (bl && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1);
            }
            boolean bl2 = j == list.size() - 1;
            this.visibleMessages.add(0, new ChatHudLine.Visible(message.creationTick(), orderedText, message.indicator(), bl2));
        }
        /*
        while (this.visibleMessages.size() > MAX_MESSAGES) {
            this.visibleMessages.remove(this.visibleMessages.size() - 1);
        }
         */
    }

    private void addMessage(ChatHudLine message) {
        this.messages.addFirst(message);
        /*
        while (this.messages.size() > MAX_MESSAGES) {
            this.messages.remove(this.messages.size() - 1);
        }
         */
    }

    private void tickRemovalQueue() {
        int i = this.client.inGameHud.getTicks();
        this.removalQueue.removeIf(message -> {
            if (i >= message.deletableAfter()) {
                return this.queueForRemoval(message.signature()) == null;
            }
            return false;
        });
    }

    public void removeMessage(MessageSignatureData signature) {
        RemovalQueuedMessage removalQueuedMessage = this.queueForRemoval(signature);
        if (removalQueuedMessage != null) {
            this.removalQueue.add(removalQueuedMessage);
        }
    }

    @Nullable
    private RemovalQueuedMessage queueForRemoval(MessageSignatureData signature) {
        int i = this.client.inGameHud.getTicks();
        ListIterator<ChatHudLine> listIterator = this.messages.listIterator();
        while (listIterator.hasNext()) {
            ChatHudLine chatHudLine = listIterator.next();
            if (!signature.equals(chatHudLine.signature())) continue;
            int j = chatHudLine.creationTick() + 60;
            if (i >= j) {
                listIterator.set(this.createRemovalMarker(chatHudLine));
                this.refresh();
                return null;
            }
            return new RemovalQueuedMessage(signature, j);
        }
        return null;
    }

    private ChatHudLine createRemovalMarker(ChatHudLine original) {
        return new ChatHudLine(original.creationTick(), DELETED_MARKER_TEXT, null, MessageIndicator.system());
    }

    public void reset() {
        this.resetScroll();
        this.refresh();
    }

    private void refresh() {
        this.visibleMessages.clear();
        for (ChatHudLine chatHudLine : Lists.reverse(this.messages)) {
            this.addVisibleMessage(chatHudLine);
        }
    }

    public ArrayListDeque<String> getMessageHistory() {
        return this.messageHistory;
    }

    public void addToMessageHistory(String message) {
        if (!message.equals(this.messageHistory.peekLast())) {
            if (this.messageHistory.size() >= MAX_MESSAGES) {
                this.messageHistory.removeFirst();
            }
            this.messageHistory.addLast(message);
        }
        if (message.startsWith("/")) {
            this.client.getCommandHistoryManager().add(message);
        }
    }

    public void resetScroll() {
        this.scrolledLines = 0;
        this.hasUnreadNewMessages = false;
    }

    public void scroll(int scroll) {
        this.scrolledLines += scroll;
        int i = this.visibleMessages.size();
        if (this.scrolledLines > i - this.getVisibleLineCount()) {
            this.scrolledLines = i - this.getVisibleLineCount();
        }
        if (this.scrolledLines <= 0) {
            this.scrolledLines = 0;
            this.hasUnreadNewMessages = false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        MessageHandler messageHandler = this.client.getMessageHandler();

        if (messageHandler.getUnprocessedMessageCount() == 0L) {
            return false;
        }
        double d = (mouseX) - 2.0;
        double e = (double)this.client.getWindow().getScaledHeight() - mouseY - 40.0;
        if (d <= this.client.getWindow().getScaledWidth() && d >= 0 && e < 0.0 && e > (double)MathHelper.floor(-9.0 * this.getChatScale())) {
            messageHandler.process();
            return true;
        }
        return false;
    }

    @Nullable
    public Style getTextStyleAt(double x, double y) {
        double e;
        double d = this.toChatLineX(x);
        int i = this.getMessageLineIndex(d, e = this.toChatLineY(y));
        if (i >= 0 && i < this.visibleMessages.size()) {
            ChatHudLine.Visible visible = this.visibleMessages.get(i);
            return this.client.textRenderer.getTextHandler().getStyleAt(visible.content(), MathHelper.floor(d));
        }
        return null;
    }

    @Nullable
    public MessageIndicator getIndicatorAt(double mouseX, double mouseY) {
        ChatHudLine.Visible visible;
        MessageIndicator messageIndicator;
        double e;
        double d = this.toChatLineX(mouseX);
        int i = this.getMessageIndex(d, e = this.toChatLineY(mouseY));
        if (i >= 0 && i < this.visibleMessages.size() && (messageIndicator = (visible = this.visibleMessages.get(i)).indicator()) != null && this.isXInsideIndicatorIcon(d, visible, messageIndicator)) {
            return messageIndicator;
        }
        return null;
    }

    private boolean isXInsideIndicatorIcon(double x, ChatHudLine.Visible line, MessageIndicator indicator) {
        if (x < 0.0) {
            return true;
        }
        MessageIndicator.Icon icon = indicator.icon();
        if (icon != null) {
            int i = this.getIndicatorX(line);
            int j = i + icon.width;
            return x >= (double)i && x <= (double)j;
        }
        return false;
    }

    private double toChatLineX(double x) {
        return (x - xShift) / this.getChatScale() - 4.0;
    }

    private double toChatLineY(double y) {
        double d = (double)this.client.getWindow().getScaledHeight() - y - 40.0;
        return d / (this.getChatScale() * (double)this.getLineHeight());
    }

    private int getMessageIndex(double chatLineX, double chatLineY) {
        int i = this.getMessageLineIndex(chatLineX, chatLineY);
        if (i == -1) {
            return -1;
        }
        while (i >= 0) {
            if (this.visibleMessages.get(i).endOfEntry()) {
                return i;
            }
            --i;
        }
        return i;
    }

    private int getMessageLineIndex(double chatLineX, double chatLineY) {
        int j;
        if (!this.isChatFocused() || this.isChatHidden()) {
            return -1;
        }
        if (chatLineX < -4.0 || chatLineX > (double)MathHelper.floor((double)this.getWidth() / this.getChatScale())) {
            return -1;
        }
        int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
        if (chatLineY >= 0.0 && chatLineY < (double)i && (j = MathHelper.floor(chatLineY + (double)this.scrolledLines)) >= 0 && j < this.visibleMessages.size()) {
            return j;
        }
        return -1;
    }

    public boolean isChatFocused() {
        return this.client.currentScreen instanceof ChatScreen;
    }

    public int getWidth() {
        return getWidth(this.client.options.getChatWidth().getValue());
    }

    public int getHeight() {
        return getHeight(this.isChatFocused() ? this.client.options.getChatHeightFocused().getValue() : this.client.options.getChatHeightUnfocused().getValue());
    }

    public double getChatScale() {
        return this.client.options.getChatScale().getValue();
    }

    public static int getWidth(double widthOption) {
        int i = 320;
        int j = 40;
        return MathHelper.floor(widthOption * 280.0 + 40.0);
    }

    public static int getHeight(double heightOption) {
        int i = 180;
        int j = 20;
        return MathHelper.floor(heightOption * 160.0 + 20.0);
    }

    public static double getDefaultUnfocusedHeight() {
        int i = 180;
        int j = 20;
        return 70.0 / (double)(getHeight(1.0) - 20);
    }

    public int getVisibleLineCount() {
        return (int) (((double) this.getHeight() / this.getLineHeight()) * 2.5);
    }

    private int getLineHeight() {
        return (int)((double)this.client.textRenderer.fontHeight * (this.client.options.getChatLineSpacing().getValue() + 1.0));
    }

    public ChatState toChatState() {
        return new ChatState(List.copyOf(this.messages), List.copyOf(this.messageHistory), List.copyOf(this.removalQueue));
    }

    public void restoreChatState(ChatState state) {
        this.messageHistory.clear();
        this.messageHistory.addAll(state.messageHistory);
        this.removalQueue.clear();
        this.removalQueue.addAll(state.removalQueue);
        this.messages.clear();
        this.messages.addAll(state.messages);
        this.refresh();
    }

    @Environment(value=EnvType.CLIENT)
    record RemovalQueuedMessage(MessageSignatureData signature, int deletableAfter) {
    }

    @Environment(value=EnvType.CLIENT)
    public static class ChatState {
        final List<ChatHudLine> messages;
        final List<String> messageHistory;
        final List<RemovalQueuedMessage> removalQueue;

        public ChatState(List<ChatHudLine> messages, List<String> messageHistory, List<RemovalQueuedMessage> removalQueue) {
            this.messages = messages;
            this.messageHistory = messageHistory;
            this.removalQueue = removalQueue;
        }
    }
}