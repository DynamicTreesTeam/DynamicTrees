package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.ArrayList;
import java.util.List;

public final class DTCommand {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public DTCommand() {
        this.subCommands.add(new RepopCommand());
        this.subCommands.add(new GetTreeCommand());
        this.subCommands.add(new GrowPulseCommand());
        this.subCommands.add(new KillTreeCommand());
        this.subCommands.add(new SpeciesListCommand());
        this.subCommands.add(new SoilLifeCommand());
        this.subCommands.add(new SetTreeCommand());
    }

    public void registerDTCommand(CommandDispatcher<CommandSource> dispatcher) {
        // Create DT command builder.
        LiteralArgumentBuilder<CommandSource> dtCommandBuilder = LiteralArgumentBuilder.<CommandSource>literal(CommandConstants.COMMAND)
                .requires(commandSource -> commandSource.hasPermissionLevel(2));

        // Add DT sub-commands.
        for (SubCommand subCommand : this.subCommands) dtCommandBuilder = dtCommandBuilder.then(subCommand.register());

        // Register DT command.
        LiteralCommandNode<CommandSource> dtCommand = dispatcher.register(dtCommandBuilder);

        // Create 'dynamictrees' alias.
        dispatcher.register(Commands.literal("dynamictrees")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .redirect(dtCommand)
        );
    }

}
