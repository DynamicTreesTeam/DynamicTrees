package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public final class DTCommand {

    public static final SubCommand repopCommand = new CommandRepop();

    public static void registerDTCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal(CommandConstants.COMMAND)
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(repopCommand.register())
        );
    }

}
