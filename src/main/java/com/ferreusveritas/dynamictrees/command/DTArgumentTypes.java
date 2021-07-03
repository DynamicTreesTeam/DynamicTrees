package com.ferreusveritas.dynamictrees.command;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

/**
 * @author Harley O'Connor
 */
public final class DTArgumentTypes {

    private DTArgumentTypes() {}

    public static void register() {
        ArgumentTypes.register("hex_color", HexColorArgument.class, new ArgumentSerializer<>(HexColorArgument::hex));
    }

}
