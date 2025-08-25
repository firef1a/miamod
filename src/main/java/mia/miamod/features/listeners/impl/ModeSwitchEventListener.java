package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;
import mia.miamod.features.listeners.DFMode;

public interface ModeSwitchEventListener extends AbstractEventListener {
    void onModeSwitch(DFMode newMode, DFMode previousMode);
}
