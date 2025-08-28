package mia.miamod.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;

public class InternalBooleanDataField extends BooleanDataField implements InternalDataField {
    public InternalBooleanDataField(String name, ParameterIdentifier identifier, Boolean defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }
}
