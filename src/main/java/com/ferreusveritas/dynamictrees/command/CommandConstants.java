package com.ferreusveritas.dynamictrees.command;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommandConstants {

    //////////////////////////////
    // Base Command
    //////////////////////////////

    public static final String COMMAND = "dt";
    public static final String COMMAND_ALIAS = "dynamictrees";

    //////////////////////////////
    // Sub-Commands
    //////////////////////////////

    public static final String GET_TREE = "gettree";
    public static final String GROW_PULSE = "growpulse";
    public static final String KILL_TREE = "killtree";
    public static final String SPECIES_LIST = "specieslist";
    public static final String SOIL_LIFE = "soillife";
    public static final String SET_TREE = "settree";
    public static final String ROTATE_JO_CODE = "rotatejocode";
    public static final String CREATE_STAFF = "createstaff";
    public static final String SET_COORD_XOR = "setcoordxor";
    public static final String CREATE_TRANSFORM_POTION = "createtransformpotion";
    public static final String TRANSFORM = "transform";
    public static final String CLEAR_ORPHANED = "clearorphaned";
    public static final String PURGE_TREES = "purgetrees";

    //////////////////////////////
    // Argument Identifiers
    //////////////////////////////

    public static final String LOCATION = "location";
    public static final String JO_CODE = "jo_code";
    public static final String TURNS = "turns";
    public static final String SPECIES = "species";

    //////////////////////////////
    // Argument Defaults
    //////////////////////////////

    public static final String DEFAULT_JO_CODE = "JP";
    public static final int DEFAULT_TURNS = 0;

    //////////////////////////////
    // Suggestions
    //////////////////////////////

    public static final Collection<String> TURNS_SUGGESTIONS = Stream.of(0, 1, 2).map(String::valueOf).collect(Collectors.toList());

}
