package mia.miamod.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;

public class StringDataField extends ParameterDataField<String> {
    public StringDataField(String name, ParameterIdentifier identifier, String defaultValue) {
        super(name, identifier, defaultValue);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public String deserialize(JsonElement jsonObject) {
        return jsonObject.getAsString();
    }
}
