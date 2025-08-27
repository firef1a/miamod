package mia.miamod.core;

public class MathUtils {
    public static double roundToDecimalPlaces(double number, int decimals) {
        return ((int) number * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }
}
