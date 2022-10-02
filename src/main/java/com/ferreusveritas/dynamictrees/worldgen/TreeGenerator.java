package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.Chance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

public class TreeGenerator {

    /**
     * Array of colored concrete blocks, indexed by {@link DyeColor} IDs.
     */
    private static Block[] concreteBlocks;

    protected static TreeGenerator INSTANCE;

    protected final UniversalPoissonDiscProvider circleProvider;
    protected final RandomXOR random = new RandomXOR();

    public static void initialise() {
        new TreeGenerator();
    }

    public static void setup() {
        concreteBlocks = Arrays.stream(DyeColor.values())
                .map(color -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(color.getName() + "_concrete")))
                .toArray(Block[]::new);
    }

    public static TreeGenerator getTreeGenerator() {
        return INSTANCE;
    }

    public TreeGenerator() {
        INSTANCE = this; // Set this here in case the lines in the contructor lead to calls that use getTreeGenerator
        this.circleProvider = new UniversalPoissonDiscProvider();
    }

    /**
     * This is for world gen debugging. The colors signify the different tree spawn failure modes.
     */
    public enum GeneratorResult {
        GENERATED(DyeColor.WHITE),
        NO_TREE(DyeColor.BLACK),
        UNHANDLED_BIOME(DyeColor.YELLOW),
        FAIL_SOIL(DyeColor.BROWN),
        FAIL_CHANCE(DyeColor.BLUE),
        FAIL_GENERATION(DyeColor.RED),
        NO_GROUND(DyeColor.PURPLE);

        private final DyeColor color;

        GeneratorResult(DyeColor color) {
            this.color = color;
        }

        public DyeColor getColor() {
            return this.color;
        }

        public BlockState getColoredBlock() {
            return concreteBlocks[color.getId()].defaultBlockState();
        }

    }

    public UniversalPoissonDiscProvider getCircleProvider() {
        return circleProvider;
    }

    public void makeConcreteCircle(LevelAccessor world, PoissonDisc circle, int h, GeneratorResult resultType, SafeChunkBounds safeBounds) {
        makeConcreteCircle(world, circle, h, resultType, safeBounds, 0);
    }

    public void makeConcreteCircle(LevelAccessor world, PoissonDisc circle, int h, GeneratorResult resultType, SafeChunkBounds safeBounds, int flags) {
        for (int ix = -circle.radius; ix <= circle.radius; ix++) {
            for (int iz = -circle.radius; iz <= circle.radius; iz++) {
                if (circle.isEdge(circle.x + ix, circle.z + iz)) {
                    safeBounds.setBlockState(world, new BlockPos(circle.x + ix, h, circle.z + iz), concreteBlocks[(circle.x ^ circle.z) & 0xF].defaultBlockState(), flags, true);
                }
            }
        }

        if (resultType != GeneratorResult.GENERATED) {
            final BlockPos pos = new BlockPos(circle.x, h, circle.z);
            safeBounds.setBlockState(world, pos, resultType.getColoredBlock(), true);
            safeBounds.setBlockState(world, pos.above(), resultType.getColoredBlock(), true);
        }
    }

    public void makeTrees(LevelContext levelContext, BiomeDatabase biomeDataBase, PoissonDisc circle, SafeChunkBounds safeBounds) {
        // TODO: De-couple ground finder from biomes, now that they can vary based on height.
        BlockPos pos = new BlockPos(circle.x, levelContext.access().getMaxBuildHeight(), circle.z);
        final Entry entry = biomeDataBase.getEntry(levelContext.access().getBiome(pos).value());
        for (BlockPos groundPos : entry.getGroundFinder().findGround(levelContext.access(), pos)) {
            makeTree(levelContext, entry, circle, groundPos, safeBounds);
        }
    }

    public GeneratorResult makeTree(LevelContext levelContext, BiomeDatabase.Entry biomeEntry, PoissonDisc circle, BlockPos groundPos, SafeChunkBounds safeBounds) {

        final Biome biome = levelContext.access().getBiome(groundPos).value();

        if (biomeEntry.isBlacklisted()) {
            return GeneratorResult.UNHANDLED_BIOME;
        }

        if (groundPos == BlockPos.ZERO) {
            return GeneratorResult.NO_GROUND;
        }

        random.setXOR(groundPos);

        BlockState dirtState = levelContext.access().getBlockState(groundPos);

        GeneratorResult result = GeneratorResult.GENERATED;

        final BiomePropertySelectors.SpeciesSelector speciesSelector = biomeEntry.getSpeciesSelector();
        final SpeciesSelection speciesSelection = speciesSelector.getSpecies(groundPos, dirtState, random);

        if (speciesSelection.isHandled()) {
            final Species species = speciesSelection.getSpecies();
            if (species.isValid()) {
                if (species.isAcceptableSoilForWorldgen(levelContext.access(), groundPos, dirtState)) {
                    if (biomeEntry.getChanceSelector().getChance(random, species, circle.radius) == Chance.OK) {
                        if (!species.generate(levelContext, groundPos, biome, random, circle.radius, safeBounds)) {
                            result = GeneratorResult.FAIL_GENERATION;
                        }
                    } else {
                        result = GeneratorResult.FAIL_CHANCE;
                    }
                } else {
                    result = GeneratorResult.FAIL_SOIL;
                }
            } else {
                result = GeneratorResult.NO_TREE;
            }
        } else {
            result = GeneratorResult.UNHANDLED_BIOME;
        }

        // Display concrete circles for testing the circle growing algorithm.
        if (DTConfigs.WORLD_GEN_DEBUG.get()) {
            this.makeConcreteCircle(levelContext.access(), circle, groundPos.getY(), result, safeBounds);
        }

        return result;
    }

}
