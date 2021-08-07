package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.Chance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class TreeGenerator {

    protected static TreeGenerator INSTANCE;

    protected final UniversalPoissonDiscProvider circleProvider;
    protected final RandomXOR random = new RandomXOR();

    public static void setup() {
        new TreeGenerator();
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

    }

    public UniversalPoissonDiscProvider getCircleProvider() {
        return circleProvider;
    }

    public void makeConcreteCircle(IWorld world, PoissonDisc circle, int h, GeneratorResult resultType, SafeChunkBounds safeBounds) {
        makeConcreteCircle(world, circle, h, resultType, safeBounds, 0);
    }

    private BlockState getConcreteByColor(DyeColor color) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(color + "_concrete"))).defaultBlockState();
    }

    public void makeConcreteCircle(IWorld world, PoissonDisc circle, int h, GeneratorResult resultType, SafeChunkBounds safeBounds, int flags) {
        for (int ix = -circle.radius; ix <= circle.radius; ix++) {
            for (int iz = -circle.radius; iz <= circle.radius; iz++) {
                if (circle.isEdge(circle.x + ix, circle.z + iz)) {
                    safeBounds.setBlockState(world, new BlockPos(circle.x + ix, h, circle.z + iz), this.getConcreteByColor(DyeColor.byId((circle.x ^ circle.z) & 0xF)), flags, true);
                }
            }
        }

        if (resultType != GeneratorResult.GENERATED) {
            final BlockPos pos = new BlockPos(circle.x, h, circle.z);
            final DyeColor color = resultType.getColor();
            safeBounds.setBlockState(world, pos, this.getConcreteByColor(color), true);
            safeBounds.setBlockState(world, pos.above(), this.getConcreteByColor(color), true);
        }
    }

    public void makeTrees(ISeedReader world, BiomeDatabase biomeDataBase, PoissonDisc circle, IGroundFinder groundFinder, SafeChunkBounds safeBounds) {
        circle.add(8, 8); // Move the circle into the "stage".
        BlockPos pos = new BlockPos(circle.x, 0, circle.z);
        final Entry entry = biomeDataBase.getEntry(world.getBiome(pos));
        for (BlockPos groundPos : groundFinder.findGround(entry, world, pos)) {
            makeTree(world, entry, circle, groundPos, safeBounds);
        }
        circle.sub(8, 8); // Move the circle back to normal coords.
    }

    public GeneratorResult makeTree(ISeedReader world, BiomeDatabase.Entry biomeEntry, PoissonDisc circle, BlockPos groundPos, SafeChunkBounds safeBounds) {

        final Biome biome = world.getBiome(groundPos);

        if (biomeEntry.isBlacklisted()) {
            return GeneratorResult.UNHANDLED_BIOME;
        }

        if (groundPos == BlockPos.ZERO) {
            return GeneratorResult.NO_GROUND;
        }

        random.setXOR(groundPos);

        final BlockState dirtState = world.getBlockState(groundPos);

        GeneratorResult result = GeneratorResult.GENERATED;

        final BiomePropertySelectors.ISpeciesSelector speciesSelector = biomeEntry.getSpeciesSelector();
        final SpeciesSelection speciesSelection = speciesSelector.getSpecies(groundPos, dirtState, random);

        if (speciesSelection.isHandled()) {
            final Species species = speciesSelection.getSpecies();
            if (species.isValid()) {
                if (species.isAcceptableSoilForWorldgen(world, groundPos, dirtState)) {
                    if (biomeEntry.getChanceSelector().getChance(random, species, circle.radius) == Chance.OK) {
                        if (!species.generate(world.getLevel(), world, groundPos, biome, random, circle.radius, safeBounds)) {
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
            this.makeConcreteCircle(world, circle, groundPos.getY(), result, safeBounds);
        }

        return result;
    }

}
