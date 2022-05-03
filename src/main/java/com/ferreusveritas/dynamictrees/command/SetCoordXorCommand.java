package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;

public final class SetCoordXorCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SET_COORD_XOR;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    private static final String XOR = "xor";

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return intArgument(XOR).suggests(((context, builder) -> SharedSuggestionProvider.suggest(Collections.singletonList("0"), builder)))
                .executes(context -> executesSuccess(() -> this.setXor(context.getSource(), intArgument(context, XOR))));
    }

    private void setXor(final CommandSourceStack source, final int xor) {
        CoordUtils.coordXor = xor;
        sendSuccessAndLog(source, new TranslatableComponent("commands.dynamictrees.success.set_xor", aqua(xor)));
    }

}
