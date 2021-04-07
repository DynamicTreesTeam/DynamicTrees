package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;

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
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return intArgument(XOR).suggests(((context, builder) -> ISuggestionProvider.suggest(Collections.singletonList("0"), builder)))
                .executes(context -> executesSuccess(() -> this.setXor(context.getSource(), intArgument(context, XOR))));
    }

    private void setXor(final CommandSource source, final int xor) {
        CoordUtils.coordXor = xor;
        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.set_xor", aqua(xor)), true);
    }

}
