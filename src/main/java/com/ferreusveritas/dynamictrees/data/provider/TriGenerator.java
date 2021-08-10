package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.data.IDataProvider;

import java.util.Optional;

/**
 * A class that handles generating a particular resource using three required inputs.
 *
 * @param <P> the type of the data provider
 * @param <A> the type of the first input object to generate from
 * @param <B> the type of the second input object to generate from
 * @param <C> the type of the third input object to generate from
 * @author Harley O'Connor
 * @see Generator
 * @see BiGenerator
 */
public interface TriGenerator<P extends IDataProvider, A, B, C> {

    /**
     * Generates the resource from the specified {@code provider} and {@code input}.
     *
     * @param provider the data provider
     * @param a the first input object to generate from
     * @param b the second input object to generate from
     * @param c the third input object to generate from
     */
    void generate(P provider, A a, B b, C c);

    default void generate(P provider, Optional<A> optionalA, Optional<B> optionalB, Optional<C> optionalC) {
        Optionals.ifAllPresent(
                (a, b, c) -> this.generate(provider, a, b, c),
                optionalA,
                optionalB,
                optionalC
        );
    }
    
}
