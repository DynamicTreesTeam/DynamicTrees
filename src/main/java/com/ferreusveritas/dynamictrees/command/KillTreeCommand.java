package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class KillTreeCommand extends SubCommand {

    public KillTreeCommand() {
        this.takesCoordinates = true;
        this.defaultToExecute = false;
    }

    @Override
    protected String getName() {
        return CommandConstants.KILL_TREE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final World world = context.getSource().getWorld();
        final BlockPos pos = Vec3Argument.getLocation(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
        final BlockPos rootPos = TreeHelper.findRootNode(world.getBlockState(pos), world, pos);

        if (rootPos == BlockPos.ZERO) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        TreeHelper.getRooty(world.getBlockState(rootPos)).destroyTree(world, rootPos);

        return 0;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
