package com.dtteam.dynamictrees.api.season;

public enum ClimateZoneType {
    NONE (SeasonType.TEMPERATURE, "desc.dynamictrees.climate.none"),
    TEMPERATE (SeasonType.TEMPERATURE, "desc.dynamictrees.climate.temperate"),
    TROPICAL (SeasonType.DRY_WET, "desc.dynamictrees.climate.tropical"),
    ARID (SeasonType.DRY_WET, "desc.dynamictrees.climate.arid"),
    COLD (SeasonType.TEMPERATURE, "desc.dynamictrees.climate.cold");

    public final SeasonType seasonType;
    public final String unlocalizedName;
    ClimateZoneType(SeasonType seasonType, String unlocalizedName){
        this.seasonType = seasonType;
        this.unlocalizedName = unlocalizedName;
    }
}