package mia.miamod.features.parameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.config.ConfigStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class ParameterDataField<T> {
    protected String name;
    protected ParameterIdentifier identifier;
    protected T dataField;

    @SuppressWarnings("unchecked")
    public ParameterDataField(String name, ParameterIdentifier identifier, T defaultValue) {
        this.identifier = identifier;
        this.dataField = (T) ConfigStore.getParameter(this, defaultValue);
        identifier.feature().addParameter(this);
    }

    public ParameterDataField() {

    }

    public void setValue(T value) { this.dataField = value;}
    public T getValue() { return dataField; }

    public abstract void serialize(JsonObject jsonObject);
    public abstract T deserialize(JsonElement jsonObject);


    public String getName() { return this.name; }
    public ParameterIdentifier getIdentifier() { return this.identifier; }
}