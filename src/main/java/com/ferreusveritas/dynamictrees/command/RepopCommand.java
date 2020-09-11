package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

public final class RepopCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.REPOPULATE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.repop.run"));
        WorldGenRegistry.populateDataBase();
        return 1;
    }

}
