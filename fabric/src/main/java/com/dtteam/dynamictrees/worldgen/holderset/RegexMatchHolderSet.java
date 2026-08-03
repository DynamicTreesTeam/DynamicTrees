package com.dtteam.dynamictrees.worldgen.holderset;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class RegexMatchHolderSet<T> extends StreamBackedHolderSet<T> {

    private final HolderLookup.RegistryLookup<T> registryLookup;
    private final String regex;
    private Pattern pattern;

    public RegexMatchHolderSet(HolderLookup.RegistryLookup<T> registryLookup, String regex) {
        this.registryLookup = registryLookup;
        this.regex = regex;
    }

    public final HolderLookup.RegistryLookup<T> registryLookup() {
        return this.registryLookup;
    }

    public final String regex() {
        return this.regex;
    }

    private Pattern getPattern() {
        if (this.pattern == null) {
            this.pattern = Pattern.compile(this.regex);
        }
        return this.pattern;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Holder<T>> stream() {
        return (Stream<Holder<T>>) (Stream<?>) this.registryLookup.listElements()
                .filter(holder ->
                        this.getInput(holder).anyMatch(input ->
                                this.getPattern().matcher(input).matches()
                        )
                );
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.registryLookup.canSerializeIn(owner);
    }

    protected abstract Stream<String> getInput(Holder<T> holder);
}
