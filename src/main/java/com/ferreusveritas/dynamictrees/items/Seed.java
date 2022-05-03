package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.event.SeedVoluntaryPlantEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabases;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
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
        if (entityItem.lifespan == 6000) { // 6000 (5 minutes) is the default lifespan for an entity item
            entityItem.lifespan = getTimeToLive(entityItem.getItem()) + 20; // override default lifespan with new value + 20 ticks (1 second)
            if (entityItem.lifespan == 6000) {
                entityItem.lifespan = 6001; // Ensure this isn't run again
            }
        }

        if (entityItem.tickCount >= entityItem.lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
            final Level world = entityItem.level;
            if (!world.isClientSide) {//Server side only
                final ItemStack seedStack = entityItem.getItem();
                final BlockPos pos = new BlockPos(entityItem.blockPosition());
                final SeedVoluntaryPlantEvent seedVolEvent = new SeedVoluntaryPlantEvent(entityItem, this.getSpecies().selfOrLocationOverride(world, pos), pos, this.shouldPlant(world, pos, seedStack));
                MinecraftForge.EVENT_BUS.post(seedVolEvent);
                if (!seedVolEvent.isCanceled() && seedVolEvent.getWillPlant()) {
                    this.doPlanting(world, pos, null, seedStack);
                }
                seedStack.setCount(0);
            }
            entityItem.kill();
        }

        return false;
    }

    public boolean doPlanting(Level world, BlockPos pos, @Nullable Player planter, ItemStack seedStack) {
        final Species species = this.getSpecies().selfOrLocationOverride(world, pos);
        if (species.plantSapling(world, pos, this.getSpecies() != species)) { // Do the planting
            String joCode = getCode(seedStack);
            if (!joCode.isEmpty()) {
                world.removeBlock(pos, false); // Remove the newly created dynamic sapling
                species.getJoCode(joCode).setCareful(true).generate(world, world, species, pos.below(), world.getBiome(pos), planter != null ? planter.getDirection() : Direction.NORTH, 8, SafeChunkBounds.ANY, false);
            }
            return true;
        }
        return false;
    }

    public boolean shouldPlant(Level world, BlockPos pos, ItemStack seedStack) {

        if (hasForcePlant(seedStack)) {
            return true;
        }

        if (!world.canSeeSkyFromBelowWater(pos)) {
            return false;
        }

        float plantChance = (float) (getSpecies().biomeSuitability(world, pos) * DTConfigs.SEED_PLANT_RATE.get());

        if (DTConfigs.SEED_ONLY_FOREST.get()) {
            plantChance *= BiomeDatabases.getDimensionalOrDefault(world.dimension().location())
                    .getForestness(world.getBiome(pos));
        }

        float accum = 1.0f;
        int count = seedStack.getCount();
        while (count-- > 0) {
            accum *= 1.0f - plantChance;
        }
        plantChance = 1.0f - accum;

        return plantChance > world.random.nextFloat();
    }

    public boolean hasForcePlant(ItemStack seedStack) {
        boolean forcePlant = false;
        if (seedStack.hasTag()) {
            CompoundTag nbtData = seedStack.getTag();
            assert nbtData != null;
            forcePlant = nbtData.getBoolean("forceplant");
        }
        return forcePlant;
    }

    public int getTimeToLive(ItemStack seedStack) {
        int lifespan = DTConfigs.SEED_TIME_TO_LIVE.get();//1 minute by default(helps with lag)
        if (seedStack.hasTag()) {
            CompoundTag nbtData = seedStack.getTag();
            assert nbtData != null;
            if (nbtData.contains("lifespan")) {
                lifespan = nbtData.getInt("lifespan");
            }
        }
        return lifespan;
    }

    public String getCode(ItemStack seedStack) {
        String joCode = "";
        if (seedStack.hasTag()) {
            CompoundTag nbtData = seedStack.getTag();
            assert nbtData != null;
            joCode = nbtData.getString("code");
        }
        return joCode;
    }

    public InteractionResult onItemUseFlowerPot(UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState emptyPotState = world.getBlockState(pos);
        final Block emptyPotBlock = emptyPotState.getBlock();

        if (!(emptyPotBlock instanceof FlowerPotBlock) || emptyPotState != emptyPotBlock.defaultBlockState() ||
                ((FlowerPotBlock) emptyPotBlock).getContent() != Blocks.AIR) {
            return InteractionResult.PASS;
        }

        final PottedSaplingBlock pottingSapling = this.getSpecies().getPottedSapling();
        world.setBlockAndUpdate(pos, pottingSapling.defaultBlockState());

        if (pottingSapling.setSpecies(world, pos, pottingSapling.defaultBlockState(), this.getSpecies()) && pottingSapling.setPotState(world, emptyPotState, pos)) {
            final Player player = context.getPlayer();

            if (player != null) {
                context.getPlayer().awardStat(Stats.POT_FLOWER);
                if (!context.getPlayer().abilities.instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public InteractionResult onItemUsePlantSeed(UseOnContext context) {

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        if (state.getMaterial().isReplaceable()) {
            pos = pos.below();
            facing = Direction.UP;
        }

        if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
            if (context.getPlayer() != null && context.getPlayer().mayUseItemAt(pos, facing, context.getItemInHand()) && context.getPlayer().mayUseItemAt(pos.above(), facing, context.getItemInHand())) {//Ensure permissions to edit block
                if (doPlanting(context.getLevel(), pos.above(), context.getPlayer(), context.getItemInHand())) {
                    context.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // Handle flower pot interaction (flower pot cancels on item use so this must be done first).
        if (onItemUseFlowerPot(context) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Handle planting seed interaction.
        if (onItemUsePlantSeed(context) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static final String LIFESPAN_TAG = "lifespan";

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, world, tooltip, flagIn);

        if (stack.hasTag()) {
            final String joCode = this.getCode(stack);
            if (!joCode.isEmpty()) {
                tooltip.add(new TranslatableComponent("tooltip.dynamictrees.jo_code", new JoCode(joCode).getTextComponent()));
            }
            if (this.hasForcePlant(stack)) {
                tooltip.add(new TranslatableComponent("tooltip.dynamictrees.force_planting",
                        new TranslatableComponent("tooltip.dynamictrees.enabled")
                                .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)))
                );
            }
            final CompoundTag nbtData = stack.getTag();
            assert nbtData != null;

            if (nbtData.contains(LIFESPAN_TAG)) {
                tooltip.add(new TranslatableComponent("tooltip.dynamictrees.seed_life_span" +
                        new TextComponent(String.valueOf(nbtData.getInt(LIFESPAN_TAG)))
                                .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)))
                );
            }
        }
    }


    ///////////////////////////////////////////
    //IPlantable Interface
    ///////////////////////////////////////////

    @Override
    public BlockState getPlant(BlockGetter world, BlockPos pos) {
        return getSpecies().getSapling().map(Block::defaultBlockState).orElse(Blocks.AIR.defaultBlockState());
    }

}
