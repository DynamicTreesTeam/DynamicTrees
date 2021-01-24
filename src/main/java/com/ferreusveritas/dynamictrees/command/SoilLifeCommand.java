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
        this.defaultToExecute = false;

        // Register setting soil life as an extra argument.
        this.extraArguments = Commands.argument(CommandConstants.SOIL_LIFE_ARGUMENT, IntegerArgumentType.integer(0, 15)).executes(this::execute);
    }

    @Override
    protected String getName() {
        return CommandConstants.SOIL_LIFE;
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

        BlockState state = world.getBlockState(rootPos);

        try {
            final int soilLife = IntegerArgumentType.getInteger(context, CommandConstants.SOIL_LIFE_ARGUMENT);
            TreeHelper.getRooty(state).setSoilLife(world, rootPos, soilLife);
        } catch (IllegalArgumentException e) {
            final int soilLife = TreeHelper.getRooty(state).getSoilLife(state, world, rootPos);
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.soillife.get", soilLife));
        }

        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

}
