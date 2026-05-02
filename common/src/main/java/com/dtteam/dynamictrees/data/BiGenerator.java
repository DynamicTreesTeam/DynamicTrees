package com.dtteam.dynamictrees.data;

import net.minecraft.util.Tuple;

/**
 * A generator for a resource, providing the means to construct a Json file using provided {@link Dependencies} and
 * using more than one generator.
 *
 * @param <P1> the type of the first generator
 * @param <P2> the type of the second generator
 * @param <I> the type of the input to get the dependencies from
 */
public interface BiGenerator<P1, P2, I> extends Generator<Tuple<P1,P2>, I> {

    @Override
    default void generate(Tuple<P1,P2> providers, I input, Dependencies dependencies){
        generate(providers.getA(), providers.getB(), input, dependencies);
    }
    void generate(P1 provider1, P2 provider2, I input, Dependencies dependencies);

}
