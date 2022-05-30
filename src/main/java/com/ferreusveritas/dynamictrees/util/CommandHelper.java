package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author Harley O'Connor
 */
public final class CommandHelper {

    public static ITextComponent posComponent(final Vector3i pos) {
        return new TranslationTextComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
    }

    public static ITextComponent posComponent(final Vector3i pos, final TextFormatting colour) {
        return posComponent(pos).copy().withStyle(style -> style.withColor(colour));
    }

    public static ITextComponent colour(final Object text, final TextFormatting colour) {
        return text instanceof ITextComponent ? ((ITextComponent) text).copy().withStyle(style -> style.withColor(colour)) :
                new StringTextComponent(String.valueOf(text)).withStyle(style -> style.withColor(colour));
    }

}
