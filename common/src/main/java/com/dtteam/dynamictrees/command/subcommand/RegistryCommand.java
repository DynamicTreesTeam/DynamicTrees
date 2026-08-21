package com.dtteam.dynamictrees.command.subcommand;

import com.dtteam.dynamictrees.api.registry.Registries;
import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

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

    protected String getName() {
        return "registry";
    }

    protected int getPermissionLevel() {
        return 0;
    }

    protected List<ArgumentBuilder<CommandSourceStack, ?>> registerArguments() {
        return this.subCommands.stream().map(SubCommand::register)
                .collect(Collectors.toList());
    }

    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return stringArgument("null");
    }

}
