package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.function.Consumer;


public class Seed extends Item {
	
	public final static Seed NULLSEED = new Seed("null") {
//		{ setCreativeTab(null); }
//		@Override public void setSpecies(Species species, ItemStack seedStack) {}
//		@Override public Species getSpecies(ItemStack seedStack) { return Species.NULLSPECIES; }
//		@Override public boolean onEntityItemUpdate(EntityItem entityItem) { entityItem.setDead(); return false; }
//		@Override public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) { return EnumActionResult.FAIL; }
	};
	
	private Species species;//The tree this seed creates
	
	public Seed(String name) {
		super(new Item.Properties());
		setRegistryName(name);
//		setCreativeTab(ModTabs.dynamicTreesTab);
	}
	
	public void setSpecies(Species species, ItemStack seedStack) {
		this.species = species;
	}
	
	public Species getSpecies(ItemStack seedStack) {
		return species;
	}
	
	public boolean isValid() {
		return this != NULLSEED;
	}
	
	public void ifValid(Consumer<Seed> c) {
		if(isValid()) {
			c.accept(this);
		}
	}
	
//	@Override
//	public boolean onEntityItemUpdate(ItemEntity entityItem) {
//
//		if(entityItem.lifespan == 6000) { //6000(5 minutes) is the default lifespan for an entity item
//			entityItem.lifespan = getTimeToLive(entityItem.getItem()) + 20;//override default lifespan with new value + 20 ticks(1 second)
//			if(entityItem.lifespan == 6000) {
//				entityItem.lifespan = 6001;//Ensure this isn't run again
//			}
//		}
//
//		if(entityItem.ticksExisted >= entityItem.lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
//			World world = entityItem.world;
//			if(!world.isRemote) {//Server side only
//				ItemStack seedStack = entityItem.getItem();
//				BlockPos pos = new BlockPos(entityItem);
//				SeedVoluntaryPlantEvent seedVolEvent = new SeedVoluntaryPlantEvent(entityItem, getSpecies(seedStack), pos, shouldPlant(world, pos, seedStack));
//				MinecraftForge.EVENT_BUS.post(seedVolEvent);
//				if(!seedVolEvent.isCanceled() && seedVolEvent.getWillPlant()) {
//					doPlanting(world, pos, null, seedStack);
//				}
//				seedStack.setCount(0);
//			}
//			entityItem.setDead();
//		}
//
//		return false;
//	}
//
//	public boolean doPlanting(World world, BlockPos pos, EntityPlayer planter, ItemStack seedStack) {
//		Species species = getSpecies(seedStack);
//		if(species.plantSapling(world, pos)) {//Do the planting
//			String joCode = getCode(seedStack);
//			if(!joCode.isEmpty()) {
//				world.setBlockToAir(pos);//Remove the newly created dynamic sapling
//				species.getJoCode(joCode).setCareful(true).generate(world, species, pos.down(), world.getBiome(pos), planter != null ? planter.getHorizontalFacing() : Direction.NORTH, 8, SafeChunkBounds.ANY);
//			}
//			return true;
//		}
//		return false;
//	}
//
//	public boolean shouldPlant(World world, BlockPos pos, ItemStack seedStack) {
//
//		if(hasForcePlant(seedStack)) {
//			return true;
//		}
//
//		if(!world.canBlockSeeSky(pos)) {
//			return false;
//		}
//
//		float plantChance = getSpecies(seedStack).biomeSuitability(world, pos) * DTConfigs.seedPlantRate;
//
//		TreeGenerator treeGen = TreeGenerator.getTreeGenerator();
//		if(DTConfigs.seedOnlyForest && treeGen != null) {
//			plantChance *= treeGen.getBiomeDataBase(world).getForestness(world.getBiome(pos));
//		}
//
//		float accum = 1.0f;
//		int count = seedStack.getCount();
//		while(count-- > 0) {
//			accum *= 1.0f - plantChance;
//		}
//		plantChance = 1.0f - accum;
//
//		return plantChance > world.rand.nextFloat();
//	}
//
//	public boolean hasForcePlant(ItemStack seedStack) {
//		boolean forcePlant = false;
//		if(seedStack.hasTagCompound()) {
//			CompoundNBT nbtData = seedStack.getTagCompound();
//			forcePlant = nbtData.getBoolean("forceplant");
//		}
//		return forcePlant;
//	}
//
//	public int getTimeToLive(ItemStack seedStack) {
//		int lifespan = DTConfigs.seedTimeToLive;//1 minute by default(helps with lag)
//		if(seedStack.hasTagCompound()) {
//			CompoundNBT nbtData = seedStack.getTagCompound();
//			if(nbtData.hasKey("lifespan")) {
//				lifespan = nbtData.getInteger("lifespan");
//			}
//		}
//		return lifespan;
//	}
//
//	public String getCode(ItemStack seedStack) {
//		String joCode = "";
//		if(seedStack.hasTagCompound()) {
//			CompoundNBT nbtData = seedStack.getTagCompound();
//			joCode = nbtData.getString("code");
//		}
//		return joCode;
//	}
//
//	public EnumActionResult onItemUseFlowerPot(EntityPlayer player, World world, BlockPos pos, EnumHand hand, ItemStack seedStack, Direction facing, float hitX, float hitY, float hitZ) {
//		//Handle Flower Pot interaction
//		BlockState emptyPotState = world.getBlockState(pos);
//		if(emptyPotState.getBlock() instanceof BlockFlowerPot && (emptyPotState == emptyPotState.getBlock().getDefaultState()) ) { //Empty Flower Pot of some kind
//			Species species = getSpecies(seedStack);
//			BlockBonsaiPot bonzaiPot = species.getBonzaiPot();
//			world.setBlockState(pos, bonzaiPot.getDefaultState());
//			if(bonzaiPot.setSpecies(world, species, pos) && bonzaiPot.setPotState(world, emptyPotState, pos)) {
//				seedStack.shrink(1);
//				return EnumActionResult.SUCCESS;
//			}
//		}
//
//		return EnumActionResult.PASS;
//	}
//
//	public EnumActionResult onItemUsePlantSeed(EntityPlayer player, World world, BlockPos pos, EnumHand hand, ItemStack seedStack, Direction facing, float hitX, float hitY, float hitZ) {
//
//		BlockState iblockstate = world.getBlockState(pos);
//		Block block = iblockstate.getBlock();
//
//		if(block.isReplaceable(world, pos)) {
//			pos = pos.down();
//			facing = Direction.UP;
//		}
//
//		if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
//			if (player.canPlayerEdit(pos, facing, seedStack) && player.canPlayerEdit(pos.up(), facing, seedStack)) {//Ensure permissions to edit block
//				if(doPlanting(world, pos.up(), player, seedStack)) {
//					seedStack.shrink(1);
//					return EnumActionResult.SUCCESS;
//				}
//			}
//		}
//
//		return EnumActionResult.PASS;
//	}
//
//	@Override
//	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
//		ItemStack seedStack = player.getHeldItem(hand);
//
//		//Handle Flower Pot interaction
//		if(onItemUseFlowerPot(player, world, pos, hand, seedStack, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
//			return EnumActionResult.SUCCESS;
//		}
//
//		//Handle Planting Seed interaction
//		if(onItemUsePlantSeed(player, world, pos, hand, seedStack, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
//			return EnumActionResult.SUCCESS;
//		}
//
//		return EnumActionResult.FAIL;
//	}
//
//	@Override
//	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
//		super.addInformation(stack, world, tooltip, flagIn);
//
//		if(stack.hasTagCompound()) {
//			String joCode = getCode(stack);
//			if(!joCode.isEmpty()) {
//				tooltip.add("Code: §6" + joCode);
//			}
//			if(hasForcePlant(stack)) {
//				tooltip.add("Force Planting: §3Enabled");
//			}
//			CompoundNBT nbtData = stack.getTagCompound();
//			if(nbtData.hasKey("lifespan")) {
//				tooltip.add("Seed Life Span: §3" + nbtData.getInteger("lifespan") );
//			}
//		}
//	}
//
}
