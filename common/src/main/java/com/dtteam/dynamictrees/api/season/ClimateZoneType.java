package com.dtteam.dynamictrees.api.season;

public enum ClimateZoneType {
    NONE (SeasonType.TEMPERATURE),
    TEMPERATE (SeasonType.TEMPERATURE),
    TROPICAL (SeasonType.DRY_WET),
    ARID (SeasonType.DRY_WET),
    COLD (SeasonType.TEMPERATURE);

    public final SeasonType seasonType;
    ClimateZoneType(SeasonType seasonType){
        this.seasonType = seasonType;
    }
}