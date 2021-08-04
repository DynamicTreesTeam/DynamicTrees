package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.Random;

import static net.minecraft.block.HugeMushroomBlock.*;

/**
 * Generates a singular huge mushroom
 *
 * @author ferreusveritas
 */
public class HugeMushroomGenFeature extends GenFeature implements IFullGenFeature {

    public static final ConfigurationProperty<Block> MUSHROOM_BLOCK = ConfigurationProperty.block("mushroom");
    public static final ConfigurationProperty<Block> STEM_BLOCK = ConfigurationProperty.block("stem");

    private int height = -1;

    public HugeMushroomGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MUSHROOM_BLOCK, STEM_BLOCK);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK)
                .with(STEM_BLOCK, Blocks.MUSHROOM_STEM);
    }

    static final SimpleVoxmap BROWN_CAP;
    static final SimpleVoxmap BROWN_CAP_MEDIUM;
    static final SimpleVoxmap BROWN_CAP_SMALL;
    static final SimpleVoxmap RED_CAP;
    static final SimpleVoxmap RED_CAP_SHORT;
    static final SimpleVoxmap RED_CAP_SMALL;

    static {
        BROWN_CAP = new SimpleVoxmap(7, 1, 7, new byte[]{
                0, 1, 2, 2, 2, 3, 0,
                1, 5, 5, 5, 5, 5, 3,
                4, 5, 5, 5, 5, 5, 6,
                4, 5, 5, 5, 5, 5, 6,
                4, 5, 5, 5, 5, 5, 6,
                7, 5, 5, 5, 5, 5, 9,
                0, 7, 8, 8, 8, 9, 0
        }).setCenter(new BlockPos(3, 0, 3));

        BROWN_CAP_MEDIUM = new SimpleVoxmap(5, 1, 5, new byte[]{
                0, 1, 2, 3, 0,
                1, 5, 5, 5, 3,
                4, 5, 5, 5, 6,
                7, 5, 5, 5, 9,
                0, 7, 8, 9, 0
        }).setCenter(new BlockPos(2, 0, 2));

        BROWN_CAP_SMALL = new SimpleVoxmap(3, 1, 3, new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }).setCenter(new BlockPos(1, 0, 1));

        RED_CAP = new SimpleVoxmap(5, 4, 5, new byte[]{
                0, 1, 2, 3, 0,
                1, 0, 0, 0, 3,
                4, 0, 10, 0, 6,
                7, 0, 0, 0, 9,
                0, 7, 8, 9, 0, // Bottom

                0, 1, 2, 3, 0,
                1, 0, 0, 0, 3,
                4, 0, 10, 0, 6,
                7, 0, 0, 0, 9,
                0, 7, 8, 9, 0,

                0, 1, 2, 3, 0,
                1, 0, 0, 0, 3,
                4, 0, 10, 0, 6,
                7, 0, 0, 0, 9,
                0, 7, 8, 9, 0,

                0, 0, 0, 0, 0,
                0, 1, 2, 3, 0,
                0, 4, 5, 6, 0,
                0, 7, 8, 9, 0,
                0, 0, 0, 0, 0 // Top
        }).setCenter(new BlockPos(2, 3, 2));

        RED_CAP_SHORT = new SimpleVoxmap(5, 3, 5, new byte[]{
                0, 1, 2, 3, 0,
                1, 0, 0, 0, 3,
                4, 0, 10, 0, 6,
                7, 0, 0, 0, 9,
                0, 7, 8, 9, 0, // Bottom

                0, 1, 2, 3, 0,
                1, 0, 0, 0, 3,
                4, 0, 10, 0, 6,
                7, 0, 0, 0, 9,
                0, 7, 8, 9, 0,

                0, 0, 0, 0, 0,
                0, 1, 2, 3, 0,
                0, 4, 5, 6, 0,
                0, 7, 8, 9, 0,
                0, 0, 0, 0, 0 // Top
        }).setCenter(new BlockPos(2, 2, 2));

        RED_CAP_SMALL = new SimpleVoxmap(3, 2, 3, new byte[]{
                1, 2, 3,
                4, 10, 6,
                7, 8, 9, // Bottom

                1, 2, 3,
                4, 5, 6,
                7, 8, 9 // Top
        }).setCenter(new BlockPos(1, 1, 1));
    }

    public HugeMushroomGenFeature setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * Select the appropriate sized cap for a huge mushroom type
     *
     * @param mushroomBlock Red or Brown mushroom block
     * @param height        The height of the huge mushroom
     * @return a voxmap of the cap to create
     */
    protected SimpleVoxmap getCapForHeight(Block mushroomBlock, int height) {

        // Brown Cap mushroom
        if (mushroomBlock == Blocks.BROWN_MUSHROOM_BLOCK) {
            switch (height) {
                case 2:
                case 3:
                    return BROWN_CAP_SMALL;
                case 4:
                case 5:
                    return BROWN_CAP_MEDIUM;
                default:
                    return BROWN_CAP;
            }
        }

        // Red Cap mushroom
        switch (height) {
            case 2:
                return BROWN_CAP_SMALL;
            case 3:
                return RED_CAP_SMALL;
            case 4:
                return RED_CAP_SHORT;
            default:
                return RED_CAP;
        }
    }

    //Override this for custom mushroom heights
    protected int getMushroomHeight(IWorld world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
        return this.height > 0 ? this.height : random.nextInt(9) + 2;
    }

    @Override
    public boolean generate(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
        final BlockPos genPos = rootPos.above();
        final int height = this.getMushroomHeight(world, rootPos, biome, random, radius, safeBounds);
        final BlockState soilState = world.getBlockState(rootPos);

        if (species.isAcceptableSoilForWorldgen(world, rootPos, soilState)) {
            Block mushroomBlock = configuredGenFeature.get(MUSHROOM_BLOCK);

            if (mushroomBlock == null) {
                mushroomBlock = random.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
            }

            final SimpleVoxmap capMap = this.getCapForHeight(mushroomBlock, height);

            final BlockPos capPos = genPos.above(height - 1); // Determine the cap position(top block of mushroom cap)
            final BlockBounds capBounds = capMap.getBounds().move(capPos); // Get a bounding box for the entire cap

            if (safeBounds.inBounds(capBounds, true)) {//Check to see if the cap can be generated in safeBounds

                // Check there's room for a mushroom cap and stem.
                for (BlockPos mutPos : Iterables.concat(BlockPos.betweenClosed(BlockPos.ZERO.below(capMap.getLenY()), BlockPos.ZERO.below(height - 1)), capMap.getAllNonZero())) {
                    final BlockPos dPos = mutPos.offset(capPos);
                    final BlockState state = world.getBlockState(dPos);
                    if (!state.getMaterial().isReplaceable()) {
                        return false;
                    }
                }

                final BlockState stemState = configuredGenFeature.get(STEM_BLOCK).defaultBlockState();

                // Construct the mushroom cap from the voxel map.
                for (SimpleVoxmap.Cell cell : capMap.getAllNonZeroCells()) {
                    world.setBlock(capPos.offset(cell.getPos()), this.getMushroomStateForValue(mushroomBlock, stemState, cell.getValue(), cell.getPos().getY()), 2);
                }

                // Construct the stem.
                final int stemLen = height - capMap.getLenY();
                for (int y = 0; y < stemLen; y++) {
                    world.setBlock(genPos.above(y), stemState, 2);
                }

                return true;
            }
        }

        return false;
    }

    // Whatever. It works.
    protected BlockState getMushroomStateForValue(Block mushroomBlock, BlockState stemBlock, int value, int y) {
        if (value == 10) {
            return stemBlock;
        }

        return mushroomBlock.defaultBlockState()
                .setValue(UP, y >= -1)
                .setValue(DOWN, false)
                .setValue(NORTH, value >= 1 && value <= 3)
                .setValue(SOUTH, value >= 7 && value <= 9)
                .setValue(WEST, value == 1 || value == 4 || value == 7)
                .setValue(EAST, value % 3 == 0);
    }

}
