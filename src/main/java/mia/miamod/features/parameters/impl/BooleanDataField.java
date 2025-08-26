package mia.miamod.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;

public class BooleanDataField extends ParameterDataField<Boolean> {
    public BooleanDataField(String name, ParameterIdentifier identifier, Boolean defaultValue) {
        super(name, identifier, defaultValue);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public Boolean deserialize(JsonElement jsonObject) {
        return jsonObject.getAsBoolean();
    }
}
