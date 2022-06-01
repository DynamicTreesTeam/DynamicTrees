package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import static com.ferreusveritas.dynamictrees.command.CommandConstants.RAW;

/**
 * @author Harley O'Connor
 */
public final class RegistrySubCommand<V extends RegistryEntry<V>> extends SubCommand {

    public final Registry<V> registry;

    public RegistrySubCommand(Registry<V> registry) {
        this.registry = registry;
    }

    @Override
    protected String getName() {
        return this.registry.getName().toLowerCase();
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArgument() {
        return Commands.literal("list")
                .executes(context -> executesSuccess(() -> this.listEntries(context.getSource(), false)))
                .then(booleanArgument(RAW)
                        .executes(context -> executesSuccess(() -> this.listEntries(context.getSource(), BoolArgumentType.getBool(context, RAW))))
                );
    }

    private void listEntries(final CommandSource source, final boolean raw) {
        if (raw) {
            this.registry.getAll().forEach(entry -> source.sendSuccess(new StringTextComponent(entry.getRegistryName().toString()), false));
            return;
        }

        this.registry.getAll().forEach(entry -> source.sendSuccess(new StringTextComponent("- ")
                .append(entry.getTextComponent()).withStyle(TextFormatting.GREEN), false));
    }

}
