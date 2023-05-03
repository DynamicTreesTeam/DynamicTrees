package com.ferreusveritas.dynamictrees.util.holderset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class RegexMatchHolderSet<T> extends StreamBackedHolderSet<T> implements ICustomHolderSet<T> {
    protected static <T> Codec<? extends ICustomHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registryKey, BiFunction<Registry<T>, String, RegexMatchHolderSet<T>> factory) {
        return RecordCodecBuilder.<RegexMatchHolderSet<T>>create(builder -> builder.group(
                RegistryOps.retrieveRegistry(registryKey).forGetter(RegexMatchHolderSet::registry),
                Codec.STRING.fieldOf("regex").forGetter(RegexMatchHolderSet::regex)
        ).apply(builder, factory));
    }

    private final Supplier<Registry<T>> registrySupplier;
    private final String regex;
    private Pattern pattern;

    public RegexMatchHolderSet(Registry<T> registry, String regex) {
        this(() -> registry, regex);
    }

    public RegexMatchHolderSet(Supplier<Registry<T>> registrySupplier, String regex) {
        this.registrySupplier = registrySupplier;
        this.regex = regex;
    }

    public final Supplier<Registry<T>> registrySupplier() {
        return this.registrySupplier;
    }

    public Registry<T> registry() {
        return this.registrySupplier.get();
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
        return (Stream<Holder<T>>) (Stream<?>) this.registry().holders().filter(holder -> this.getInput(holder).anyMatch(input -> this.getPattern().matcher(input).matches()));
    }

    /**
     * Gets the stream of input data from the holder to use for regex matching.
     * If any string matches, the holder will be included in the set.
     */
    protected abstract Stream<String> getInput(Holder<T> holder);
}