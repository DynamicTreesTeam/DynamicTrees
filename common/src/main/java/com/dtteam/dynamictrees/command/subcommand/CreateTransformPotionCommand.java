//package com.dtteam.dynamictrees.command;
//
//import com.dtteam.dynamictrees.init.DTRegistries;
//import com.dtteam.dynamictrees.item.DendroPotion;
//import com.dtteam.dynamictrees.tree.species.Species;
//import com.dtteam.dynamictrees.command.CommandHelper;
//import com.dtteam.dynamictrees.util.helper.ItemUtils;
//import com.mojang.brigadier.builder.ArgumentBuilder;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import net.minecraft.ChatFormatting;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.core.BlockPos;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.ItemStack;
//
///**
// * @author Harley O'Connor
// */
//public final class CreateTransformPotionCommand extends SubCommand {
//
////    protected String getName() {
//        return CommandConstants.CREATE_TRANSFORM_POTION;
//    }
//
////    protected int getPermissionLevel() {
//        return 2;
//    }
//
////    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
//        return blockPosArgument().then(transformableSpeciesArgument().executes(context -> this.spawnTransformPotion(context.getSource(),
//                blockPosArgument(context), speciesArgument(context))));
//    }
//
//    private int spawnTransformPotion(final CommandSourceStack source, final BlockPos pos, final Species species) throws CommandSyntaxException {
//        if (!species.isTransformable()) {
//            throw SPECIES_NOT_TRANSFORMABLE.create(species.getTextComponent());
//        }
//
//        final DendroPotion dendroPotion = DTRegistries.DENDRO_POTION.get();
//        final ItemStack dendroPotionStack = new ItemStack(dendroPotion);
//
//        dendroPotion.applyIndexTag(dendroPotionStack, DendroPotion.DendroPotionType.TRANSFORM.getIndex()); // Make it a transform potion.
//        dendroPotion.setTargetSpecies(dendroPotionStack, species); // Tell it to set the target tree to the selected family.
//
//        ItemUtils.spawnItemStack(source.getLevel(), pos, dendroPotionStack, true); // Spawn potion in the world.
//        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.create_transform_potion",
//                species.getTextComponent(), CommandHelper.posComponent(pos, ChatFormatting.AQUA)));
//
//        return 1;
//    }
//
//}
