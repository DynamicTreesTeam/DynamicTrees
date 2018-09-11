package com.ferreusveritas.dynamictrees.compat;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WailaBranchHandler implements IWailaDataProvider {
	
	private BlockPos lastPos = BlockPos.ORIGIN;
	private Species lastSpecies = Species.NULLSPECIES;
	
	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		
		NBTTagCompound nbtData = accessor.getNBTData();
		BlockPos pos = accessor.getPosition();
		Species species = Species.NULLSPECIES;
		
		//Attempt to get species from server via NBT data
		if(nbtData.hasKey("species")) {
			species = TreeRegistry.findSpecies(new ResourceLocation(nbtData.getString("species")));
		}
		
		//Attempt to get species by checking if we're still looking at the same block
		if(species == Species.NULLSPECIES && pos == lastPos) {
			species = lastSpecies;
		}
		
		//Attempt to get species from the world as a last resort as the operation can be rather expensive
		if(species == Species.NULLSPECIES) {
			species = getWailaSpecies(accessor.getWorld(), pos);			
		}
		
		//Update the cached species and position
		lastSpecies = species;
		lastPos = pos;
		
		if(species != Species.NULLSPECIES) {
			tooltip.add("Species: " + species.getRegistryName().getResourcePath());				
		}
		
		return tooltip;
	}
	
	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
		Species species = getWailaSpecies(world, pos);
		
		if(species != Species.NULLSPECIES) {
			tag.setString("species", species.getRegistryName().toString());
		}
		
		return tag;
	}
	
	private Species getWailaSpecies(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		
		if(state.getBlock() instanceof BlockBranch) {
			BlockBranch branch = (BlockBranch) state.getBlock();
			Species species = TreeHelper.getExactSpecies(state, world, pos);
			if(species == Species.NULLSPECIES) {
				species = branch.getFamily().getCommonSpecies();
			}
			return species;
		}
		
		return Species.NULLSPECIES;
	}
	
}
