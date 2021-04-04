package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author Harley O'Connor
 */
public final class CreateTransformPotionCommand extends SubCommand {

    public CreateTransformPotionCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;

        this.extraArguments = Commands.argument(CommandConstants.TREE_FAMILY_ARGUMENT, ResourceLocationArgument.id())
                .suggests((context, builder) -> ISuggestionProvider.suggestResource(TreeRegistry.getTransformableSpeciesLocations(), builder)).executes(this::execute);
    }

    @Override
    protected String getName() {
        return CommandConstants.CREATE_TRANSFORM_POTION;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final BlockPos pos = this.getPositionArg(context);
        final Species species = TreeRegistry.findSpecies(ResourceLocationArgument.getId(context, CommandConstants.TREE_FAMILY_ARGUMENT));

        // Ensure species given exists.
        if (!species.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getId(context, CommandConstants.TREE_FAMILY_ARGUMENT)));
            return 0;
        }

        final DendroPotion dendroPotion = DTRegistries.DENDRO_POTION;
        final ItemStack dendroPotionStack = new ItemStack(dendroPotion);

        dendroPotion.applyIndexTag(dendroPotionStack, DendroPotion.DendroPotionType.TRANSFORM.getIndex()); // Make it a transform potion.
        dendroPotion.setTargetSpecies(dendroPotionStack, species); // Tell it to set the target tree to the selected family.

        ItemUtils.spawnItemStack(context.getSource().getLevel(), pos, dendroPotionStack, true); // Spawn potion in the world.

        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
