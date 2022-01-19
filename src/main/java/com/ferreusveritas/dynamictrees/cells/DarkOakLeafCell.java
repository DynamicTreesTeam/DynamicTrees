package com.ferreusveritas.dynamictrees.cells;

public class DarkOakLeafCell extends MatrixCell {

    public DarkOakLeafCell(int value) {
        super(value, valMap);
    }

    static final byte[] valMap = {
            0, 0, 0, 0, 0, 5, 6, 6, //D Maps 1 -> 0, 2 -> 0, 3 -> 0, 4 -> 0, 7 -> 6, * -> *
            0, 0, 0, 3, 0, 5, 6, 6, //U Maps 1 -> 0, 2 -> 0, 4 -> 0, 7 -> 6, * -> *
            0, 1, 2, 3, 4, 5, 6, 7, //N Maps * -> *
            0, 1, 2, 3, 4, 5, 6, 7, //S Maps * -> *
            0, 1, 2, 3, 4, 5, 6, 7, //W Maps * -> *
            0, 1, 2, 3, 4, 5, 6, 7  //E Maps * -> *
    };

}
