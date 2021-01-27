package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CommandUtils;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public final class CreateTransformPotionCommand extends SubCommand {

    public CreateTransformPotionCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;

        final Set<ResourceLocation> speciesResLocs = new HashSet<>(Species.REGISTRY.getKeys());

        // Only suggest common species (they usually share their name with the tree family).
        speciesResLocs.removeIf(speciesResLoc -> {
            final Species species = TreeRegistry.findSpecies(speciesResLoc);
            return !species.getFamily().getCommonSpecies().equals(species);
        });

        this.extraArguments = Commands.argument(CommandConstants.TREE_FAMILY_ARGUMENT, ResourceLocationArgument.resourceLocation())
                .suggests((context, builder) -> ISuggestionProvider.suggestIterable(speciesResLocs, builder)).executes(this::execute);
    }

    @Override
    protected String getName() {
        return CommandConstants.CREATE_TRANSFORM_POTION;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final BlockPos pos = this.getPositionArg(context);
        final Species species = TreeRegistry.findSpecies(ResourceLocationArgument.getResourceLocation(context, CommandConstants.TREE_FAMILY_ARGUMENT));

        // Ensure species given exists.
        if (!species.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getResourceLocation(context, CommandConstants.TREE_FAMILY_ARGUMENT)));
            return 0;
        }

        final TreeFamily family = species.getFamily();

        final DendroPotion dendroPotion = DTRegistries.dendroPotion;
        final ItemStack dendroPotionStack = new ItemStack(dendroPotion);

        dendroPotion.applyIndexTag(dendroPotionStack, DendroPotion.DendroPotionType.TRANSFORM.getIndex()); // Make it a transform potion.
        dendroPotion.setTargetTree(dendroPotionStack, family); // Tell it to set the target tree to the selected family.

        CommandUtils.spawnItemStack(context.getSource().getWorld(), pos, dendroPotionStack); // Spawn potion in the world.

        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
