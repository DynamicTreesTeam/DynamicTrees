package com.ferreusveritas.dynamictrees.items;

import java.util.List;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.blocks.BonsaiPotBlock;
import com.ferreusveritas.dynamictrees.event.SeedVoluntaryPlantEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.resources.DTDataPackRegistries;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;


public class Seed extends Item implements IPlantable {
	
	private final Species species;//The tree this seed creates
	
	public Seed() { super(new Item.Properties());setRegistryName("null"); species = Species.NULL_SPECIES;}
	
	public Seed(Species species) {
		super(new Item.Properties().group(DTRegistries.dynamicTreesTab));
		setRegistryName(species.getRegistryName().getPath() + "_seed");
		this.species = species;
	}
	
	public Species getSpecies() {
		return species;
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if(entityItem.lifespan == 6000) { //6000(5 minutes) is the default lifespan for an entity item
			entityItem.lifespan = getTimeToLive(entityItem.getItem()) + 20;//override default lifespan with new value + 20 ticks(1 second)
			if(entityItem.lifespan == 6000) {
				entityItem.lifespan = 6001;//Ensure this isn't run again
			}
		}
		
		if(entityItem.ticksExisted >= entityItem.lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
			World world = entityItem.world;
			if(!world.isRemote) {//Server side only
				ItemStack seedStack = entityItem.getItem();
				BlockPos pos = new BlockPos(entityItem.getPosition());
				SeedVoluntaryPlantEvent seedVolEvent = new SeedVoluntaryPlantEvent(entityItem, getSpecies(), pos, shouldPlant(world, pos, seedStack));
				MinecraftForge.EVENT_BUS.post(seedVolEvent);
				if(!seedVolEvent.isCanceled() && seedVolEvent.getWillPlant()) {
					doPlanting(world, pos, null, seedStack);
				}
				seedStack.setCount(0);
			}
			entityItem.onKillCommand();
		}
		
		return false;
	}
	
	public boolean doPlanting(World world, BlockPos pos, PlayerEntity planter, ItemStack seedStack) {
		Species species = getSpecies();
		if(species.plantSapling(world, pos)) {//Do the planting
			String joCode = getCode(seedStack);
			if(!joCode.isEmpty()) {
				world.removeBlock(pos, false);//Remove the newly created dynamic sapling
				species.getJoCode(joCode).setCareful(true).generate(world, world, species, pos.down(), world.getBiome(pos), planter != null ? planter.getHorizontalFacing() : Direction.NORTH, 8, SafeChunkBounds.ANY);
			}
			return true;
		}
		return false;
	}
	
	public boolean shouldPlant(World world, BlockPos pos, ItemStack seedStack) {
		
		if(hasForcePlant(seedStack)) {
			return true;
		}
		
		if(!world.canBlockSeeSky(pos)) {
			return false;
		}
		
		float plantChance = (float) (getSpecies().biomeSuitability(world, pos) * DTConfigs.seedPlantRate.get());
		
		if(DTConfigs.seedOnlyForest.get()) {
			plantChance *= DTDataPackRegistries.BIOME_DATABASE_MANAGER.getDimensionDatabase(world.getDimensionKey().getLocation())
					.getForestness(world.getBiome(pos));
		}
		
		float accum = 1.0f;
		int count = seedStack.getCount();
		while(count-- > 0) {
			accum *= 1.0f - plantChance;
		}
		plantChance = 1.0f - accum;
		
		return plantChance > world.rand.nextFloat();
	}
	
	public boolean hasForcePlant(ItemStack seedStack) {
		boolean forcePlant = false;
		if(seedStack.hasTag()) {
			CompoundNBT nbtData = seedStack.getTag();
			forcePlant = nbtData.getBoolean("forceplant");
		}
		return forcePlant;
	}
	
	public int getTimeToLive(ItemStack seedStack) {
		int lifespan = DTConfigs.seedTimeToLive.get();//1 minute by default(helps with lag)
		if(seedStack.hasTag()) {
			CompoundNBT nbtData = seedStack.getTag();
			assert nbtData != null;
			if(nbtData.contains("lifespan")) {
				lifespan = nbtData.getInt("lifespan");
			}
		}
		return lifespan;
	}
	
	public String getCode(ItemStack seedStack) {
		String joCode = "";
		if(seedStack.hasTag()) {
			CompoundNBT nbtData = seedStack.getTag();
			assert nbtData != null;
			joCode = nbtData.getString("code");
		}
		return joCode;
	}

	public ActionResultType onItemUseFlowerPot(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState emptyPotState = world.getBlockState(pos);
		Block emptyPotBlock = emptyPotState.getBlock();

		if (!(emptyPotBlock instanceof FlowerPotBlock) || emptyPotState != emptyPotBlock.getDefaultState())
			return ActionResultType.PASS;

		BonsaiPotBlock bonsaiPot = this.getSpecies().getBonsaiPot();
		world.setBlockState(pos, bonsaiPot.getDefaultState());

		if (bonsaiPot.setSpecies(world, pos, bonsaiPot.getDefaultState(), this.getSpecies()) && bonsaiPot.setPotState(world, emptyPotState, pos)) {
			context.getItem().shrink(1);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
	
	public ActionResultType onItemUsePlantSeed(ItemUseContext context) {
		
		BlockState state = context.getWorld().getBlockState(context.getPos());
		BlockPos pos = context.getPos();
		Direction facing = context.getFace();
		if(state.getMaterial().isReplaceable()) {
			pos = pos.down();
			facing = Direction.UP;
		}
		
		if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
			if (context.getPlayer().canPlayerEdit(pos, facing, context.getItem()) && context.getPlayer().canPlayerEdit(pos.up(), facing, context.getItem())) {//Ensure permissions to edit block
				if(doPlanting(context.getWorld(), pos.up(), context.getPlayer(), context.getItem())) {
					context.getItem().shrink(1);
					return ActionResultType.SUCCESS;
				}
			}
		}
		
		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		// Handle flower pot interaction (flower pot cancels on item use so this must be done first).
		if(onItemUseFlowerPot(context) == ActionResultType.SUCCESS) {
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		// Handle planting seed interaction.
		if(onItemUsePlantSeed(context) == ActionResultType.SUCCESS) {
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, world, tooltip, flagIn);
		
		if(stack.hasTag()) {
			String joCode = getCode(stack);
			if(!joCode.isEmpty()) {
				tooltip.add(new StringTextComponent("Code: §6" + joCode));
			}
			if(hasForcePlant(stack)) {
				tooltip.add(new StringTextComponent("Force Planting: §3Enabled"));
			}
			CompoundNBT nbtData = stack.getTag();
			assert nbtData != null;
			if(nbtData.contains("lifespan")) {
				tooltip.add(new StringTextComponent("Seed Life Span: §3" + nbtData.getInt("lifespan")));
			}
		}
	}
	
	
	///////////////////////////////////////////
	//IPlantable Interface
	///////////////////////////////////////////
	
	@Override
	public BlockState getPlant(IBlockReader world, BlockPos pos) {
		return getSpecies().getSapling().map(Block::getDefaultState).orElse(Blocks.AIR.getDefaultState());
	}
	
}
