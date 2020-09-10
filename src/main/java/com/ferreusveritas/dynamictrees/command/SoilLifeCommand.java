package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class SoilLifeCommand extends SubCommand {

    public SoilLifeCommand() {
        this.takesCoordinates = true;

        // Register setting soil life as an extra argument.
        this.extraArguments.add(Commands.argument("life", IntegerArgumentType.integer(0, 15)).executes(context -> setSoilLife(context, context.getSource().getWorld(), Vec3Argument.getLocation(context, "location").getBlockPos(context.getSource()), IntegerArgumentType.getInteger(context, "life"))));
    }

    @Override
    protected String getName() {
        return CommandConstants.SOIL_LIFE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.nocoords", "tree"));
        return 0;
    }

    @Override
    protected int executeWithCoords (CommandContext<CommandSource> context, World worldIn, BlockPos blockPos) {
        BlockPos rootPos = TreeHelper.findRootNode(worldIn.getBlockState(blockPos), worldIn, blockPos);

        if (rootPos == BlockPos.ZERO) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        BlockState state = worldIn.getBlockState(rootPos);

        final int life = TreeHelper.getRooty(state).getSoilLife(state, worldIn, rootPos);
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.soillife.get", life));

        return 1;
    }

    private int setSoilLife (CommandContext<CommandSource> context, World worldIn, BlockPos blockPos, int soilLife) {
        BlockPos rootPos = TreeHelper.findRootNode(worldIn.getBlockState(blockPos), worldIn, blockPos);

        if (rootPos == BlockPos.ZERO) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        BlockState state = worldIn.getBlockState(rootPos);
        TreeHelper.getRooty(state).setSoilLife(worldIn, rootPos, soilLife);

        return 1;
    }

}
