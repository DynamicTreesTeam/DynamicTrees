package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.tileentity.TileEntityDendroCoil;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.Circle;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.worldgen.CircleHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator.EnumGeneratorResult;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
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
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (world.isBlockIndirectlyGettingPowered(pos) != 0) {
			growPulse(world, pos);
		}
	}

	public String getCode(World world, BlockPos pos) {
		pos = pos.up();
		if(TreeHelper.isRooty(world.getBlockState(pos))) {
			return new JoCode().buildFromTree(world, pos).toString();
		}
		
		return "";
	}

	public void setCode(World world, BlockPos pos, String treeName, String JoCode) {
		Species species = TreeRegistry.findSpeciesSloppy(treeName);
		JoCode jo = species.getJoCode(JoCode);
		if(species != Species.NULLSPECIES) {
			jo.setCareful(true).generate(world, species, pos.up(), world.getBiome(pos), EnumFacing.NORTH, 8);
		} else {
			Logger.getLogger(ModConstants.MODID).log(Level.WARNING, "Tree: " + treeName + " not found.");
		}
	}

	public void createStaff(World world, BlockPos pos, String treeName, String JoCode, String rgb, boolean readOnly) {
		ItemStack stack = new ItemStack(ModItems.treeStaff, 1, 0);
		Species species = TreeRegistry.findSpeciesSloppy(treeName);
		ModItems.treeStaff.setSpecies(stack, species).setCode(stack, JoCode).setColor(stack, rgb).setReadOnly(stack, readOnly);
		EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack);
		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;
		CompatHelper.spawnEntity(world, entityItem);
	}

	public String getSpecies(World world, BlockPos pos) {
		IBlockState rootyState = world.getBlockState(pos.up());
		ITreePart part = TreeHelper.getTreePart(rootyState);
		if(part.isRootNode()) {
			return TreeHelper.getExactSpecies(rootyState, world, pos.up()).toString();
		}
		
		return "";
	}

	public void plantTree(World world, BlockPos pos, String treeName) {
		Species species = TreeRegistry.findSpeciesSloppy(treeName);
		species.plantSapling(world, pos.up(2));
	}

	public void growPulse(World world, BlockPos pos) {
		ITreePart part = TreeHelper.getTreePart(world.getBlockState(pos.up()));
		if(part.isRootNode()) {
			TreeHelper.growPulse(world, pos.up());
		}
	}
	
	public void killTree(World world, BlockPos pos) {
		ITreePart part = TreeHelper.getTreePart(world.getBlockState(pos.up()));
		if(part.isRootNode()) {
			((BlockRooty)part).destroyTree(world, pos.up());
		}
	}

	public int getSoilLife(World world, BlockPos pos) {
		IBlockState rootyState = world.getBlockState(pos.up());
		BlockRooty rooty = TreeHelper.getRooty(rootyState);
		if(rooty != null) {
			return rooty.getSoilLife(rootyState, world, pos.up());
		}
		return 0;
	}

	public void setSoilLife(World world, BlockPos pos, int life) {
		ITreePart part = TreeHelper.getTreePart(world.getBlockState(pos.up()));
		if(part.isRootNode()) {
			((BlockRooty)part).setSoilLife(world, pos.up(), life);
		}
	}
	
	public void testPoisson(World world, BlockPos pos, int rad1, int rad2, double angle, boolean onlyTight) {
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
			TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleA, pos.getY(), EnumGeneratorResult.NOTREE, 3);

			Circle circleB = CircleHelper.findSecondCircle(circleA, rad2, angle, onlyTight);
			TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleB, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			world.setBlockState(new BlockPos(circleB.x, pos.up().getY(), circleB.z), circleB.isTight() ? Blocks.DIAMOND_BLOCK.getDefaultState() : Blocks.COBBLESTONE.getDefaultState());
		}
	}
	
	public void testPoisson2(World world, BlockPos pos, int rad1, int rad2, double angle, int rad3, boolean onlyTight) {
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
			TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleA, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			
			Circle circleB = CircleHelper.findSecondCircle(circleA, rad2, angle, onlyTight);
			TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleB, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			
			CircleHelper.maskCircles(circleA, circleB);
			
			Circle circleC = CircleHelper.findThirdCircle(circleA, circleB, rad3);
			if(circleC != null) {
				TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleC, pos.getY(), EnumGeneratorResult.NOTREE, 3);
			} else {
				System.out.println("Angle:" + angle);
				world.setBlockState(new BlockPos(circleA.x, pos.up().getY(), circleA.z), Blocks.REDSTONE_BLOCK.getDefaultState());
			}
		}
	}
	
	public void testPoisson3(World world, BlockPos posA, int radA, BlockPos posB, int radB, int radC) {
		posA = posA.up();
		posB = posB.up();
				
		//System.out.println("Test: " + "R1:" + rad1 + ", R2:" + rad2 + ", angle:" + angle + ", R3:" + rad3);
		
		for(int y = 0; y < 2; y++) {
			for(int z = -28; z <= 28; z++) {
				for(int x = -28; x <= 28; x++) {
					world.setBlockToAir(posA.add(x, y, z));
				}
			}
		}
		
		if(radA >= 2 && radB >= 2 && radA <= 8 && radB <= 8 && radC >= 2 && radC <= 8) {
			Circle circleA = new Circle(posA, radA);
			TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleA, posA.getY(), EnumGeneratorResult.NOTREE, 3);
			
			Circle circleB = new Circle(posB, radB);
			TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleB, posB.getY(), EnumGeneratorResult.NOTREE, 3);
			
			CircleHelper.maskCircles(circleA, circleB);
			
			Circle circleC = CircleHelper.findThirdCircle(circleA, circleB, radC);
			if(circleC != null) {
				TreeGenerator.getTreeGenerator().makeWoolCircle(world, circleC, posA.getY(), EnumGeneratorResult.NOTREE, 3);
			} else {
				world.setBlockState(new BlockPos(circleA.x, posA.up().getY(), circleA.z), Blocks.REDSTONE_BLOCK.getDefaultState());
			}
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
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("§6ComputerCraft Peripheral");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

}
