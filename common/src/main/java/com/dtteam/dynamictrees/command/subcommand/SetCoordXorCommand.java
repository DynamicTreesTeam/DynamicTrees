package com.dtteam.dynamictrees.command.subcommand;

import com.dtteam.dynamictrees.command.CommandConstants;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;

import java.util.Collections;

public final class SetCoordXorCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SET_COORD_XOR;
    }

    @Override
    protected PermissionCheck getPermissionLevel() {
        return Commands.LEVEL_GAMEMASTERS;
    }

    private static final String XOR = "xor";

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return intArgument(XOR).suggests(((context, builder) -> SharedSuggestionProvider.suggest(Collections.singletonList("0"), builder)))
                .executes(context -> executesSuccess(() -> this.setXor(context.getSource(), intArgument(context, XOR))));
    }

    private void setXor(final CommandSourceStack source, final int xor) {
        CoordUtils.coordXor = xor;
        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.set_xor", aqua(xor)));
    }

}
