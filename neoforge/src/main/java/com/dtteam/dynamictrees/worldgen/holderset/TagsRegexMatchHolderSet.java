package com.dtteam.dynamictrees.worldgen.holderset;

import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;

import java.util.stream.Stream;

public class TagsRegexMatchHolderSet<T> extends RegexMatchHolderSet<T> {

    //TODO: ???
    @Override
    public boolean isBound() {
        return true;
    }

    public static class Type implements HolderSetType {
        @Override
        public <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<Holder<T>> codec, boolean b) {
            return RegexMatchHolderSet.mapCodec(resourceKey, TagsRegexMatchHolderSet::new);
        }

        @Override
        public <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
            return ByteBufCodecs.fromCodecWithRegistriesTrusted(RegexMatchHolderSet.mapCodec(resourceKey, TagsRegexMatchHolderSet::new).codec());
        }
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
        return NeoForgeRegistryLoader.TAGS_REGEX_MATCH_HOLDER_SET_TYPE.get();
    }
}