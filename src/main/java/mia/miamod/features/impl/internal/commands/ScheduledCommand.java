package mia.miamod.features.impl.internal.commands;

public record ScheduledCommand(String command, long delay) {
    public ScheduledCommand(String command) {
        this(command, 0L);
    }
    public long getDelay() {
        return (50L * (command.length()) + 25L) + 100L + this.delay;
    }
}
