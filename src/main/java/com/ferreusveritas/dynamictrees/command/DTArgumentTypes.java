package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Harley O'Connor
 */
public final class DTArgumentTypes {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, DynamicTrees.MOD_ID);

    public static final RegistryObject<SingletonArgumentInfo<HexColorArgument>> HEX_COLOR = ARGUMENT_TYPES.register("hex_color", () -> ArgumentTypeInfos.registerByClass(HexColorArgument.class,
            SingletonArgumentInfo.contextFree(HexColorArgument::hex)));

}