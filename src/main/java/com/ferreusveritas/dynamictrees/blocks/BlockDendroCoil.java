package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.tileentity.TileEntityDendroCoil;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.ILorable;
import com.ferreusveritas.dynamictrees.util.IRegisterable;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockDendroCoil extends BlockContainer implements IPeripheralProvider, IRegisterable, ILorable {

	IIcon topIcon;
	IIcon sideIcon;
	IIcon bottomIcon;

	protected String registryName;
	
	public BlockDendroCoil() {
		this("dendrocoil");
	}
	
	public BlockDendroCoil(String name) {
		super(Material.iron);
		setBlockName(name);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		ComputerCraftAPI.registerPeripheralProvider(this);
		setRegistryName(name);
		setUnlocalizedNameReg(name);
		GameRegistry.registerTileEntity(TileEntityDendroCoil.class, name);
	}

	@Override
	public void setRegistryName(String regName) {
		registryName = regName;
	}

	@Override
	public String getRegistryName() {
		return registryName;
	}
	
	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		setBlockName(unlocalName);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
			growPulse(world, new BlockPos(x, y, z));
		}
	}

	public void growPulse(World world, BlockPos pos){
		
		for(BlockPos iPos: BlockPos.getAllInBox(pos.add(new BlockPos(-8, 0, -8)), pos.add(new BlockPos(8, 32, 8)))) {
			IBlockState blockState = iPos.getBlockState(world);
			Block block = blockState.getBlock();
			if(block instanceof IAgeable) {
				((IAgeable)block).age(world, iPos, world.rand, true);
			} else
			if(block instanceof BlockRootyDirt){
				if(world.rand.nextInt(8) == 0){
					block.updateTick(world, iPos.getX(), iPos.getY(), iPos.getZ(), world.rand);
				}
			}
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
				return tree.getName();
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
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityDendroCoil();
	}

	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);
		
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
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		topIcon = reg.registerIcon(DynamicTrees.MODID + ":" + "coil-top");
		sideIcon = reg.registerIcon(DynamicTrees.MODID + ":" + "coil-side");
		bottomIcon = reg.registerIcon(DynamicTrees.MODID + ":" + "coil-bottom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

}
