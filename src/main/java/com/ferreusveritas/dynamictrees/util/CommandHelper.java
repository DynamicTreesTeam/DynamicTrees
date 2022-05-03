package com.ferreusveritas.dynamictrees.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author Harley O'Connor
 */
public final class CommandHelper {

    public static Component posComponent(final Vec3i pos) {
        return new TranslatableComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
    }

    public static Component posComponent(final Vec3i pos, final ChatFormatting colour) {
        return posComponent(pos).copy().withStyle(style -> style.withColor(colour));
    }

    public static Component colour(final Object text, final ChatFormatting colour) {
        return text instanceof Component ? ((Component) text).copy().withStyle(style -> style.withColor(colour)) :
                new TextComponent(String.valueOf(text)).withStyle(style -> style.withColor(colour));
    }

}
