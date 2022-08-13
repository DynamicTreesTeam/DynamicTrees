package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when {@link Species#transitionToTree(net.minecraft.world.World, net.minecraft.util.math.BlockPos)} is invoked,
 * before placement checks and logic are done.
 * <p>
 * This event is {@link Cancelable}.
 * <p>
 * This event does not {@linkplain HasResult have a result}.
 * <p>
 * This event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS Forge bus}.
 *
 * @author Harley O'Connor
 */
@Cancelable
public final class TransitionSaplingToTreeEvent extends Event {

    /** The species to transition to. */
    private final Species species;
    private final World world;
    /** Position of sapling block. */
    private final BlockPos pos;

    public TransitionSaplingToTreeEvent(Species species, World world, BlockPos pos) {
        this.species = species;
        this.world = world;
        this.pos = pos;
    }

    public Species getSpecies() {
        return species;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }
}
