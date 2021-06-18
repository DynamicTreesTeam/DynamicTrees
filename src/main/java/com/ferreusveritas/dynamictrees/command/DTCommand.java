package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DTCommand {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public DTCommand() {
        // Registers sub-commands.
        Collections.addAll(this.subCommands, new GetTreeCommand(), new GrowPulseCommand(), new KillTreeCommand(), new RegistryCommand(),
                new FertilityCommand(), new SetTreeCommand(), new RotateJoCodeCommand(), new CreateStaffCommand(), new SetCoordXorCommand(),
                new CreateTransformPotionCommand(), new TransformCommand(), new ClearOrphanedCommand(), new PurgeTreesCommand());
    }

    public void registerDTCommand(CommandDispatcher<CommandSource> dispatcher) {
        // Create DT command builder.
        LiteralArgumentBuilder<CommandSource> dtCommandBuilder = LiteralArgumentBuilder.literal(CommandConstants.COMMAND);

        // Add sub-commands.
        for (SubCommand subCommand : this.subCommands) dtCommandBuilder = dtCommandBuilder.then(subCommand.register());

        // Register command.
        LiteralCommandNode<CommandSource> dtCommand = dispatcher.register(dtCommandBuilder);

        // Create 'dynamictrees' alias.
        dispatcher.register(Commands.literal(CommandConstants.COMMAND_ALIAS)
                .requires(commandSource -> commandSource.hasPermission(2))
                .redirect(dtCommand)
        );
    }

}
