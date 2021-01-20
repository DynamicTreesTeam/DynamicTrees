package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.TrunkShellBlock.ShellMuse;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class WailaBranchHandler implements IComponentProvider { //IServerDataProvider<String>
	
	private BlockPos lastPos = BlockPos.ZERO;
	private Species lastSpecies = Species.NULLSPECIES;
	private float lastVolume = 0;

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if(WailaOther.invalid) {
			lastPos = BlockPos.ZERO;
			lastSpecies = Species.NULLSPECIES;
			lastVolume = 0;

			WailaOther.invalid = false;
		}

		CompoundNBT nbtData = accessor.getServerData();
		BlockPos pos = accessor.getPosition();
		Species species = Species.NULLSPECIES;

		//Attempt to get species from server via NBT data
		if(nbtData.contains("species")) {
			species = TreeRegistry.findSpecies(new ResourceLocation(nbtData.getString("species")));
		}

		//Attempt to get species by checking if we're still looking at the same block
		if(species == Species.NULLSPECIES && lastPos.equals(pos)) {
			species = lastSpecies;
		}

		//Attempt to get species from the world as a last resort as the operation can be rather expensive
		if(species == Species.NULLSPECIES) {
			species = getWailaSpecies(accessor.getWorld(), pos);
		}

		if(!lastPos.equals(pos)) {
			lastVolume = getTreeVolume(accessor.getWorld(), pos);
		}

		//Update the cached species and position
		lastSpecies = species;
		lastPos = pos;

		if(species != Species.NULLSPECIES) {
			tooltip.add(new StringTextComponent("Species: " + species.getRegistryName().getPath()));

			ItemStack seedStack = species.getSeedStack(1);

			RenderableTextComponent seedRender = getRenderable(seedStack);
			RenderableTextComponent logRender, stickRender;
			logRender = stickRender = getRenderable(ItemStack.EMPTY);

			if(lastVolume > 0) {
				LogsAndSticks las = species.getLogsAndSticks(lastVolume);
				if(las.logs > 0) {
					ItemStack logStack = species.getFamily().getPrimitiveLogs(las.logs);
					if (!logStack.isEmpty()){
						logRender = getRenderable(logStack);
					}
				}
				if(las.sticks > 0) {
					ItemStack stickStack = species.getFamily().getStick(las.sticks);
					if (!stickStack.isEmpty()){
						stickRender = getRenderable(stickStack);
					}
				}
			}

			RenderableTextComponent renderables = new RenderableTextComponent(seedRender, logRender, stickRender);

			tooltip.add(renderables);
		}
	}
	
	private float getTreeVolume(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		
		//Dereference proxy trunk shell block
		if(block instanceof TrunkShellBlock) {
			ShellMuse muse = ((TrunkShellBlock)block).getMuse(world, pos);
			if(muse != null) {
				state = muse.state;
				block = state.getBlock();
				pos = muse.pos;
			}
		}
		
		if(block instanceof BranchBlock) {
			BranchBlock branch = (BranchBlock) block;
			
			// Analyze only part of the tree beyond the break point and calculate it's volume, then destroy the branches
			NodeNetVolume volumeSum = new NodeNetVolume();
			branch.analyse(state, world, pos, null, new MapSignal(volumeSum));
			
			return (float)(volumeSum.getVolume() * DTConfigs.treeHarvestMultiplier.get());
		}
		
		return 0;
	}

//	@Override
//	public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, String string) {
//		compoundNBT.putString("species", string);
//	}

	private Species getWailaSpecies(World world, BlockPos pos) {
		return TreeHelper.getBestGuessSpecies(world, pos);
	}

	private static RenderableTextComponent getRenderable(ItemStack stack) {
		CompoundNBT tag = new CompoundNBT();
		if (!stack.isEmpty()) {
			tag.putString("id", stack.getItem().getRegistryName().toString());
			tag.putInt("count", stack.getCount());
			if (stack.hasTag())
				tag.putString("nbt", stack.getTag().toString());
			return new RenderableTextComponent(new ResourceLocation("item"), tag);
		} else {
			tag.putInt("width", 0);
			return new RenderableTextComponent(new ResourceLocation("spacer"), tag);
		}
	}
}
