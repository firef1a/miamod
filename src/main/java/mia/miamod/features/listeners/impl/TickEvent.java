package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;

public interface TickEvent extends AbstractEventListener {
    void tickR(int tick); // rising edge
    void tickF(int tick); // falling edge
}
