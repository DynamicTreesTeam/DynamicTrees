package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class GetTreeCommand extends SubCommand {

    public GetTreeCommand () {
        this.takesCoordinates = true;
        this.defaultToExecute = false;
    }

    @Override
    protected String getName () {
        return CommandConstants.GET_TREE;
    }

    @Override
    protected int execute (CommandContext<CommandSource> context) {
        final World world = context.getSource().getWorld();
        final BlockPos pos = Vec3Argument.getLocation(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
        Species species = TreeHelper.getBestGuessSpecies(world, pos);

        if (species == Species.NULLSPECIES) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        String code = TreeHelper.getJoCode(world, pos).map(JoCode::toString).orElse("?");

        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.success", species.toString(), code));

        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

}
