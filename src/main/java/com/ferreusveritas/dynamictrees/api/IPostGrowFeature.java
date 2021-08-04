package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPostGrowFeature {

    /**
     * Allows a species to do things after a grow event just occured.  Such as used by Jungle trees to create cocoa pods
     * on the trunk
     *
     * @param configuredGenFeature The {@link ConfiguredGenFeature}
     * @param world                The world
     * @param rootPos              The position of the rooty dirt block
     * @param treePos              The position of the base trunk block of the tree(usually directly above the rooty
     *                             dirt block)
     * @param species              The species being processed
     * @param fertility            The fertility of the soil block this tree is planted in
     * @param natural              If true then this member is being used to grow the tree naturally(create drops or
     *                             fruit). If false then this member is being used to grow a tree with a growth
     *                             accelerant like bonemeal or the potion of burgeoning
     * @return true if operation was successful. false otherwise
     */
    boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int fertility, boolean natural);

}
