package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.event.SeedVoluntaryPlantEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.LazyValue;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabases;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeRegistry;
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
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
import java.util.Random;

public class Seed extends Item implements IPlantable {

    private static final LazyValue<Random> BACKUP_RANDOM = LazyValue.supplied(Random::new);

    /**
     * If set to {@code true} in the item stack tag, forces the tree to be planted before despawning.
     */
    public static final String FORCE_PLANT_KEY = "forceplant";
    public static final String LIFESPAN_KEY = "lifespan";
    /**
     * If set in the item stack tag, generates the tree with the corresponding {@link JoCode} if the seed is planted.
     */
    public static final String CODE_KEY = "code";
    /**
     * If set in the item stack tag, selects a random code of the corresponding radius to generate the tree with if the
     * seed is planted.
     */
    public static final String CODE_RADIUS_KEY = "CodeRadius";

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
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return getTimeToLive(itemStack) + 20; // override default lifespan with new value + 20 ticks (1 second)
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        if (entityItem.tickCount >= entityItem.lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
            final World world = entityItem.level;
            if (!world.isClientSide) {//Server side only
                final ItemStack seedStack = entityItem.getItem();
                final BlockPos pos = new BlockPos(entityItem.blockPosition());
                final SeedVoluntaryPlantEvent seedVolEvent =
                        new SeedVoluntaryPlantEvent(entityItem, this.getSpecies().selfOrLocationOverride(world, pos),
                                pos, this.shouldPlant(world, pos, seedStack));
                MinecraftForge.EVENT_BUS.post(seedVolEvent);
                if (!seedVolEvent.isCanceled() && seedVolEvent.getWillPlant()) {
                    this.doPlanting(world, pos, null, seedStack);
                }
                seedStack.setCount(0);
            }
            entityItem.kill();
        }

        return false;
        //posX = 96347
        //posY = 4
        //posZ = 197
        //BlockPos{x=96346, y=4, z=196}
    }

    public boolean doPlanting(World world, BlockPos pos, @Nullable PlayerEntity planter, ItemStack seedStack) {
        final Species species = this.getSpecies().selfOrLocationOverride(world, pos);
        if (species.plantSapling(world, pos, this.getSpecies() != species)) { // Do the planting
            String joCode = getCode(seedStack, world.random);
            if (!joCode.isEmpty()) {
                world.removeBlock(pos, false); // Remove the newly created dynamic sapling
                species.getJoCode(joCode).setCareful(true)
                        .generate(WorldContext.create(world), species, pos.below(), world.getBiome(pos),
                                planter != null ? planter.getDirection() : Direction.NORTH, 8, SafeChunkBounds.ANY,
                                false);
            }
            return true;
        }
        return false;
    }

    public boolean shouldPlant(World world, BlockPos pos, ItemStack seedStack) {

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
            CompoundNBT nbtData = seedStack.getTag();
            assert nbtData != null;
            forcePlant = nbtData.getBoolean(FORCE_PLANT_KEY);
        }
        return forcePlant;
    }

    public int getTimeToLive(ItemStack seedStack) {
        int lifespan = DTConfigs.SEED_TIME_TO_LIVE.get();//1 minute by default(helps with lag)
        if (seedStack.hasTag()) {
            CompoundNBT nbtData = seedStack.getTag();
            assert nbtData != null;
            if (nbtData.contains(LIFESPAN_KEY)) {
                lifespan = nbtData.getInt(LIFESPAN_KEY);
            }
        }
        return lifespan;
    }

    public String getCode(ItemStack seedStack, Random random) {
        String joCode = "";
        if (seedStack.hasTag()) {
            CompoundNBT nbtData = seedStack.getTag();
            assert nbtData != null;
            if (nbtData.contains(CODE_KEY)) {
                joCode = nbtData.getString(CODE_KEY);
            } else if (nbtData.contains(CODE_RADIUS_KEY)) {
                joCode = String.valueOf(JoCodeRegistry.getRandomCode(species.getRegistryName(),
                        MathHelper.clamp(nbtData.getInt(CODE_RADIUS_KEY), 2, 8), random));
            }
        }
        return joCode;
    }

    public ActionResultType onItemUseFlowerPot(ItemUseContext context) {
        final World world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState emptyPotState = world.getBlockState(pos);
        final Block emptyPotBlock = emptyPotState.getBlock();

        if (!(emptyPotBlock instanceof FlowerPotBlock) || emptyPotState != emptyPotBlock.defaultBlockState() ||
                ((FlowerPotBlock) emptyPotBlock).getContent() != Blocks.AIR) {
            return ActionResultType.PASS;
        }

        final PottedSaplingBlock pottingSapling = this.getSpecies().getPottedSapling();
        world.setBlockAndUpdate(pos, pottingSapling.defaultBlockState());

        if (pottingSapling.setSpecies(world, pos, pottingSapling.defaultBlockState(), this.getSpecies()) &&
                pottingSapling.setPotState(world, emptyPotState, pos)) {
            final PlayerEntity player = context.getPlayer();

            if (player != null) {
                context.getPlayer().awardStat(Stats.POT_FLOWER);
                if (!context.getPlayer().abilities.instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    public ActionResultType onItemUsePlantSeed(ItemUseContext context) {

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        if (state.getMaterial().isReplaceable()) {
            pos = pos.below();
            facing = Direction.UP;
        }

        if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
            if (context.getPlayer() != null && context.getPlayer().mayUseItemAt(pos, facing, context.getItemInHand()) &&
                    context.getPlayer().mayUseItemAt(pos.above(), facing,
                            context.getItemInHand())) {//Ensure permissions to edit block
                if (doPlanting(context.getLevel(), pos.above(), context.getPlayer(), context.getItemInHand())) {
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
        if (onItemUseFlowerPot(context) == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        // Handle planting seed interaction.
        if (onItemUsePlantSeed(context) == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip,
                                ITooltipFlag flagIn) {
        super.appendHoverText(stack, world, tooltip, flagIn);

        if (stack.hasTag()) {
            final String joCode = this.getCode(stack, world == null ? BACKUP_RANDOM.get() : world.random);
            if (!joCode.isEmpty()) {
                tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.jo_code",
                        new JoCode(joCode).getTextComponent()));
            }
            if (this.hasForcePlant(stack)) {
                tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.force_planting",
                        new TranslationTextComponent("tooltip.dynamictrees.enabled")
                                .withStyle(style -> style.withColor(TextFormatting.DARK_AQUA)))
                );
            }
            final CompoundNBT nbtData = stack.getTag();
            assert nbtData != null;

            if (nbtData.contains(LIFESPAN_KEY)) {
                tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.seed_life_span" +
                        new StringTextComponent(String.valueOf(nbtData.getInt(LIFESPAN_KEY)))
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
