package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.event.SeedVoluntaryPlantEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class Seed extends Item {
	
	public final static Seed NULLSEED = new Seed() {
		@Override public void setSpecies(Species species, ItemStack seedStack) {}
		@Override public Species getSpecies(ItemStack seedStack) { return Species.NULLSPECIES; }
		@Override public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) { entityItem.onKillCommand(); return false; }
		@Override public ActionResultType onItemUse(ItemUseContext context) { return ActionResultType.FAIL; }
	};
	
	private Species species;//The tree this seed creates

	public Seed() { super(new Item.Properties());setRegistryName("null"); }
	public Seed(String name) {
		super(new Item.Properties().group(DTRegistries.dynamicTreesTab));
		setRegistryName(name);
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
				BlockPos pos = new BlockPos(entityItem);
				SeedVoluntaryPlantEvent seedVolEvent = new SeedVoluntaryPlantEvent(entityItem, getSpecies(seedStack), pos, shouldPlant(world, pos, seedStack));
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
		Species species = getSpecies(seedStack);
		if(species.plantSapling(world, pos)) {//Do the planting
			String joCode = getCode(seedStack);
			if(!joCode.isEmpty()) {
				world.removeBlock(pos, false);//Remove the newly created dynamic sapling
				species.getJoCode(joCode).setCareful(true).generate(world, species, pos.down(), world.getBiome(pos), planter != null ? planter.getHorizontalFacing() : Direction.NORTH, 8, SafeChunkBounds.ANY);
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

		float plantChance = (float) (getSpecies(seedStack).biomeSuitability(world, pos) * DTConfigs.seedPlantRate.get());

		TreeGenerator treeGen = TreeGenerator.getTreeGenerator();
		if(DTConfigs.seedOnlyForest.get() && treeGen != null) {
			plantChance *= treeGen.getBiomeDataBase(world).getForestness(world.getBiome(pos));
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
			if(nbtData.hasUniqueId("lifespan")) {
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
		//Handle Flower Pot interaction
		BlockState emptyPotState = context.getWorld().getBlockState(context.getPos());
		if(emptyPotState.getBlock() instanceof FlowerPotBlock && (emptyPotState == emptyPotState.getBlock().getDefaultState()) ) { //Empty Flower Pot of some kind
			Species species = getSpecies(context.getItem());
			BlockBonsaiPot bonzaiPot = species.getBonzaiPot();
			context.getWorld().setBlockState(context.getPos(), bonzaiPot.getDefaultState());
			if(bonzaiPot.setSpecies(context.getWorld(), species, context.getPos()) && bonzaiPot.setPotState(context.getWorld(), emptyPotState, context.getPos())) {
				context.getItem().shrink(1);
				return ActionResultType.SUCCESS;
			}
		}

		return ActionResultType.PASS;
	}

	public ActionResultType onItemUsePlantSeed(ItemUseContext context) {

		BlockState iblockstate = context.getWorld().getBlockState(context.getPos());
		Block block = iblockstate.getBlock();
		BlockPos pos = context.getPos();
		Direction facing = context.getFace();
		if(block.canBeReplacedByLeaves(context.getWorld().getBlockState(pos), context.getWorld(), pos)) {
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
	public ActionResultType onItemUse(ItemUseContext context) {
		ItemStack seedStack = Objects.requireNonNull(context.getPlayer()).getHeldItem(context.getHand());

		//Handle Flower Pot interaction
		if(onItemUseFlowerPot(context) == ActionResultType.SUCCESS) {
			return ActionResultType.SUCCESS;
		}

		//Handle Planting Seed interaction
		if(onItemUsePlantSeed(context) == ActionResultType.SUCCESS) {
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.FAIL;
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
			if(nbtData.hasUniqueId("lifespan")) {
				tooltip.add(new StringTextComponent("Seed Life Span: §3" + nbtData.getInt("lifespan")));
			}
		}
	}

}
