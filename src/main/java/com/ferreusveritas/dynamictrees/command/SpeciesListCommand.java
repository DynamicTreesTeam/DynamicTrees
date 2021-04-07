package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public final class SpeciesListCommand extends SimpleSubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SPECIES_LIST;
    }

    @Override
    protected void execute(CommandContext<CommandSource> context) {
        TreeRegistry.getSpeciesDirectory().forEach(r -> context.getSource().sendSuccess(new StringTextComponent(r.toString()), false));
    }

}
