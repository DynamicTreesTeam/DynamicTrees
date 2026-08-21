package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.systems.genfeature.context.FullGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.tree.ChunkTreeHelper;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.Comparator;

/**
 * Used to add mushrooms under a tree canopy.  Currently used by dark oaks for roofed forests.
 *
 * @author ferreusveritas
 */
public class HugeMushroomUndergrowthGenFeature extends HugeMushroomGenFeature {

    public static final ConfigurationProperty<Integer> MAX_MUSHROOMS = ConfigurationProperty.integer("max_mushrooms");
    public static final ConfigurationProperty<Integer> MAX_ATTEMPTS = ConfigurationProperty.integer("max_attempts");

    public HugeMushroomUndergrowthGenFeature(Identifier registryName) {
        super(registryName);
    }

    protected void registerProperties() {
        super.registerProperties();
        this.register(MAX_MUSHROOMS, MAX_ATTEMPTS);
    }

    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MAX_MUSHROOMS, 2)
                .with(MAX_ATTEMPTS, 4);
    }

    protected boolean generate(GenFeatureConfiguration configuration, FullGenerationContext context) {
        return false;
    }

    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.endPoints().isEmpty() || !context.isWorldGen() || context.radius() < 5) {
            return false;
        }

        final LevelAccessor level = context.level();
        final BlockPos rootPos = context.pos();
        final BlockPos lowest = Collections.min(context.endPoints(), Comparator.comparingInt(Vec3i::getY));
        final RandomSource rand = context.random();

        int success = 0;

        for (int tries = 0; tries < configuration.get(MAX_ATTEMPTS); tries++) {

            float angle = (float) (rand.nextFloat() * Math.PI * 2);
            int xOff = (int) (Mth.sin(angle) * (context.radius() - 1));
            int zOff = (int) (Mth.cos(angle) * (context.radius() - 1));

            BlockPos mushPos = rootPos.offset(xOff, 0, zOff);

            mushPos = CoordUtils.findWorldSurface(level, new BlockPos(mushPos), context.isWorldGen()).above();

            if (ChunkTreeHelper.canCheckSurroundings(level, mushPos, 3)) {
                int maxHeight = lowest.getY() - mushPos.getY();
                if (maxHeight >= 2) {
                    int height = Math.min(Mth.clamp(rand.nextInt(maxHeight) + 3, 3, maxHeight), maxHeightBase+maxHeightVar - 1);

                    if (this.setHeight(height).generateMushrooms(configuration, new FullGenerationContext(
                            context.level(),
                            mushPos.below(),
                            context.species(),
                            context.biome(),
                            context.radius(),
                            context.isWorldGen()
                    ))) {
                        if (++success >= configuration.get(MAX_MUSHROOMS)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
