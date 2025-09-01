package mia.miamod.core.items.values;

public class StringDataValue extends DataValue {
    public StringDataValue(String value) {
        super(value);
    }

    public String getValue() {
        return (String) super.getValue();
    }
}