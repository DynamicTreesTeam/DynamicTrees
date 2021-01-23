package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public final class SpeciesListCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SPECIES_LIST;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        TreeRegistry.getSpeciesDirectory().forEach(r -> this.sendMessage(context, (new StringTextComponent(r.toString()))));
        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

}
