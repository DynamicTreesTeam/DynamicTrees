package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.tileentity.TileEntityDendroCoil;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockDendroCoil extends BlockContainer implements IPeripheralProvider {

	public BlockDendroCoil() {
		this("dendrocoil");
	}
	
	public BlockDendroCoil(String name) {
		super(Material.IRON);
		setUnlocalizedName(name);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		ComputerCraftAPI.registerPeripheralProvider(this);
		setRegistryName(name);
		GameRegistry.registerTileEntity(TileEntityDendroCoil.class, name);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		if (world.isBlockIndirectlyGettingPowered(pos) != 0) {
			growPulse(world, pos);
		}
	}

	public String getCode(World world, BlockPos pos) {
		pos = pos.up();
		if(TreeHelper.isRootyDirt(world, pos)) {
			return new JoCode().buildFromTree(world, pos).toString();
		}
		
		return "";
	}

	public void setCode(World world, BlockPos pos, String treeName, String JoCode) {
		JoCode jo = new JoCode(JoCode);
		DynamicTree tree = TreeRegistry.findTree(treeName);
		if(tree != null) {
			jo.growTree(world, tree, pos.up(), EnumFacing.NORTH, 8);
		} else {
			Logger.getLogger(DynamicTrees.MODID).log(Level.WARNING, "Tree: " + treeName + " not found.");
		}
	}

	public void createStaff(World world, BlockPos pos, String treeName, String JoCode, String rgb, boolean readOnly) {
		ItemStack stack = new ItemStack(DynamicTrees.treeStaff, 1, 0);
		DynamicTree tree = TreeRegistry.findTree(treeName);
		DynamicTrees.treeStaff.setTree(stack, tree).setCode(stack, JoCode).setColor(stack, rgb).setReadOnly(stack, readOnly);
		EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack);
		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;
		world.spawnEntityInWorld(entityItem);
	}

	public String getTree(World world, BlockPos pos) {
		ITreePart part = TreeHelper.getSafeTreePart(world, pos.up());
		if(part.isRootNode()) {
			DynamicTree tree = part.getTree(world, pos.up());
			if(tree != null) {
				return tree.getFullName();
			}
		}
		
		return "";
	}

	public void plantTree(World world, BlockPos pos, String treeName) {
		DynamicTree tree = TreeRegistry.findTree(treeName);
		if(tree != null) {
			tree.getSeed().plantSapling(world, pos.up(2), tree.getSeedStack());
		}
	}

	public void growPulse(World world, BlockPos pos) {
		ITreePart part = TreeHelper.getSafeTreePart(world, pos.up());
		if(part.isRootNode()) {
			TreeHelper.growPulse(world, pos.up());
		}
	}
	
	public void killTree(World world, BlockPos pos) {
		ITreePart part = TreeHelper.getSafeTreePart(world, pos.up());
		if(part.isRootNode()) {
			((BlockRootyDirt)part).destroyTree(world, pos.up());
		}
	}

	public int getSoilLife(World world, BlockPos pos) {
		ITreePart part = TreeHelper.getSafeTreePart(world, pos.up());
		if(part.isRootNode()) {
			return ((BlockRootyDirt)part).getSoilLife(world, pos.up());
		}
		return 0;
	}

	public void setSoilLife(World world, BlockPos pos, int life) {
		ITreePart part = TreeHelper.getSafeTreePart(world, pos.up());
		if(part.isRootNode()) {
			((BlockRootyDirt)part).setSoilLife(world, pos.up(), life);
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDendroCoil();
	}

	@Override
	public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing facing) {
		TileEntity te = world.getTileEntity(pos);
		
		if(te instanceof TileEntityDendroCoil) {
			return (TileEntityDendroCoil)te;
		}

		return null;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		tooltip.add("§6ComputerCraft Peripheral");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

}
