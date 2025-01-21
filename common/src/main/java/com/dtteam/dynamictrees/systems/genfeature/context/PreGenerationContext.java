package com.dtteam.dynamictrees.systems.genfeature.context;

import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.systems.poissondisc.PoissonDisc;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.JoCode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

/**
 * @author Harley O'Connor
 */
public class PreGenerationContext extends GenFeatureContext {

    private final int radius;
    private final Direction facing;
    private final boolean worldGen;
    private final JoCode joCode;

    /**
     * Instantiates a new {@link PreGenerationContext} object.
     *
     * @param rootPos  The {@link BlockPos} of the {@link SoilBlock} the generated tree is planted on.
     * @param species  The {@link Species} being grown.
     * @param radius   The radius of the {@link PoissonDisc} the tree generated in.
     * @param facing   The {@link Direction} that will be applied to the {@link JoCode} during generation.
     * @param worldGen Weather this is being run during world generation.
     * @param joCode   The {@link JoCode} generating the tree.
     */
    public PreGenerationContext(LevelAccessor level, BlockPos rootPos, Species species, int radius, Direction facing, boolean worldGen, JoCode joCode) {
        super(level, rootPos, species);
        this.radius = radius;
        this.facing = facing;
        this.worldGen = worldGen;
        this.joCode = joCode;
    }

    public int radius() {
        return radius;
    }

    public Direction facing() {
        return facing;
    }

    public JoCode joCode() {
        return joCode;
    }

    public final boolean isWorldGen() {
        return worldGen;
    }

}
