package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.waila.WailaOther;
import com.ferreusveritas.dynamictrees.systems.nodemappers.TransformNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Harley O'Connor
 */
public final class TransformCommand extends SubCommand {

    private static final Dynamic2CommandExceptionType SPECIES_EQUAL = new Dynamic2CommandExceptionType((toSpecies, fromSpecies) -> Component.translatable("commands.dynamictrees.error.species_equal", darkRed(toSpecies), darkRed(fromSpecies)));

    @Override
    protected String getName() {
        return CommandConstants.TRANSFORM;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().then(transformableSpeciesArgument().executes(context -> executesSuccess(() ->
                this.transformSpecies(context.getSource(), rootPosArgument(context), speciesArgument(context)))));
    }

    private void transformSpecies(final CommandSourceStack source, final BlockPos rootPos, final Species toSpecies) throws CommandSyntaxException {
        final Level world = source.getLevel();

        final Species fromSpecies = TreeHelper.getExactSpecies(world, rootPos);

        if (toSpecies == fromSpecies) {
            throw SPECIES_EQUAL.create(toSpecies.getTextComponent(), fromSpecies.getTextComponent());
        }

        if (!toSpecies.isTransformable() || !fromSpecies.isTransformable()) {
            throw SPECIES_NOT_TRANSFORMABLE.create(!toSpecies.isTransformable() ? toSpecies.getTextComponent() : fromSpecies.getTextComponent());
        }

        final BlockState rootyState = world.getBlockState(rootPos);
        final RootyBlock rootyBlock = ((RootyBlock) rootyState.getBlock());

        // Transform tree.
        rootyBlock.startAnalysis(world, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));

        if (rootyBlock.getSpecies(rootyState, world, rootPos) != toSpecies) {
            // Place new rooty dirt block if transforming to species that requires tile entity.
            toSpecies.placeRootyDirtBlock(world, rootPos, rootyBlock.getFertility(rootyState, world, rootPos));
        }

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.transform",
                fromSpecies.getTextComponent(), CommandHelper.posComponent(rootPos, ChatFormatting.AQUA),
                toSpecies.getTextComponent()));

        WailaOther.invalidateWailaPosition();
    }
}
