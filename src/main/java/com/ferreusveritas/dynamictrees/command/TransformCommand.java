package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.WailaOther;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeTransform;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.ResourceLocation;
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

        this.extraArguments = Commands.argument(CommandConstants.SPECIES_ARGUMENT, ResourceLocationArgument.resourceLocation())
                .suggests((context, builder) -> ISuggestionProvider.suggestIterable(Species.REGISTRY.getKeys(), builder)).executes(this::execute);
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
        final World world = context.getSource().getWorld();
        final BlockPos pos = Vec3Argument.getLocation(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
        final Species species = TreeRegistry.findSpecies(ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT));
        final Species originalSpecies = TreeHelper.getBestGuessSpecies(world, pos);

        if (!species.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT)));
            return 0;
        }

        if (!originalSpecies.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        if (species == originalSpecies) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.transform.speciesequal"));
            return 0;
        }

        final BlockPos rootPos = TreeHelper.findRootNode(world, pos);

        if (rootPos.equals(BlockPos.ZERO)) {
            return 0; // This should never happen, but just in case we return.
        }

        final BlockState rootyState = world.getBlockState(rootPos);
        final RootyBlock rootyBlock = ((RootyBlock) rootyState.getBlock());

        // Transform tree.
        rootyBlock.startAnalysis(world, rootPos, new MapSignal(new NodeTransform(originalSpecies, species)));

        if (rootyBlock.getSpecies(rootyState, world, rootPos) != species) {
            // Place new rooty dirt block if transforming to species that requires tile entity.
            species.placeRootyDirtBlock(world, rootPos, rootyBlock.getSoilLife(rootyState, world, rootPos));
        }

        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.transform.success",
                originalSpecies.getLocalizedName(), species.getLocalizedName()));
        WailaOther.invalidateWailaPosition();

        return 1;
    }

}
