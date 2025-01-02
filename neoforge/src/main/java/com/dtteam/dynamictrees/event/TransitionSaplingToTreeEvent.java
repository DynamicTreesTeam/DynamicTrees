package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when {@link Species#transitionToTree(Level, BlockPos)} is invoked, before placement checks and logic are done.
 * <p>
 * This event is {@link ICancellableEvent}.
 * <p>
 * This event does not have a result.
 * <p>
 * This event is fired on the NeoForge event bus.
 *
 * @author Harley O'Connor
 */
public class TransitionSaplingToTreeEvent extends Event implements ICancellableEvent {

    /** The species to transition to. */
    private final Species species;
    private final Level level;
    /** Position of sapling block. */
    private final BlockPos pos;

    public TransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos) {
        this.species = species;
        this.level = level;
        this.pos = pos;
    }

    public Species getSpecies() {
        return species;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

}
