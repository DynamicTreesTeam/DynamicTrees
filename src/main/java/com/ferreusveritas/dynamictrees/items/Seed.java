package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.event.SeedVoluntaryPlantEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

// TODO: Make compostable via ComposterBlock#registerCompostable
public class Seed extends Item implements IPlantable {
	
	private final Species species;//The tree this seed creates
	
	public Seed() {
		super(new Item.Properties());
		this.setRegistryName("null");
		this.species = Species.NULL_SPECIES;
	}
	
	public Seed(Species species) {
		super(new Item.Properties().tab(DTRegistries.ITEM_GROUP));
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
		
		if(entityItem.tickCount >= entityItem.lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
			World world = entityItem.level;
			if(!world.isClientSide) {//Server side only
				ItemStack seedStack = entityItem.getItem();
				BlockPos pos = new BlockPos(entityItem.blockPosition());
				SeedVoluntaryPlantEvent seedVolEvent = new SeedVoluntaryPlantEvent(entityItem, getSpecies(), pos, shouldPlant(world, pos, seedStack));
				MinecraftForge.EVENT_BUS.post(seedVolEvent);
				if(!seedVolEvent.isCanceled() && seedVolEvent.getWillPlant()) {
					doPlanting(world, pos, null, seedStack);
				}
				seedStack.setCount(0);
			}
			entityItem.kill();
		}
		
		return false;
	}
	
	public boolean doPlanting(World world, BlockPos pos, PlayerEntity planter, ItemStack seedStack) {
		Species species = getSpecies();
		if(species.plantSapling(world, pos)) {//Do the planting
			String joCode = getCode(seedStack);
			if(!joCode.isEmpty()) {
				world.removeBlock(pos, false);//Remove the newly created dynamic sapling
				species.getJoCode(joCode).setCareful(true).generate(world, world, species, pos.below(), world.getBiome(pos), planter != null ? planter.getDirection() : Direction.NORTH, 8, SafeChunkBounds.ANY);
			}
			return true;
		}
		return false;
	}
	
	public boolean shouldPlant(World world, BlockPos pos, ItemStack seedStack) {
		
		if(hasForcePlant(seedStack)) {
			return true;
		}
		
		if(!world.canSeeSkyFromBelowWater(pos)) {
			return false;
		}
		
		float plantChance = (float) (getSpecies().biomeSuitability(world, pos) * DTConfigs.SEED_PLANT_RATE.get());
		
		if(DTConfigs.SEED_ONLY_FOREST.get()) {
			plantChance *= DTResourceRegistries.BIOME_DATABASE_MANAGER.getDimensionDatabase(world.dimension().location())
					.getForestness(world.getBiome(pos));
		}
		
		float accum = 1.0f;
		int count = seedStack.getCount();
		while(count-- > 0) {
			accum *= 1.0f - plantChance;
		}
		plantChance = 1.0f - accum;
		
		return plantChance > world.random.nextFloat();
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
		int lifespan = DTConfigs.SEED_TIME_TO_LIVE.get();//1 minute by default(helps with lag)
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
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState emptyPotState = world.getBlockState(pos);
		Block emptyPotBlock = emptyPotState.getBlock();

		if (!(emptyPotBlock instanceof FlowerPotBlock) || emptyPotState != emptyPotBlock.defaultBlockState())
			return ActionResultType.PASS;

		PottedSaplingBlock bonsaiPot = this.getSpecies().getBonsaiPot();
		world.setBlockAndUpdate(pos, bonsaiPot.defaultBlockState());

		if (bonsaiPot.setSpecies(world, pos, bonsaiPot.defaultBlockState(), this.getSpecies()) && bonsaiPot.setPotState(world, emptyPotState, pos)) {
			context.getItemInHand().shrink(1);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
	
	public ActionResultType onItemUsePlantSeed(ItemUseContext context) {
		
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();
		if(state.getMaterial().isReplaceable()) {
			pos = pos.below();
			facing = Direction.UP;
		}
		
		if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
			if (context.getPlayer().mayUseItemAt(pos, facing, context.getItemInHand()) && context.getPlayer().mayUseItemAt(pos.above(), facing, context.getItemInHand())) {//Ensure permissions to edit block
				if(doPlanting(context.getLevel(), pos.above(), context.getPlayer(), context.getItemInHand())) {
					context.getItemInHand().shrink(1);
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
	public ActionResultType useOn(ItemUseContext context) {
		// Handle planting seed interaction.
		if(onItemUsePlantSeed(context) == ActionResultType.SUCCESS) {
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}

	public static final String LIFESPAN_TAG = "lifespan";
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.appendHoverText(stack, world, tooltip, flagIn);
		
		if (stack.hasTag()) {
			final String joCode = this.getCode(stack);
			if (!joCode.isEmpty()) {
				tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.jo_code", new JoCode(joCode).getTextComponent()));
			}
			if (this.hasForcePlant(stack)) {
				tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.force_planting",
						new TranslationTextComponent("tooltip.dynamictrees.enabled")
								.withStyle(style -> style.withColor(TextFormatting.DARK_AQUA)))
				);
			}
			final CompoundNBT nbtData = stack.getTag();
			assert nbtData != null;

			if (nbtData.contains(LIFESPAN_TAG)) {
				tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.seed_life_span" +
						new StringTextComponent(String.valueOf(nbtData.getInt(LIFESPAN_TAG)))
								.withStyle(style -> style.withColor(TextFormatting.DARK_AQUA)))
				);
			}
		}
	}
	
	
	///////////////////////////////////////////
	//IPlantable Interface
	///////////////////////////////////////////
	
	@Override
	public BlockState getPlant(IBlockReader world, BlockPos pos) {
		return getSpecies().getSapling().map(Block::defaultBlockState).orElse(Blocks.AIR.defaultBlockState());
	}
	
}
