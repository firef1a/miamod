package mia.miamod.render.util.elements.impl;

import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.render.util.ARGB;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntegerInputField extends StringInputField{
    public IntegerInputField(Matrix4f matrix4f, IntegerDataField stringDataField, float x, float y, float z, ARGB argb, boolean shadow) {
        super(matrix4f, stringDataField, x, y, z, argb, shadow);
    }

    @Override
    protected boolean characterFilter(char chr) {
        new ArrayList<>(Collections.singleton("0123456789".toCharArray()));
        return List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9').contains(chr);
    }

    @Override
    protected void setString(String value) {
        long v = 0L;
        if (!(value.isEmpty() || value.isBlank())) {
            try{
                v = Long.parseLong(value);
            } catch (Exception e) {
                v = 0L;
            }

        }

        if (stringDataField != null) {
            int intVal = 0;
            try {
                intVal = v > (long) Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
            } catch (Exception e){
                intVal = 0;
            }

            ((IntegerDataField) stringDataField).setValue(intVal);
        }
    }
}
