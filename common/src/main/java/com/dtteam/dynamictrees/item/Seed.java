package com.dtteam.dynamictrees.item;

import com.dtteam.dynamictrees.api.lazyvalue.LazyValue;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.*;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.dtteam.dynamictrees.worldgen.JoCodeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Seed extends Item {//implements IPlantable {

    private static final LazyValue<RandomSource> BACKUP_RANDOM = LazyValue.supplied(RandomSource::create);

    /**
     * If set to {@code true} in the item stack tag, forces the tree to be planted before despawning.
     */
    public static final String FORCE_PLANT_KEY = "ForcePlant";
    public static final String LIFESPAN_KEY = "Lifespan";
    /**
     * If set in the item stack tag, generates the tree with the corresponding {@link JoCode} if the seed is planted.
     * If set as an integer, selects a random code of the corresponding radius to generate the tree with if the seed is
     * planted.
     */
    public static final String CODE_KEY = "Code";

    private final Species species;//The tree this seed creates

    //This constructor is only used for the null registration
    public Seed() {
        super(new Properties());
        this.species = Species.NULL_SPECIES;
    }

    public Seed(Species species) {
        this(species, new Properties());
    }

    public Seed(@NotNull Species species, Properties properties) {
        super(properties);
        this.species = species;
        DTRegistries.CREATIVE_TAB_ITEMS.add(this);
    }

    @NotNull
    public Species getSpecies() {
        return species;
    }

    /** NeoForge Override */
    @SuppressWarnings("unused")
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        int lifespan = Services.INTERACTION.setSeedItemEntityLifespan(entityItem, this);

        if (entityItem.tickCount >= lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
            final Level level = entityItem.level();
            if (!level.isClientSide()) {//Server side only
                final ItemStack seedStack = entityItem.getItem();
                final BlockPos pos = new BlockPos(entityItem.blockPosition());
                VoluntaryPlantEventResult result = Services.EVENT.postSeedVoluntaryPlantEvent(entityItem, this.getSpecies().selfOrLocationOverride(level, pos), pos, this.shouldPlant(level, pos, seedStack));
                if (!result.cancelled() && result.willPlant()) {
                    this.doPlanting(level, pos, null, seedStack);
                }
                seedStack.setCount(0);
            }
            entityItem.discard();
        }

        return false;
    }

    public record VoluntaryPlantEventResult(boolean cancelled, boolean willPlant) { }

    public boolean doPlanting(Level level, BlockPos pos, @Nullable Player planter, ItemStack seedStack) {
        final Species species = this.getSpecies().selfOrLocationOverride(level, pos);
        if (species.plantSapling(level, pos, this.getSpecies() != species)) { // Do the planting
            String joCode = getCode(seedStack, level.getRandom());
            if (!joCode.isEmpty()) {
                level.removeBlock(pos, false); // Remove the newly created dynamic sapling
                BlockPos rootPos = pos.below();
                DynamicTreeGenerationContext context = new DynamicTreeGenerationContext(LevelContext.create(level), species, rootPos, rootPos.mutable(), level.getBiome(pos), planter != null ? planter.getDirection() : Direction.NORTH, 8, false);
                species.getJoCode(joCode).setCareful(true).generate(context);
            }
            return true;
        }
        return false;
    }

    public boolean shouldPlant(Level level, BlockPos pos, ItemStack seedStack) {

        if (hasForcePlant(seedStack)) {
            return true;
        }

        if (!level.canSeeSkyFromBelowWater(pos)) {
            return false;
        }

        float plantChance = (float) (getSpecies().biomeSuitability(level, pos) * DTConfigs.SERVER.seedPlantRate.get());

        if (DTConfigs.SERVER.seedOnlyForest.get()) {
//            plantChance *= BiomeDatabases.getDimensionalOrDefault(level.dimension().identifier())
//                    .getForestness(level.getBiome(pos));
        }

        float accum = 1.0f;
        int count = seedStack.getCount();
        while (count-- > 0) {
            accum *= 1.0f - plantChance;
        }
        plantChance = 1.0f - accum;

        return plantChance > level.getRandom().nextFloat();
    }

    public boolean hasForcePlant(ItemStack seedStack) {
        boolean forcePlant = false;
//        if (seedStack.hasTag()) {
//            CompoundTag nbtData = seedStack.getTag();
//            assert nbtData != null;
//            forcePlant = nbtData.getBoolean(FORCE_PLANT_KEY);
//        }
        return forcePlant;
    }

    public int getTimeToLive(ItemStack seedStack) {
        int lifespan = DTConfigs.SERVER.seedTimeToLive.get();
//        if (seedStack.hasTag()) {
//            CompoundTag nbtData = seedStack.getTag();
//            assert nbtData != null;
//            if (nbtData.contains(LIFESPAN_KEY)) {
//                lifespan = nbtData.getInt(LIFESPAN_KEY);
//            }
//        }
        return lifespan;
    }

    public String getCode(ItemStack seedStack, RandomSource random) {
        String joCode = "";
//        if (seedStack.hasTag()) {
//            CompoundTag tag = seedStack.getTag();
//            assert tag != null;
//            if (tag.contains(CODE_KEY)) {
//                if (tag.getTagType(CODE_KEY) == Tag.TAG_STRING) {
//                    joCode = tag.getString(CODE_KEY);
//                } else if (tag.getTagType(CODE_KEY) == Tag.TAG_INT) {
//                    final JoCode code = getJoCodeForRadius(random, tag.getInt(CODE_KEY));
//                    if (code != null) {
//                        joCode = code.toString();
//                    }
//                }
//            }
//        }
        return joCode;
    }

    @Nullable
    private JoCode getJoCodeForRadius(RandomSource random, int radius) {
        return JoCodeRegistry.getRandomCode(species.getRegistryName(), Mth.clamp(radius, 2, 8), random);
    }

    public InteractionResult useOn(UseOnContext context) {
        // Handle planting seed on ground

        if (context.getLevel().getFluidState(context.getClickedPos().above()).isEmpty()
                && onItemUsePlantSeed(context, false) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResult use(Level level, Player player, InteractionHand pHand) {
        // Handle planting seed on water
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos fluidPos = blockhitresult.getBlockPos();
        if (getSpecies().selfOrLocationOverride(level, fluidPos).isPlantableOnFluid()){
            ItemStack itemstack = player.getItemInHand(pHand);
            if (blockhitresult.getType() == HitResult.Type.BLOCK && !level.getFluidState(fluidPos).isEmpty() &&
                    level.getFluidState(fluidPos.below()).isEmpty()) {
                if (onItemUsePlantSeed(new UseOnContext(player, pHand, blockhitresult), true) == InteractionResult.SUCCESS) {
                    return InteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
                }
            }
            return InteractionResult.PASS; // pass(itemstack);
        }
        return super.use(level, player, pHand);
    }


    public InteractionResult onItemUsePlantSeed(UseOnContext context, boolean onFluid) {

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockPos pos = context.getClickedPos().above(onFluid?1:0);
        Direction facing = context.getClickedFace();
        if (state.canBeReplaced()) {
            pos = pos.below();
            facing = Direction.UP;
        }

        if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
            if (context.getPlayer() != null && context.getPlayer().mayUseItemAt(pos, facing, context.getItemInHand()) && context.getPlayer().mayUseItemAt(pos.above(), facing, context.getItemInHand())) {//Ensure permissions to edit block
                if (doPlanting(context.getLevel(), pos.above(), context.getPlayer(), context.getItemInHand())) {
                    if (!context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    /** NeoForge Override */
    @SuppressWarnings("unused")
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context){
        // Handle flower pot interaction (flower pot cancels use so this must be done first).
        if (onItemUseFlowerPot(context) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public InteractionResult onItemUseFlowerPot(UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState emptyPotState = level.getBlockState(pos);
        final Block emptyPotBlock = emptyPotState.getBlock();

        if (!(emptyPotBlock instanceof FlowerPotBlock) || emptyPotState != emptyPotBlock.defaultBlockState() ||
                ((FlowerPotBlock) emptyPotBlock).getPotted() != Blocks.AIR) {
            return InteractionResult.PASS;
        }

        final PottedSaplingBlock pottingSapling = this.getSpecies().getPottedSapling();
        level.setBlockAndUpdate(pos, pottingSapling.defaultBlockState());

        if (pottingSapling.setSpecies(level, pos, pottingSapling.defaultBlockState(), this.getSpecies()) && pottingSapling.setPotState(level, emptyPotState, pos)) {
            final Player player = context.getPlayer();

            if (player != null) {
                context.getPlayer().awardStat(Stats.POT_FLOWER);
                if (!context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

////    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
//        super.appendHoverText(stack, level, display, tooltip, flag);
//
//        if (stack.hasTag()) {
//            final String joCode = this.getCode(stack, level == null ? BACKUP_RANDOM.get() : level.getRandom());
//            if (!joCode.isEmpty()) {
//                tooltip.add(Component.translatable("tooltip.dynamictrees.jo_code", new JoCode(joCode).getTextComponent()));
//            }
//            if (this.hasForcePlant(stack)) {
//                tooltip.add(Component.translatable("tooltip.dynamictrees.force_planting",
//                        Component.translatable("tooltip.dynamictrees.enabled")
//                                .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)))
//                );
//            }
//            final CompoundTag nbtData = stack.getTag();
//            assert nbtData != null;
//
//            if (nbtData.contains(LIFESPAN_KEY)) {
//                tooltip.add(Component.translatable("tooltip.dynamictrees.seed_life_span" +
//                        Component.literal(String.valueOf(nbtData.getInt(LIFESPAN_KEY)))
//                                .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)))
//                );
//            }
//        }
//    }

    ///////////////////////////////////////////
    //IPlantable Interface
    ///////////////////////////////////////////

////    public BlockState getPlant(BlockGetter level, BlockPos pos) {
//        return getSpecies().getSapling().map(Block::defaultBlockState).orElse(Blocks.AIR.defaultBlockState());
//    }

}
