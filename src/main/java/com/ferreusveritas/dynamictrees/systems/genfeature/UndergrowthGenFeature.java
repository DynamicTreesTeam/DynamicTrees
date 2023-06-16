package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class UndergrowthGenFeature extends GenFeature {

    public static final ConfigurationProperty<Species> SPECIES_A = ConfigurationProperty.property("undergrowth_species", Species.class);
    public static final ConfigurationProperty<Species> SPECIES_B = ConfigurationProperty.property("secondary_undergrowth_species", Species.class);
    public static final ConfigurationProperty<Float> PROPORTION = ConfigurationProperty.floatProperty("proportion_of_secondary_species");

    public UndergrowthGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(SPECIES_A, SPECIES_B, PROPORTION);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(SPECIES_A, Species.NULL_SPECIES)
                .with(SPECIES_B, Species.NULL_SPECIES)
                .with(PROPORTION, .4f);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        return this.tryToPlaceTree(configuration, context.levelContext(), context.level(), context.pos(), context.radius());
    }

    private boolean tryToPlaceTree(GenFeatureConfiguration config, LevelContext levelContext, LevelAccessor level, BlockPos rootPos, int radius) {
        if (radius <= 2) return false;

        final Vec3 vTree = new Vec3(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

        int placedBushes = 0;
        int attempts = 0;
        while (placedBushes <= 2 && attempts <= 10) {
            int rad = Mth.clamp(level.getRandom().nextInt(radius - 2) + 2, 2, radius - 1);
            Vec3 v = vTree.add(new Vec3(1, 0, 0).scale(rad).yRot((float) (level.getRandom().nextFloat() * Math.PI * 2)));
            BlockPos vPos = BlockPos.containing(v);

            final BlockPos groundPos = CoordUtils.findWorldSurface(level, vPos, true);

            if (!areTreesAround(level, groundPos)){
                Species species = level.getRandom().nextFloat() < config.get(PROPORTION) ? config.get(SPECIES_B) : config.get(SPECIES_A);
                 if (placeTreeAtLocation(levelContext, groundPos, species)){
                     placedBushes++;
                 }
            }
            attempts++;
        }

        return placedBushes > 0;
    }

    int checkDown = -1;
    int checkUp = 4;
    private boolean areTreesAround(LevelAccessor world, BlockPos rootPos) {
        for (int i = checkDown; i <= checkUp; i++) {
            // - - - - -
            // - X X X -
            // - X 0 X -
            // - X X X -
            // - - - - -
            for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
                BlockPos offsetPos = rootPos.offset(dir.getOffset()).above(i);
                if (TreeHelper.isBranch(world.getBlockState(offsetPos)))
                    return true;
            }
        }
        return false;
    }

    private boolean placeTreeAtLocation(LevelContext levelContext, BlockPos newRootPos, Species species) {
        LevelAccessor level = levelContext.accessor();
        for (int i = 1; i >= -1; i--) {
            BlockPos offsetRootPos = newRootPos.above(i);
            if (species.isAcceptableSoil(level.getBlockState(offsetRootPos))) {

                if (level instanceof WorldGenRegion)
                    species.generate(new GenerationContext(
                            levelContext, species, offsetRootPos, offsetRootPos.mutable(),
                            level.getNoiseBiome(offsetRootPos.getX(), offsetRootPos.getY(), offsetRootPos.getZ()),
                            Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom()), 2, SafeChunkBounds.ANY_WG
                    ));
                return true;
            }
        }
        return false;
    }

}
