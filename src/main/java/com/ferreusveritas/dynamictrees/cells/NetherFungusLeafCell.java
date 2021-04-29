package com.ferreusveritas.dynamictrees.cells;

public class NetherFungusLeafCell extends MatrixCell {

	public NetherFungusLeafCell(int value) {
		super(value, valMap);
	}

	/**
	 * For hydro 6 and 7, the values stay the same.
	 * For hydro 5 and lower, only the bottom returns its value, all other
	 * faces are set to 0. This is to cause the droopy wart effect
	 */
	static final byte[] valMap = {
			0, 1, 2, 3, 4, 5, 6, 7, //D Maps 5 -> 4, * -> *
			0, 0, 0, 0, 0, 0, 6, 7, //U Maps 6 -> 6, 7 -> 7, * -> 0
			0, 0, 0, 0, 0, 0, 6, 7, //N Maps 6 -> 6, 7 -> 7, * -> 0
			0, 0, 0, 0, 0, 0, 6, 7, //S Maps 6 -> 6, 7 -> 7, * -> 0
			0, 0, 0, 0, 0, 0, 6, 7, //W Maps 6 -> 6, 7 -> 7, * -> 0
			0, 0, 0, 0, 0, 0, 6, 7  //E Maps 6 -> 6, 7 -> 7, * -> 0
	};
	
}
