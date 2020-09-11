package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class GrowPulseCommand extends SubCommand {

    public GrowPulseCommand () {
        this.takesCoordinates = true;
        this.defaultToExecute = false;
    }

    @Override
    protected String getName() {
        return CommandConstants.GROW_PULSE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final World world = context.getSource().getWorld();
        final BlockPos pos = Vec3Argument.getLocation(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
        ITreePart part = TreeHelper.getTreePart(world.getBlockState(pos));

        if (part == TreeHelper.nullTreePart) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        // TODO: Find out why this isn't working.
        if (part.isRootNode()) TreeHelper.growPulse(world, pos);

        return 1;
    }

}
