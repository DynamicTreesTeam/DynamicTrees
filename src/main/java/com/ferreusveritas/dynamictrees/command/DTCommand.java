package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public final class DTCommand {

    public static final SubCommand repopCommand = new RepopCommand();
    public static final SubCommand getTreeCommand = new GetTreeCommand();

    public static void registerDTCommand(CommandDispatcher<CommandSource> dispatcher) {
        // Create 'dt' command.
        LiteralCommandNode<CommandSource> dtCommand = dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal(CommandConstants.COMMAND)
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(repopCommand.register())
                .then(getTreeCommand.register())
        );

        // Create 'dynamictrees' alias.
        dispatcher.register(Commands.literal("dynamictrees")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .redirect(dtCommand)
        );
    }

}
