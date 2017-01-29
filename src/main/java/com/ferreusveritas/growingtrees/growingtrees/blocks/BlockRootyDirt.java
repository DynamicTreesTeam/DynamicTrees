package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import com.ferreusveritas.growingtrees.ConfigHandler;
import com.ferreusveritas.growingtrees.Dir;
import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.inspectors.NodeDisease;
import com.ferreusveritas.growingtrees.inspectors.NodeFreezer;
import com.ferreusveritas.growingtrees.inspectors.NodeTwinkle;
import com.ferreusveritas.growingtrees.renderers.RendererBranch;
import com.ferreusveritas.growingtrees.renderers.RendererRootyDirt;
import com.ferreusveritas.growingtrees.renderers.RendererRootyDirt.RenderType;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRootyDirt extends Block implements ITreePart {

	public IIcon dirtIcon;
	public IIcon grassIcon;
	public IIcon myceliumIcon;
	public IIcon podzolIcon;
	
	public BlockRootyDirt() {
		super(Material.ground);
        this.setTickRandomly(true);
	}
    
	@Override
    public void updateTick(World world, int x, int y, int z, Random random){
		grow(world, x, y, z, random);
	}

	public boolean grow(World world, int x, int y, int z, Random random){
	
		BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
		
		if(branch != null){
			GrowingTree tree = branch.getTree();
			float growthRate = tree.getGrowthRate(world, x, y + 1, z) * ConfigHandler.treeGrowthRateMultiplier;
			do{
				if(random.nextFloat() < growthRate){
					int life = getSoilLife(world, x, y, z);
					if(life > 0){
						boolean success = false;

						float energy = tree.getEnergy(world, x, y + 1, z);
						for(int i = 0; !success && i < 1 + tree.retries; i++){//Some species have multiple growth retry attempts
							success = branch.growSignal(world, x, y + 1, z, new GrowSignal(branch, x, y, z, energy)).success;
						}

						int soilLongevity = tree.getSoilLongevity(world, x, y + 1, z) * (success ? 1 : 16);//Don't deplete the soil as much if the grow operation failed

						if(random.nextInt(soilLongevity) == 0){//1 in X(soilLongevity) chance to draw nutrients from soil
							setSoilLife(world, x, y, z, life - 1);//decrement soil life
						}
					} else {
						if(random.nextFloat() < ConfigHandler.diseaseChance){
							branch.analyse(world, x, y, z, ForgeDirection.DOWN, new MapSignal(new NodeDisease(tree)));
						}
					}
				}
			} while(--growthRate > 0.0f);
		} else {
			world.setBlock(x,  y,  z, Blocks.dirt, 0, 3);
			return false;
		}
		
		return true;
	}
	
	@Override
    public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.dirt);
    }
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return 20.0f;//Encourage proper tool usage and discourage bypassing tree felling by digging the root from under the tree
	};
	
	@Override
    protected boolean canSilkHarvest() {
		return false;
    }
    
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz){

		ItemStack equippedItem = player.getCurrentEquippedItem();
		
		if(equippedItem != null){//Something in the hand
			return applyItemSubstance(world, x, y, z, player, equippedItem);
		}
		else{//Bare hand
			if(world.isRemote){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Rooty Soil Life: " + world.getBlockMetadata(x, y, z)));
			}
			return true;
		}
    }
	
	@Override
	public boolean applyItemSubstance(World world, int x, int y, int z, EntityPlayer player, ItemStack itemStack){
		BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
		
		if(branch != null && branch.getTree().applySubstance(world, x, y, z, this, itemStack)){
			if(itemStack.getItem() == Items.potionitem){
				player.setCurrentItemOrArmor(0, new ItemStack(Items.glass_bottle));
			} else {
				itemStack.stackSize--;
			}
			return true;
		}
		return false;
	}
	
	public boolean substanceFertilize(World world, int x, int y, int z, int amount){
		if(fertilize(world, x, y, z, amount)){
			if(world.isRemote){
				TreeHelper.getSafeTreePart(world, x, y + 1, z).analyse(world, x, y + 1, z, ForgeDirection.UNKNOWN, new MapSignal(new NodeTwinkle("happyVillager", 8)));
			}
			return true;
		}
		return false;
	}
	
	public boolean substanceDeplete(World world, int x, int y, int z, int amount){
		if(fertilize(world, x, y, z, -amount)){
			if(world.isRemote){
				TreeHelper.getSafeTreePart(world, x, y + 1, z).analyse(world, x, y + 1, z, ForgeDirection.UNKNOWN, new MapSignal(new NodeTwinkle("crit", 8)));
			}
			return true;
		}
		return false;
	}
	
	public boolean substanceInstantGrowth(World world, int x, int y, int z){
		if(world.isRemote){
			TreeHelper.getSafeTreePart(world, x, y + 1, z).analyse(world, x, y + 1, z, ForgeDirection.UNKNOWN, new MapSignal(new NodeTwinkle("spell", 8)));
		} else {
			grow(world, x, y, z, world.rand);
		}
		return true;
	}
	
	public boolean substanceFreeze(World world, int x, int y, int z){
		BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
		if(branch != null){
			branch.analyse(world, x, y, z, ForgeDirection.DOWN, new MapSignal(new NodeFreezer(), new NodeTwinkle("fireworksSpark", 8)));
			fertilize(world, x, y, z, -15);//destroy the soil life so it can no longer grow
		}
		return true;
	}
	
	public boolean substanceDisease(World world, int x, int y, int z){
		if(world.isRemote){
			TreeHelper.getSafeTreePart(world, x, y + 1, z).analyse(world, x, y + 1, z, ForgeDirection.UNKNOWN, new MapSignal(new NodeTwinkle("crit", 8)));
		} else {
			BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
			if(branch != null){
				TreeHelper.getSafeTreePart(world, x, y + 1, z).analyse(world, x, y + 1, z, ForgeDirection.UNKNOWN, new MapSignal(new NodeDisease(branch.getTree())));
				fertilize(world, x, y, z, -15);//destroy the soil life so it can no longer grow
			}
		}
		return true;
	}
	
	public void destroyTree(World world, int x, int y, int z){
		BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
		if(branch != null){
			branch.destroyEntireTree(world, x, y + 1, z);
		}
	}
	
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int localMeta, EntityPlayer player) {
		destroyTree(world, x, y, z);
	}
	
	@Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion){
		destroyTree(world, x, y, z);
	}
	
	public int getSoilLife(IBlockAccess blockAccess, int x, int y, int z){
		return blockAccess.getBlockMetadata(x, y, z);
	}
	
	public void setSoilLife(World world, int x, int y, int z, int life){
		world.setBlockMetadataWithNotify(x, y, z, MathHelper.clamp_int(life, 0, 15), 3);
	}
	
	public boolean fertilize(World world, int x, int y, int z, int amount){
		int soilLife = getSoilLife(world, x, y, z);
		if((soilLife == 0 && amount < 0) || (soilLife == 15 && amount > 0)){
			return false;//Already maxed out
		}
		setSoilLife(world, x, y, z, soilLife + amount);
		return true;
	}
	
	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, GrowingTree leavesTree) {
		return 0;
	}

	@Override
	public GrowSignal growSignal(World world, int x, int y, int z, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from, int fromRadius) {
		return 8;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from) {
		return 0;
	}

	@Override
	public int getRadius(IBlockAccess blockAccess, int x, int y, int z) {
		return 0;
	}

	@Override
	public boolean isRootNode() {
		return true;
	}

	@Override
	public MapSignal analyse(World world, int x, int y, int z, ForgeDirection fromDir, MapSignal signal) {
		signal.run(world, this, x, y, z, fromDir);//Run inspector of choice

		signal.rootX = x;
		signal.rootY = y;
		signal.rootZ = z;
		signal.found = true;
		
		return signal;
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, int x, int y, int z, ForgeDirection dir, int radius){
		if(dir == ForgeDirection.DOWN){
			//1.) If it's a twig(radius == 1) and the soil is barren then don't count the rooty dirt block as support
			//2.) If it's a stocky piece(radius > 1) then count the soil as support regardless of soil life(for preserving mature trees)
			return radius == 1 && blockAccess.isAirBlock(x, y + 2, z) && getSoilLife(blockAccess, x, y, z) > 0 ? 0x12 : 0x11;
		} 
		return 0;
	}

	@Override
    public int getMobilityFlag() {
        return 2;
    }

	@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {

		if(RendererRootyDirt.renderPass == 1){//First Pass
			switch(side){
				case 0: return dirtIcon;//Bottom
				case 1: switch(RendererRootyDirt.renderType){//Top
					case GRASS: return Blocks.grass.getIcon(side, 0);
					case MYCELIUM: return Blocks.mycelium.getIcon(side, 0);
					case PODZOL: return Blocks.dirt.getIcon(side, 2);
					default: return Blocks.dirt.getIcon(side, 0);
					}
				default: switch(RendererRootyDirt.renderType){//All other sides
					case GRASS: return grassIcon;
					case MYCELIUM: return myceliumIcon;
					case PODZOL: return podzolIcon;
					default: return dirtIcon;
				}
			}
		} else {//Second Pass
			if(RendererRootyDirt.renderType == RenderType.GRASS){
				if(side == 1){//Top
					return Blocks.grass.getIcon(side, 0);
				} else if(side != 0){//NSWE
					return Blocks.grass.getIconSideOverlay();
				}
			}
		}

		return dirtIcon;//Everything else
    }

	@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
		if(side == 1){
			return Blocks.dirt.getIcon(side, 0);
		}
		return dirtIcon;
    }

	
	public RenderType getRenderType(IBlockAccess blockAccess, int x, int y, int z){
		BlockAndMeta mimic = new BlockAndMeta();
		
		final int dMap[] = {0, -1, 1};
		
		for(int depth = 0; depth < 3; depth++){
			for(Dir d: Dir.CARDINAL){
				mimic.setFromCoords(blockAccess, x + d.xOffset, y + dMap[depth], z + d.zOffset);

				if(mimic.matches(Blocks.grass)){
					return RenderType.GRASS;
				} else if(mimic.matches(Blocks.mycelium)){
					return RenderType.MYCELIUM;
				} else if(mimic.matches(Blocks.dirt, 2)){
					return RenderType.PODZOL;
				}
			}
		}
		
		return RenderType.DIRT;//Default to plain old dirt
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
		
		boolean shouldRender = super.shouldSideBeRendered(access, x, y, z, side);

		if(shouldRender){
			if(RendererRootyDirt.renderPass == 1){//First Pass
				if(RendererRootyDirt.renderType == RenderType.GRASS){
					return side != 1;//Don't render top of grass block on first pass	
				}
				return true;//Render all sides of dirt, mycelium and podzol block on first pass
			} else {//Second Pass
				if(RendererRootyDirt.renderType == RenderType.GRASS){
					return side != 0;//Don't render bottom of grass block on second pass	
				}
				return false;//Render nothing for dirt, mycelium and podzol block on second pass
			}
		}
		
		return false;
    }
	
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
    	if(RendererRootyDirt.renderType == RenderType.GRASS && RendererRootyDirt.renderPass == 2){
    		return Blocks.grass.colorMultiplier(blockAccess, x, y, z);
    	} else {
    		return super.colorMultiplier(blockAccess, x, y, z);
    	}
    }
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister register) {
		dirtIcon = register.registerIcon(GrowingTrees.MODID + ":" + "rootydirt-dirt");
		grassIcon = register.registerIcon(GrowingTrees.MODID + ":" + "rootydirt-grass");
		myceliumIcon = register.registerIcon(GrowingTrees.MODID + ":" + "rootydirt-mycelium");
		podzolIcon = register.registerIcon(GrowingTrees.MODID + ":" + "rootydirt-podzol");
	}

	@Override
	public GrowingTree getTree(IBlockAccess blockAccess, int x, int y, int z) {
		return TreeHelper.isBranch(blockAccess, x, y + 1, z) ? TreeHelper.getSafeTreePart(blockAccess, x, y + 1, z).getTree(blockAccess, x, y + 1, z) : null;
	}

	@Override
	public int getRenderType() {
		return RendererRootyDirt.renderId;
	}
	
}
