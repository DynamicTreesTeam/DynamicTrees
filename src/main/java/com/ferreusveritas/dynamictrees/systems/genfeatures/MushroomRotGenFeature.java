package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostRotGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

/**
 * Default implementation of {@link IPostRotGenFeature}, simply turning the rotted branch
 * into the {@link #MUSHROOM} set in the {@link ConfiguredGenFeature} object.
 *
 * @author Harley O'Connor
 */
public class MushroomRotGenFeature extends GenFeature implements IPostRotGenFeature {

    public static final ConfigurationProperty<Block> MUSHROOM = ConfigurationProperty.block("mushroom");
    public static final ConfigurationProperty<Block> ALTERNATE_MUSHROOM = ConfigurationProperty.block("alternate_mushroom");
    public static final ConfigurationProperty<Float> ALTERNATE_MUSHROOM_CHANCE = ConfigurationProperty.floatProperty("alternate_mushroom_chance");

    public MushroomRotGenFeature(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MUSHROOM, ALTERNATE_MUSHROOM, ALTERNATE_MUSHROOM_CHANCE);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MUSHROOM, Blocks.BROWN_MUSHROOM)
                .with(ALTERNATE_MUSHROOM, Blocks.RED_MUSHROOM)
                .with(ALTERNATE_MUSHROOM_CHANCE, .25f);
    }

    @Override
    public void postRot(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos pos, int neighborCount, int radius, int fertility, Random random, boolean rapid) {
        final Block mushroom = configuredGenFeature.get(ALTERNATE_MUSHROOM_CHANCE) > random.nextFloat() ? configuredGenFeature.get(MUSHROOM) : configuredGenFeature.get(ALTERNATE_MUSHROOM);

        if (radius <= 4 || !this.canSustainMushroom(world, pos, mushroom) || world.getBrightness(LightType.SKY, pos) >= 4)
            return;

        world.setBlock(pos, mushroom.defaultBlockState(), 3);
    }

    private boolean canSustainMushroom (final IWorld world, final BlockPos pos, final Block block) {
        return block instanceof IPlantable && world.getBlockState(pos).canSustainPlant(world, pos, Direction.UP, (IPlantable) block);
    }

}
