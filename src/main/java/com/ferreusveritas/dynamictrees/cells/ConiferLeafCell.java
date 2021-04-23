package com.ferreusveritas.dynamictrees.cells;

public class ConiferLeafCell extends MatrixCell {
	
	public ConiferLeafCell(int value) {
		super(value, valMap);
	}

	static final byte[] valMap = {
			0, 0, 0, 0, 0, 0, 0, 0, //D Maps * -> 0
			0, 1, 2, 2, 4, 0, 0, 0, //U Maps 3 -> 2, 4 -> 4, * -> *
			0, 1, 2, 0, 2, 0, 0, 0, //N Maps 3 -> 0, 4 -> 2, * -> *
			0, 1, 2, 0, 2, 0, 0, 0, //S Maps 3 -> 0, 4 -> 2, * -> *
			0, 1, 2, 0, 2, 0, 0, 0, //W Maps 3 -> 0, 4 -> 2, * -> *
			0, 1, 2, 0, 2, 0, 0, 0  //E Maps 3 -> 0, 4 -> 2, * -> *
	};
	
}
