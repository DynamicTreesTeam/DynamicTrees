package com.dtteam.dynamictrees.worldgen.feature;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.poissondisc.PoissonDisc;
import com.dtteam.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.CoordUtils;
import com.dtteam.dynamictrees.util.LevelContext;
import com.dtteam.dynamictrees.util.RandomXOR;
import com.dtteam.dynamictrees.worldgen.BiomeDatabase;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Arrays;

/**
 * @author Harley O'Connor
 */
public class DynamicTreeFeature extends Feature<NoneFeatureConfiguration> {

    public static final UniversalPoissonDiscProvider DISC_PROVIDER = new UniversalPoissonDiscProvider();
    protected static final RandomXOR RANDOM = new RandomXOR();

    private static Block[] concreteBlocks;

    public static void setup() {
        concreteBlocks = Arrays.stream(DyeColor.values())
                .map(color -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(color.getName() + "_concrete")))
                .toArray(Block[]::new);
    }

    public DynamicTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        LevelContext levelContext = LevelContext.create(context.level());

        if (BiomeDatabases.isBlacklisted(levelContext.dimensionName())) {
            return false;
        }

        BiomeDatabase biomeDatabase = BiomeDatabases.getDimensionalOrDefault(levelContext.dimensionName());
        ChunkPos chunkPos = context.level().getChunk(context.origin()).getPos();

        DISC_PROVIDER.getPoissonDiscs(levelContext, chunkPos).forEach(disc ->
                generateTrees(levelContext, biomeDatabase, disc, context.origin())
        );

        return true;
    }

    public static boolean isFoliage(LevelSimulatedReader pLevel, BlockPos pPos) {
        return pLevel.isStateAtPosition(pPos, (state) -> state.is(DTBlockTags.FOLIAGE));
    }

    protected void generateTrees(LevelContext levelContext, BiomeDatabase biomeDatabase, PoissonDisc disc, BlockPos originPos) {
        BlockPos basePos = new BlockPos(disc.x, 0, disc.z);
        Holder<Biome> biome = levelContext.level().getBiome(basePos);
        Heightmap.Types heightmap = Heightmap.Types.valueOf(biomeDatabase.getHeightmap(biome).toUpperCase());
        for (BlockPos groundPos : GroundFinder.getGroundFinder(levelContext.level()).findGround(levelContext.accessor(), basePos, heightmap)) {
            BiomeDatabase.EntryReader entry = biomeDatabase.getEntry(levelContext.level().getBiome(groundPos));
            generateTree(levelContext, entry, disc, originPos, groundPos);
        }
    }

    public static boolean validTreePos(LevelSimulatedReader pLevel, BlockPos pPos) {
        return pLevel.isStateAtPosition(pPos, (state) ->
                state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(DTBlockTags.FOLIAGE));
    }
    
    protected GeneratorResult generateTree(LevelContext levelContext, BiomeDatabase.EntryReader biomeEntry, PoissonDisc circle, BlockPos originPos, BlockPos groundPos) {
        if (groundPos == BlockPos.ZERO) {
            return GeneratorResult.NO_GROUND;
        }

        // If there is already a rooty block, a cave rooted tree has taken this disc, so ignore
        if (levelContext.accessor().getBlockState(groundPos).getBlock() instanceof SoilBlock) {
            return GeneratorResult.ALREADY_GENERATED;
        }

        RANDOM.setXOR(groundPos);

        BlockState dirtState = levelContext.accessor().getBlockState(groundPos);

        GeneratorResult result = GeneratorResult.GENERATED;

        BiomePropertySelectors.SpeciesSelector speciesSelector = biomeEntry.getSpeciesSelector();
        BiomePropertySelectors.SpeciesSelection speciesSelection = speciesSelector.getSpecies(groundPos, dirtState, RANDOM);

        if (!biomeEntry.isBlacklisted() && speciesSelection.isHandled()) {
            Species species = speciesSelection.getSpecies();
            if (species.isValid()) {
                if (species.isAcceptableSoilForWorldgen(levelContext.accessor(), groundPos, dirtState)) {
                    if (biomeEntry.getChanceSelector().getChance(RANDOM, species, circle.radius) == BiomePropertySelectors.Chance.OK) {
                        Holder<Biome> biome = levelContext.level().getBiome(groundPos);
                        if (!species.generate(new DynamicTreeGenerationContext(levelContext, species, originPos, groundPos.mutable(), biome, CoordUtils.getRandomDir(RANDOM), circle.radius, true))) {
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
        if (Services.CONFIG.getBoolConfig(IConfigHelper.DEBUG)) {
            this.generateConcreteCircle(levelContext.accessor(), circle, groundPos.getY(), result);
        }

        return result;
    }

    private void generateConcreteCircle(LevelAccessor level, PoissonDisc circle, int h, GeneratorResult resultType) {
        for (int ix = -circle.radius; ix <= circle.radius; ix++) {
            for (int iz = -circle.radius; iz <= circle.radius; iz++) {
                if (circle.isEdge(circle.x + ix, circle.z + iz)) {
                    level.setBlock(new BlockPos(circle.x + ix, h, circle.z + iz), concreteBlocks[(circle.x ^ circle.z) & 0xF].defaultBlockState(), 0);
                }
            }
        }

        if (resultType != GeneratorResult.GENERATED) {
            BlockPos pos = new BlockPos(circle.x, h, circle.z);
            level.setBlock(pos, resultType.getColoredBlock(), 0);
            level.setBlock(pos.above(), resultType.getColoredBlock(), 0);
        }
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
        NO_GROUND(DyeColor.PURPLE),
        ALREADY_GENERATED(DyeColor.GRAY);

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
}
