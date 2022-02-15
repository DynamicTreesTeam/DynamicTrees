package com.ferreusveritas.dynamictrees.compat.seasons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper.isSeasonBetween;

/**
 * Stores the period in which a fruit or pod can flower.
 */
public class FlowerPeriod {

    public static final Codec<FlowerPeriod> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("start").forGetter(FlowerPeriod::getStart),
                    Codec.FLOAT.fieldOf("end").forGetter(FlowerPeriod::getEnd)
            ).apply(instance, FlowerPeriod::new)
    );

    /**
     * The minimum season value at which the fruit can flower.
     */
    private final float start;
    /**
     * The maximum season value at which the fruit can flower.
     */
    private final float end;

    public FlowerPeriod(float start, float end) {
        this.start = start;
        this.end = end;
    }

    float getStart() {
        return start;
    }

    float getEnd() {
        return end;
    }

    /**
     * Tests if the given season is in this flower period, bearing the fruiting offset in mind.
     *
     * @param seasonValue    the value of the season
     * @param fruitingOffset the season value offset for the fruit
     * @return {@code true} if the fruit can flower at the given season time
     */
    public boolean isIn(Float seasonValue, Float fruitingOffset) {
        return isSeasonBetween(seasonValue, start + fruitingOffset, end + fruitingOffset);
    }

}
