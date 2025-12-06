package com.dtteam.dynamictrees.api.season;

public enum ClimateZoneType {
    NONE (SeasonType.STANDARD),
    TEMPERATE (SeasonType.STANDARD),
    TROPICAL (SeasonType.DRY_WET),
    ARID (SeasonType.DRY_WET),
    COLD (SeasonType.STANDARD);

    public final SeasonType seasonType;
    ClimateZoneType(SeasonType seasonType){
        this.seasonType = seasonType;
    }
}