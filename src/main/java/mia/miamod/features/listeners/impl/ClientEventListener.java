package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;

public interface ClientEventListener extends AbstractEventListener {
    void clientInitialize();
    void clientShutdown();
}
