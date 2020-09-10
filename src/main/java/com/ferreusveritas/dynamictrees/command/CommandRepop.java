package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandRepop extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.REPOP;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        context.getSource().sendFeedback(new TranslationTextComponent("commands.dynamictrees.repop.run"), true);
        WorldGenRegistry.populateDataBase();
        return 1;
    }

}
