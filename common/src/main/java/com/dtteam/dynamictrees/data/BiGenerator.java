package com.dtteam.dynamictrees.data;

import com.mojang.datafixers.util.Pair;

/**
 * A generator for a resource, providing the means to construct a Json file using provided {@link Dependencies} and
 * using more than one generator.
 *
 * @param <P1> the type of the first generator
 * @param <P2> the type of the second generator
 * @param <I> the type of the input to get the dependencies from
 */
public interface BiGenerator<P1, P2, I> extends Generator<Pair<P1,P2>, I> {

    @Override
    default void generate(Pair<P1,P2> providers, I input, Dependencies dependencies){
        generate(providers.getFirst(), providers.getSecond(), input, dependencies);
    }
    void generate(P1 provider1, P2 provider2, I input, Dependencies dependencies);

}
