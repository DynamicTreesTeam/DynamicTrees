package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.FastColor;

public class ColorUtil {
    public static int decodeARGB32(String rgbString) throws NumberFormatException {
        int packedColor = Integer.decode(rgbString);

        return FastColor.ARGB32.color(0xFF, FastColor.ARGB32.red(packedColor), FastColor.ARGB32.green(packedColor), FastColor.ARGB32.blue(packedColor));
    }
}
