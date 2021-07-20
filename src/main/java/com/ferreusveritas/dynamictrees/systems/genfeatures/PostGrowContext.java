package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class PostGrowContext extends GenerationContext<World> {

    private final BlockPos treePos;
    private final int fertility;
    private final boolean natural;

    /**
     * Instantiates a new {@link PostGrowContext} object.
     *
     * @param world The {@link World} object.
     * @param rootPos The {@link BlockPos} of the {@link RootyBlock} the generated tree is planted on.
     * @param treePos The {@link BlockPos} of the base trunk block of the tree (usually directly above the rooty dirt block).
     * @param species The {@link Species} being grown.
     * @param fertility The fertility of the {@link RootyBlock} the tree is planted in.
     * @param natural If {@code true}, this member is being used to grow the tree naturally (create drops or fruit),
     *                otherwise this member is being used to grow a tree with a growth accelerant like bonemeal or the
     *                potion of burgeoning.
     */
    public PostGrowContext(World world, BlockPos rootPos, Species species, BlockPos treePos, int fertility, boolean natural) {
        super(world, rootPos, species);
        this.treePos = treePos;
        this.fertility = fertility;
        this.natural = natural;
    }

    public BlockPos treePos() {
        return treePos;
    }

    public int fertility() {
        return fertility;
    }

    public boolean natural() {
        return natural;
    }

}
