package com.dtteam.dynamictrees.worldgen.holderset;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;

import java.util.stream.Stream;

public class NameRegexMatchHolderSet<T> extends RegexMatchHolderSet<T> {

    public NameRegexMatchHolderSet(HolderLookup.RegistryLookup<T> registryLookup, String regex) {
        super(registryLookup, regex);
    }

    @Override
    protected Stream<String> getInput(Holder<T> holder) {
        return holder.unwrapKey().stream().map(key -> key.location().toString());
    }
}
