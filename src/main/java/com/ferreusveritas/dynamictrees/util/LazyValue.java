package com.ferreusveritas.dynamictrees.util;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface LazyValue<T> {
	
	T get();

	static <T> LazyValue<T> uninitialised(Supplier<T> supplier) {
		return new SuppliedLazyValue<>(supplier);
	}
	
}
