package com.ferreusveritas.dynamictrees.cells;

public class DarkOakLeafCell extends MatrixCell {
	
	public DarkOakLeafCell(int value) {
		super(value, valMap);
	}

	static final byte[] valMap = {
			0, 1, 2, 3, 3, 0, 0, 0, //D Maps 4 -> 3, * -> *
			0, 1, 2, 3, 3, 0, 0, 0, //U Maps 4 -> 3, * -> *
			0, 1, 2, 3, 4, 0, 0, 0, //N Maps * -> *
			0, 1, 2, 3, 4, 0, 0, 0, //S Maps * -> *
			0, 1, 2, 3, 4, 0, 0, 0, //W Maps * -> *
			0, 1, 2, 3, 4, 0, 0, 0  //E Maps * -> *
	};
	
}
