package com.ferreusveritas.dynamictrees.util.holderset;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.holdersets.HolderSetType;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TagsRegexMatchHolderSet<T> extends RegexMatchHolderSet<T> {
    public static <T> Codec<? extends ICustomHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
        return RegexMatchHolderSet.codec(registryKey, TagsRegexMatchHolderSet::new);
    }

    public TagsRegexMatchHolderSet(HolderLookup.RegistryLookup<T> registryLookup, String regex) {
        super(registryLookup, regex);
    }

    @Override
    protected Stream<String> getInput(Holder<T> holder) {
        return holder.tags().map(tagKey -> tagKey.location().toString());
    }

    @Override
    public HolderSetType type() {
        return DTRegistries.TAGS_REGEX_MATCH_HOLDER_SET_TYPE.get();
    }
}