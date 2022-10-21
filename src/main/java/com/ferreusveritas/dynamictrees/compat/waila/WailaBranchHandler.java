package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock.ShellMuse;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ui.ElementHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class WailaBranchHandler implements IComponentProvider {

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
            lastVolume = getTreeVolume(accessor.getLevel(), pos);
        }

        //Update the cached species and position
        lastSpecies = species;
        lastPos = pos;

        if (species != Species.NULL_SPECIES) {
            if (species.showSpeciesOnWaila()) {
                tooltip.add(new TranslatableComponent("tooltip.dynamictrees.species", species.getTextComponent()));
            }

            if (Minecraft.getInstance().options.advancedItemTooltips) {
                tooltip.add(new TextComponent(ChatFormatting.DARK_GRAY + species.getRegistryName().toString()));
            }

            ItemStack seedStack = species.getSeedStack(1);

            List<IElement> elements = new LinkedList<>();
            elements.add(getElement(seedStack)); //adds seed;

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

    private NetVolumeNode.Volume getTreeVolume(Level level, BlockPos pos) {
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
            volume.multiplyVolume(DTConfigs.TREE_HARVEST_MULTIPLIER.get());

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
}