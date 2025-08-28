package mia.miamod.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;

public class IntegerDataField extends ParameterDataField<Integer> {
    public IntegerDataField(String name, ParameterIdentifier identifier, Integer defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public Integer deserialize(JsonElement jsonObject) {
        return jsonObject.getAsInt();
    }
}
