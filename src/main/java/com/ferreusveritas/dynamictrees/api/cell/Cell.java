package com.ferreusveritas.dynamictrees.api.cell;

import net.minecraft.core.Direction;

public interface Cell {

    /**
     * @return The actual value of the cell.
     */
    int getValue();

    /**
     * Gets the value the cell returns for the given side.
     *
     * @param side The side's {@link Direction}.
     * @return The value for the given side.
     */
    int getValueFromSide(Direction side);

}
