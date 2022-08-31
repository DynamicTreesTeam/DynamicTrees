package com.ferreusveritas.dynamictrees.util.holderset;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.holdersets.HolderSetType;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class NameRegexMatchHolderSet<T> extends RegexMatchHolderSet<T> {
    public static <T> Codec<? extends ICustomHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
        return RegexMatchHolderSet.codec(registryKey, NameRegexMatchHolderSet::new);
    }

    public NameRegexMatchHolderSet(Registry<T> registry, String regex) {
        super(registry, regex);
    }

    public NameRegexMatchHolderSet(Supplier<Registry<T>> registrySupplier, String regex) {
        super(registrySupplier, regex);
    }

    @Override
    protected Stream<String> getInput(Holder<T> holder) {
        return holder.unwrapKey().stream().map(key -> key.location().toString());
    }

    @Override
    public HolderSetType type() {
        return DTRegistries.NAME_REGEX_MATCH_HOLDER_SET_TYPE.get();
    }
}
