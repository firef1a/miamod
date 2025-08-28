package mia.miamod.features.parameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.config.ConfigStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class ParameterDataField<T> {
    protected final String name;
    protected final ParameterIdentifier identifier;
    protected final Class<T> classType;
    protected T dataField;
    protected boolean isConfig;

    @SuppressWarnings("unchecked")
    public ParameterDataField(String name, ParameterIdentifier identifier, T defaultValue, boolean isConfig) {
        this.name = name;
        this.identifier = identifier;
        this.classType = (Class<T>) defaultValue.getClass();
        this.dataField = ConfigStore.getParameter(this, defaultValue);
        this.isConfig = isConfig;
        identifier.feature().addParameter(this);
    }

    public void setValue(T value) { this.dataField = value;}
    public T getValue() { return dataField; }

    public abstract void serialize(JsonObject jsonObject);
    public abstract T deserialize(JsonElement jsonObject);


    public String getName() { return this.name; }
    public Class<T> getDataClassType() { return this.classType; }
    public ParameterIdentifier getIdentifier() { return this.identifier; }
    public boolean isConfig() { return isConfig; }
}