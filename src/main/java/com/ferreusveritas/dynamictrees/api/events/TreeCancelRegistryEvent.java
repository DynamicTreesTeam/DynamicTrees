package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import net.minecraftforge.eventbus.api.Event;

/**
 * Addons can use this event to register their tree cancellers.
 *
 * @author Harley O'Connor
 */
public class TreeCancelRegistryEvent extends Event {

    private final ITreeCanceller treeCanceller;

    public TreeCancelRegistryEvent(ITreeCanceller treeCanceller) {
        this.treeCanceller = treeCanceller;
    }

    public ITreeCanceller getTreeCanceller() {
        return treeCanceller;
    }

}
