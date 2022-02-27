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
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class TransformCommand extends SubCommand {

    private static final Dynamic2CommandExceptionType SPECIES_EQUAL = new Dynamic2CommandExceptionType((toSpecies, fromSpecies) -> new TranslationTextComponent("commands.dynamictrees.error.species_equal", darkRed(toSpecies), darkRed(fromSpecies)));

    @Override
    protected String getName() {
        return CommandConstants.TRANSFORM;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArgument() {
        return blockPosArgument().then(transformableSpeciesArgument().executes(context -> executesSuccess(() ->
                this.transformSpecies(context.getSource(), rootPosArgument(context), speciesArgument(context)))));
    }

    private void transformSpecies(final CommandSource source, final BlockPos rootPos, final Species toSpecies) throws CommandSyntaxException {
        final World world = source.getLevel();

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

        sendSuccessAndLog(source, new TranslationTextComponent("commands.dynamictrees.success.transform",
                fromSpecies.getTextComponent(), CommandHelper.posComponent(rootPos, TextFormatting.AQUA),
                toSpecies.getTextComponent()));

        WailaOther.invalidateWailaPosition();
    }
}
