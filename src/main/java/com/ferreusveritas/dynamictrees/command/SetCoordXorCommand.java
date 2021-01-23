package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public final class SetCoordXorCommand extends SubCommand {

    public SetCoordXorCommand() {
        this.defaultToExecute = false;

        this.extraArguments = Commands.argument(CommandConstants.XOR_ARGUMENT, IntegerArgumentType.integer()).executes(this::execute);
    }

    @Override
    protected String getName() {
        return CommandConstants.SET_COORD_XOR;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final int coordXor = IntegerArgumentType.getInteger(context, CommandConstants.XOR_ARGUMENT);
        CoordUtils.coordXor = coordXor;

        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.xor.set", coordXor));
        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
