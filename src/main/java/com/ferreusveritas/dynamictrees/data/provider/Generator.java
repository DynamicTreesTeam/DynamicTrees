package com.ferreusveritas.dynamictrees.data.provider;

import net.minecraft.data.IDataProvider;

import java.util.Optional;

/**
 * A class that handles generating a particular resource using one required input.
 *
 * @param <P> the type of the data provider
 * @param <I> the type of the input object to generate from
 * @author Harley O'Connor
 * @see BiGenerator
 * @see TriGenerator
 */
public interface Generator<P extends IDataProvider, I> {

    /**
     * Generates the resource from the specified {@code provider} and {@code input}.
     *
     * @param provider the data provider
     * @param input the input object to generate from
     */
    void generate(P provider, I input);

    default void generate(P provider, Optional<I> optionalInput) {
        optionalInput.ifPresent(input -> this.generate(provider, input));
    }

}
