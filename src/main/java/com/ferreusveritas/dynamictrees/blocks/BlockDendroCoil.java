package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.tileentity.TileEntityDendroCoil;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.Circle;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.ILorable;
import com.ferreusveritas.dynamictrees.util.IRegisterable;
import com.ferreusveritas.dynamictrees.worldgen.CircleHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator.EnumGeneratorResult;

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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

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
	public void onNeighborBlockChange(net.minecraft.world.World world, int x, int y, int z, Block block) {
		if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
			growPulse(new World(world), new BlockPos(x, y, z));
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
			jo.generate(world, tree, pos.up(), EnumFacing.NORTH, 8);
		} else {
			Logger.getLogger(DynamicTrees.MODID).log(Level.WARNING, "Tree: " + treeName + " not found.");
		}
	}

	public void createStaff(World world, BlockPos pos, String treeName, String JoCode, String rgb, boolean readOnly) {
		ItemStack stack = new ItemStack(DynamicTrees.treeStaff, 1, 0);
		DynamicTree tree = TreeRegistry.findTree(treeName);
		DynamicTrees.treeStaff.setTree(stack, tree).setCode(stack, JoCode).setColor(stack, rgb).setReadOnly(stack, readOnly);
		EntityItem entityItem = new EntityItem(world.getWorld(), pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack);
		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;
		CompatHelper.spawnEntity(world, entityItem);
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
	
	public void testPoisson(World world, BlockPos pos, int rad1, int rad2, double angle) {
		pos = pos.up();
		
		for(int y = 0; y < 2; y++) {
			for(int z = -28; z <= 28; z++) {
				for(int x = -28; x <= 28; x++) {
					world.setBlockToAir(pos.add(x, y, z));
				}
			}
		}
		
		if(rad1 >= 2 && rad2 >= 2 && rad1 <= 8 && rad2 <= 8) {
			Circle circleA = new Circle(pos, rad1);
			DynamicTrees.treeGenerator.makeWoolCircle(world, circleA, pos.getY(), EnumGeneratorResult.NOTREE, 3);

			Circle circleB = CircleHelper.findSecondCircle(circleA, rad2, angle);
			DynamicTrees.treeGenerator.makeWoolCircle(world, circleB, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			world.setBlockState(new BlockPos(circleB.x, pos.up().getY(), circleB.z), circleB.isLoose() ? new BlockAndMeta(Blocks.cobblestone) : new BlockAndMeta(Blocks.diamond_block));
		}
	}
	
	public void testPoisson2(World world, BlockPos pos, int rad1, int rad2, double angle, int rad3) {
		pos = pos.up();
				
		//System.out.println("Test: " + "R1:" + rad1 + ", R2:" + rad2 + ", angle:" + angle + ", R3:" + rad3);
		
		for(int y = 0; y < 2; y++) {
			for(int z = -28; z <= 28; z++) {
				for(int x = -28; x <= 28; x++) {
					world.setBlockToAir(pos.add(x, y, z));
				}
			}
		}
		
		if(rad1 >= 2 && rad2 >= 2 && rad1 <= 8 && rad2 <= 8 && rad3 >= 2 && rad3 <= 8) {
			Circle circleA = new Circle(pos, rad1);
			DynamicTrees.treeGenerator.makeWoolCircle(world, circleA, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			
			Circle circleB = CircleHelper.findSecondCircle(circleA, rad2, angle);
			DynamicTrees.treeGenerator.makeWoolCircle(world, circleB, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			
			CircleHelper.maskCircles(circleA, circleB);
			
			Circle circleC = CircleHelper.findThirdCircle(circleA, circleB, rad3);
			if(circleC != null) {
				DynamicTrees.treeGenerator.makeWoolCircle(world, circleC, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			} else {
				System.out.println("Angle:" + angle);
				world.setBlockState(new BlockPos(circleA.x, pos.up().getY(), circleA.z), new BlockAndMeta(Blocks.redstone_block));
			}
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(net.minecraft.world.World world, int meta) {
		return new TileEntityDendroCoil();
	}

	@Override
	public IPeripheral getPeripheral(net.minecraft.world.World world, int x, int y, int z, int side) {
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
