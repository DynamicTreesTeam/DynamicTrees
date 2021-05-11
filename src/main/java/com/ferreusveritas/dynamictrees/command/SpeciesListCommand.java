package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

public final class SpeciesListCommand extends SimpleSubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SPECIES_LIST;
    }

    @Override
    protected void execute(CommandContext<CommandSource> context) {
        Species.REGISTRY.forEach(species ->
                sendSuccess(context.getSource(), species.getTextComponent()));
    }

}
