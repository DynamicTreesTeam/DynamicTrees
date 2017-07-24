package com.ferreusveritas.growingtrees.blocks;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.TreeRegistry;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.tileentity.TileEntityDendroCoil;
import com.ferreusveritas.growingtrees.trees.GrowingTree;
import com.ferreusveritas.growingtrees.worldgen.JoCode;

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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDendroCoil extends BlockContainer implements IPeripheralProvider {

	static String name = "dendrocoil";
	IIcon topIcon;
	IIcon sideIcon;
	IIcon bottomIcon;

	public BlockDendroCoil() {
		super(Material.iron);
		setBlockName(GrowingTrees.MODID + "_" + name);
		setCreativeTab(GrowingTrees.growingTreesTab);
		ComputerCraftAPI.registerPeripheralProvider(this);
		GameRegistry.registerTileEntity(TileEntityDendroCoil.class, "dendrocoil");
		GameRegistry.registerBlock(this, name);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		topIcon = reg.registerIcon(GrowingTrees.MODID + ":" + "coil-top");
		sideIcon = reg.registerIcon(GrowingTrees.MODID + ":" + "coil-side");
		bottomIcon = reg.registerIcon(GrowingTrees.MODID + ":" + "coil-bottom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
			growPulse(world, x, y, z);
		}
	}

	public void growPulse(World world, int x, int y, int z){
		for(int iy = 0; iy < 32; iy++){
			for(int iz = -8; iz <= 8; iz++){
				for(int ix = -8; ix <= 8; ix++){
					Block thisBlock = world.getBlock(x + ix, y + iy, z + iz);
					if(thisBlock instanceof BlockGrowingLeaves){
						((BlockGrowingLeaves)thisBlock).updateLeaves(world, x + ix, y + iy, z + iz, world.rand, false);
					} else
					if(thisBlock instanceof BlockBranch){
						thisBlock.updateTick(world, x + ix, y + iy, z + iz, world.rand);
					} else
					if(thisBlock instanceof BlockRootyDirt){
						if(world.rand.nextInt(8) == 0){
							thisBlock.updateTick(world, x + ix, y + iy, z + iz, world.rand);
						}
					}
				}
			}
		}
	}

	public String getCode(World world, int x, int y, int z) {
		if(TreeHelper.isRootyDirt(world, x, y + 1, z)) {
			return new JoCode().buildFromTree(world, x, y + 1, z).toString();
		}
		
		return "";
	}

	public void setCode(World world, int x, int y, int z, String treeName, String JoCode) {
		JoCode jo = new JoCode(JoCode);
		GrowingTree tree = TreeRegistry.findTree(treeName);
		if(tree != null) {
			jo.growTree(world, tree, x, y + 1, z, ForgeDirection.NORTH, 8);
		} else {
			System.out.println("Tree: " + treeName + " not found.");
		}
	}

	public void createStaff(World world, int x, int y, int z, String treeName, String JoCode, String rgb, boolean readOnly) {
		ItemStack stack = new ItemStack(GrowingTrees.treeStaff, 1, 0);
		Seed seed = TreeRegistry.findTree(treeName).getSeed();
		GrowingTrees.treeStaff.setSeed(stack, seed);
		GrowingTrees.treeStaff.setCode(stack, JoCode);
		GrowingTrees.treeStaff.setColor(stack, rgb);
		GrowingTrees.treeStaff.setReadOnly(stack, readOnly);
		EntityItem entityItem = new EntityItem(world, x + 0.5, y + 1.5, z + 0.5, stack);
		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;
		world.spawnEntityInWorld(entityItem);
	}

	public String getTree(World world, int x, int y, int z) {
		ITreePart part = TreeHelper.getSafeTreePart(world, x, y + 1, z);
		if(part.isRootNode()) {
			GrowingTree tree = part.getTree(world, x, y + 1, z);
			if(tree != null) {
				return tree.getName();
			}
		}
		
		return "";
	}

	public void plantTree(World world, int x, int y, int z, String treeName) {
		GrowingTree tree = TreeRegistry.findTree(treeName);
		if(tree != null) {
			tree.getSeed().plantTree(world, x, y + 2, z);
		}
	}

	public void killTree(World world, int x, int y, int z) {
		ITreePart part = TreeHelper.getSafeTreePart(world, x, y + 1, z);
		if(part.isRootNode()) {
			((BlockRootyDirt)part).destroyTree(world, x, y + 1, z);
		}
	}

	public int getSoilLife(World world, int x, int y, int z) {
		ITreePart part = TreeHelper.getSafeTreePart(world, x, y + 1, z);
		if(part.isRootNode()) {
			return ((BlockRootyDirt)part).getSoilLife(world, x, y + 1, z);
		}
		return 0;
	}

	public void setSoilLife(World world, int x, int y, int z, int life) {
		ITreePart part = TreeHelper.getSafeTreePart(world, x, y + 1, z);
		if(part.isRootNode()) {
			((BlockRootyDirt)part).setSoilLife(world, x, y + 1, z, life);
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

}
