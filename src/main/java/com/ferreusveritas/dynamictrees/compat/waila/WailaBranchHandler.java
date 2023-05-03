package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock.ShellMuse;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.tree.species.Species.LogsAndSticks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ElementHelper;

import java.util.LinkedList;
import java.util.List;

public class WailaBranchHandler implements IBlockComponentProvider {
    public static final ResourceLocation ID = new ResourceLocation(DynamicTrees.MOD_ID, "branch");

    private BlockPos lastPos = BlockPos.ZERO;
    private Species lastSpecies = Species.NULL_SPECIES;
    private NetVolumeNode.Volume lastVolume = new NetVolumeNode.Volume();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (WailaOther.invalid) {
            lastPos = BlockPos.ZERO;
            lastSpecies = Species.NULL_SPECIES;
            lastVolume = new NetVolumeNode.Volume();

            WailaOther.invalid = false;
        }

        CompoundTag nbtData = accessor.getServerData();
        BlockPos pos = accessor.getPosition();
        Species species = Species.NULL_SPECIES;

        //Attempt to get species from server via NBT data
        if (nbtData.contains("species")) {
            species = TreeRegistry.findSpecies(new ResourceLocation(nbtData.getString("species")));
        }

        //Attempt to get species by checking if we're still looking at the same block
        if (species == Species.NULL_SPECIES && lastPos.equals(pos)) {
            species = lastSpecies;
        }

        //Attempt to get species from the world as a last resort as the operation can be rather expensive
        if (species == Species.NULL_SPECIES) {
            species = getWailaSpecies(accessor.getLevel(), pos);
        }

        if (!species.useDefaultWailaBody()) {
            return;
        }

        if (!lastPos.equals(pos)) {
            lastVolume = getTreeVolume(accessor.getLevel(), pos, species);
        }

        //Update the cached species and position
        lastSpecies = species;
        lastPos = pos;

        if (species != Species.NULL_SPECIES) {
            if (species.showSpeciesOnWaila()) {
                tooltip.add(Component.translatable("tooltip.dynamictrees.species", species.getTextComponent()));
            }

            if (Minecraft.getInstance().options.advancedItemTooltips) {
                tooltip.add(Component.literal(ChatFormatting.DARK_GRAY + species.getRegistryName().toString()));
            }

            ItemStack seedStack = species.getSeedStack(1);

            List<IElement> elements = new LinkedList<>();
            elements.add(getElement(seedStack)); //adds seed;

            if (species.hasFruits()){
                for (Fruit fruit : species.getFruits()){
                    ItemStack fruitStack = fruit.getItemStack();
                    if (fruitStack.getItem() != seedStack.getItem())
                        elements.add(getElement(fruitStack));
                }
            }
            if (species.hasPods()){
                for (Pod pod : species.getPods()){
                    ItemStack podStack = pod.getItemStack();
                    if (podStack.getItem() != seedStack.getItem())
                        elements.add(getElement(podStack));
                }
            }

            if (lastVolume.getVolume() > 0) {
                LogsAndSticks las = species.getLogsAndSticks(lastVolume);
                List<ItemStack> logStacks = las.logs;
                if (!logStacks.isEmpty()) {
                    for (ItemStack logStack : logStacks) {
                        elements.add(getElement(logStack));
                    }
                }
                if (las.sticks > 0) {
                    ItemStack stickStack = species.getFamily().getStick(las.sticks);
                    if (!stickStack.isEmpty()) {
                        elements.add(getElement(stickStack));
                    }
                }
            }

            tooltip.add(elements.remove(0));
            elements.forEach(tooltip::append);
            tooltip.add(ElementHelper.INSTANCE.spacer(0, 2));
        }
    }

    private NetVolumeNode.Volume getTreeVolume(Level level, BlockPos pos, Species species) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        //Dereference proxy trunk shell block
        if (block instanceof TrunkShellBlock) {
            ShellMuse muse = ((TrunkShellBlock) block).getMuse(level, pos);
            if (muse != null) {
                state = muse.state;
                block = state.getBlock();
                pos = muse.pos;
            }
        }

        if (block instanceof BranchBlock) {
            BranchBlock branch = (BranchBlock) block;
            // Analyze only part of the tree beyond the break point and calculate it's volume
            NetVolumeNode volumeSum = new NetVolumeNode();
            branch.analyse(state, level, pos, null, new MapSignal(volumeSum));

            NetVolumeNode.Volume volume = volumeSum.getVolume();
            species.processVolume(volume);

            return volume;
        }

        return new NetVolumeNode.Volume();
    }

    private Species getWailaSpecies(Level level, BlockPos pos) {
        return TreeHelper.getBestGuessSpecies(level, pos);
    }

    private static IElement getElement(ItemStack stack) {
        if (!stack.isEmpty()) {
            return ElementHelper.INSTANCE.item(stack);
        } else {
            return ElementHelper.INSTANCE.spacer(0, 0);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}