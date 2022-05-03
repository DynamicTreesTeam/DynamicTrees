package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;

/**
 * @author Harley O'Connor
 */
public final class DTArgumentTypes {

    private DTArgumentTypes() {
    }

    public static void register() {
        ArgumentTypes.register(DynamicTrees.MOD_ID + ":hex_color", HexColorArgument.class, new EmptyArgumentSerializer<>(HexColorArgument::hex));
    }

}
