package com.ferreusveritas.dynamictrees.api.backport;

public enum EnumActionResult {
    SUCCESS(true),
    PASS(false),
    FAIL(false);
	
	private final boolean value;
	
	EnumActionResult(boolean value) {
		this.value = value;
	}

	public boolean result() {
		return value;
	}
	
}