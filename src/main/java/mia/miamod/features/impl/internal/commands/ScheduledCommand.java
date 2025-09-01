package mia.miamod.features.impl.internal.commands;

import java.util.ArrayList;

public record ScheduledCommand(String command, long delay, ArrayList<ChatConsumer> commandConsumers) {
    public ScheduledCommand(String command, long delay) {
        this(command, delay, new ArrayList<>());
    }
    public ScheduledCommand(String command) {
        this(command, 0L);
    }
    public long getDelay() {
        return (50L * (command.length()) + 25L) + 100L;
    }
}
