package mia.miamod.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.miamod.features.parameters.ParameterDataField;
import mia.miamod.features.parameters.ParameterIdentifier;

public class IntegerSliderDataField extends IntegerDataField {
    private final int minValue, maxValue;
    public IntegerSliderDataField(String name, ParameterIdentifier identifier, Integer defaultValue, Integer minValue, Integer maxValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void setValue(Integer value) { this.dataField = Math.clamp(value, minValue, maxValue);}

    @Override
    public Integer getValue() { return Math.clamp(dataField, minValue, maxValue); }

    public Integer getMinValue() { return minValue; }
    public Integer getMaxValue() { return maxValue; }
}
