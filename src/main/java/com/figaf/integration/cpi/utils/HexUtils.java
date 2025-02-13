package com.figaf.integration.cpi.utils;

public class HexUtils {

    public static String stringToHex(String input) {
        StringBuilder hexBuilder = new StringBuilder(input.length() * 2);
        for (char ch : input.toCharArray()) {
            hexBuilder.append(String.format("%02X", (int) ch));
        }
        return hexBuilder.toString();
    }
}