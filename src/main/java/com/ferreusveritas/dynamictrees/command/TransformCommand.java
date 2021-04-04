package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.WailaOther;
import com.ferreusveritas.dynamictrees.systems.nodemappers.TransformNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class TransformCommand extends SubCommand {

    public TransformCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;

        this.extraArguments = Commands.argument(CommandConstants.SPECIES_ARGUMENT, ResourceLocationArgument.id())
                .suggests((context, builder) -> ISuggestionProvider.suggestResource(TreeRegistry.getTransformableSpeciesLocations(), builder)).executes(this::execute);
    }

    @Override
    protected String getName() {
        return CommandConstants.TRANSFORM;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final World world = context.getSource().getLevel();
        final BlockPos pos = Vec3Argument.getCoordinates(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
        final Species toSpecies = TreeRegistry.findSpecies(ResourceLocationArgument.getId(context, CommandConstants.SPECIES_ARGUMENT));

        if (!toSpecies.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getId(context, CommandConstants.SPECIES_ARGUMENT)));
            return 0;
        }

        final BlockPos rootPos = TreeHelper.findRootNode(world, pos);

        if (rootPos.equals(BlockPos.ZERO)) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        final Species fromSpecies = TreeHelper.getExactSpecies(world, rootPos);

        if (toSpecies == fromSpecies) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.transform.speciesequal"));
            return 0;
        }

        if (!toSpecies.isTransformable() || !fromSpecies.isTransformable()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.transform.nottransformableerror", !toSpecies.isTransformable() ? toSpecies.getRegistryName() : fromSpecies.getRegistryName()));
            return 0;
        }

        final BlockState rootyState = world.getBlockState(rootPos);
        final RootyBlock rootyBlock = ((RootyBlock) rootyState.getBlock());

        // Transform tree.
        rootyBlock.startAnalysis(world, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));

        if (rootyBlock.getSpecies(rootyState, world, rootPos) != toSpecies) {
            // Place new rooty dirt block if transforming to species that requires tile entity.
            toSpecies.placeRootyDirtBlock(world, rootPos, rootyBlock.getSoilLife(rootyState, world, rootPos));
        }

        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.transform.success",
                fromSpecies.getLocalizedName(), toSpecies.getLocalizedName()));
        WailaOther.invalidateWailaPosition();

        return 1;
    }

}
