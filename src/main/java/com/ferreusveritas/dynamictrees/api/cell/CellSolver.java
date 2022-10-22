package com.ferreusveritas.dynamictrees.api.cell;

public interface CellSolver {

    /**
     * Solves the center cell from values of the surrounding 6 cells.
     *
     * @param cells An array of 6 cells, one for each of the 6 sides of a cube.
     * @return the calculated solution for the center cell.
     */
    int solve(Cell[] cells);

}
