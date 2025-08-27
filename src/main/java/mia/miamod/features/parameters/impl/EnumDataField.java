package mia.miamod.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class EnumDataField<T extends Enum> extends ParameterDataField<T> {
    public EnumDataField(String name, ParameterIdentifier identifier, T defaultValue) {
        super(name, identifier, defaultValue);
    }

    @Override
    public void serialize(@NotNull JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(@NotNull JsonElement jsonObject) {
        try {
            return (T) Enum.valueOf(this.classType, jsonObject.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setValue(Enum value) {
        this.dataField = (T) value;
    }

    @Override
    public T getValue() {
        return this.dataField;
    }
}
