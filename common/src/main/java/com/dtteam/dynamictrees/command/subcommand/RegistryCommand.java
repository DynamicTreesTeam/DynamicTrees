package com.dtteam.dynamictrees.command.subcommand;

import com.dtteam.dynamictrees.api.registry.Registries;
import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionCheck;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
public final class RegistryCommand extends SubCommand {

    private final List<RegistrySubCommand<?>> subCommands = Lists.newArrayList();

    public RegistryCommand() {
        Registries.REGISTRIES.forEach(registry -> subCommands.add(new RegistrySubCommand<>(registry)));
    }

    @Override
    protected String getName() {
        return "registry";
    }

    @Override
    protected PermissionCheck getPermissionLevel() {
        return Commands.LEVEL_ALL;
    }

    @Override
    protected List<ArgumentBuilder<CommandSourceStack, ?>> registerArguments() {
        return this.subCommands.stream().map(SubCommand::register)
                .collect(Collectors.toList());
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return stringArgument("null");
    }

}
