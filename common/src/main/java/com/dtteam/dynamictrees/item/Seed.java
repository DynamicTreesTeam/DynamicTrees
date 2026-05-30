package com.dtteam.dynamictrees.item;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.client.Tooltips;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.season.ClimateHelper;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
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

import java.util.List;

public class Seed extends Item {

    private final Species species;//The tree this seed creates

    //This constructor is only used for the null registration
    public Seed() {
        super(new Properties().setId(ResourceKey.create(Registries.ITEM, DynamicTrees.NULL)));
        this.species = Species.NULL_SPECIES;
    }

    public Seed(Identifier id, Species species) {
        this(species, new Properties().setId(ResourceKey.create(Registries.ITEM, id)));
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
            plantChance *= BiomeDatabases.getDimensionalOrDefault(level.dimension().identifier())
                    .getForestness(level.getBiome(pos));
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
        if (seedStack.has(DTRegistries.FORCE_PLANT_COMPONENT.get())) {
            Boolean value = seedStack.get(DTRegistries.FORCE_PLANT_COMPONENT.get());
            if (value != null) forcePlant = value;
        }
        return forcePlant;
    }

    public int getTimeToLive(ItemStack seedStack) {
        int lifespan = DTConfigs.SERVER.seedTimeToLive.get();
        if (seedStack.has(DTRegistries.LIFESPAN_COMPONENT.get())) {
            Integer value = seedStack.get(DTRegistries.LIFESPAN_COMPONENT.get());
            if (value != null) lifespan = value;
        }
        return lifespan;
    }

    public String getCode(ItemStack itemStack, RandomSource random) {
        String code = "";
        if (itemStack.has(DTRegistries.JOCODE_DATA_COMPONENT.get())) {
            String newCode = itemStack.get(DTRegistries.JOCODE_DATA_COMPONENT.get());
            if (newCode != null) code = newCode;
        }
        return code;
    }

//    @Nullable
//    private JoCode getJoCodeForRadius(RandomSource random, int radius) {
//        return JoCodeRegistry.getRandomCode(species.getRegistryName(), Mth.clamp(radius, 2, 8), random);
//    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Handle planting seed on ground
        if (context.getLevel().getFluidState(context.getClickedPos().above()).isEmpty()
                && onItemUsePlantSeed(context, false) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos fluidPos = blockhitresult.getBlockPos();
        if (getSpecies().selfOrLocationOverride(level, fluidPos).isPlantableOnFluid()){
            if (blockhitresult.getType() == HitResult.Type.BLOCK && !level.getFluidState(fluidPos).isEmpty() &&
                    level.getFluidState(fluidPos.below()).isEmpty()) {
                if (onItemUsePlantSeed(new UseOnContext(player, hand, blockhitresult), true) == InteractionResult.SUCCESS) {
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
        return super.use(level, player, hand);
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

    public void appendHoverText(ItemStack stack, LevelContext levelContext, List<Component> tooltip, Player player) {
        addSeasonTooltip(levelContext, tooltip, player);
        addComponentTooltips(stack, levelContext, tooltip);
    }

    protected void addComponentTooltips(ItemStack stack, LevelContext levelContext, List<Component> tooltip) {
        if (stack.has(DTRegistries.JOCODE_DATA_COMPONENT.get())) {
            String joCode = this.getCode(stack, levelContext.accessor().getRandom());
            tooltip.add(Component.translatable("tooltip.dynamictrees.jo_code", new JoCode(joCode).getTextComponent()));
        }

        if (this.hasForcePlant(stack)) {
            tooltip.add(Component.translatable("tooltip.dynamictrees.force_planting",
                    Component.translatable("tooltip.dynamictrees.enabled")
                            .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)))
            );
        }

        if (stack.has(DTRegistries.LIFESPAN_COMPONENT.get())) {
            int lifeSpan = getTimeToLive(stack);
            tooltip.add(Component.translatable("tooltip.dynamictrees.seed_life_span", Component.literal(String.valueOf(lifeSpan))
                            .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)))
            );
        }
    }

    protected void addSeasonTooltip(LevelContext levelContext, List<Component> tooltip, Player player) {
        if (SeasonHelper.getSeasonValue(levelContext, BlockPos.ZERO) == null || !species.isValid())
            return;

        BlockPos playerPos = BlockPos.containing(player.position());
        ClimateZoneType climate = ClimateHelper.getClimate(player.level(), playerPos);
        int flags = species.getSeasonalTooltipFlags(levelContext, player);
        Tooltips.applySeasonalTooltips(tooltip, flags, climate);
    }

}
