package de.fiereu.openmmo.common.enums

// Stored in storyVars[VAR_KEY] as ordinal.
// CLASSIC_RB/YELLOW share Kanto maps with FireRed (parallel "skin" mode).
// CLASSIC_GS/CRYSTAL share Johto maps with HeartGold.
// All modes get full MMO layer; mechanics differ.
enum class GameMode {
    CLASSIC_RB,
    CLASSIC_YELLOW,
    CLASSIC_GS,
    CLASSIC_CRYSTAL,
    REMAKE,
    MODERN_SWORD,
    ;

    companion object {
        const val VAR_KEY = "gameMode"

        fun fromVar(value: Int?): GameMode = entries.getOrElse(value ?: REMAKE.ordinal) { REMAKE }

        fun isGen1(mode: GameMode) = mode == CLASSIC_RB || mode == CLASSIC_YELLOW
        fun isGen2(mode: GameMode) = mode == CLASSIC_GS || mode == CLASSIC_CRYSTAL
        fun isClassic(mode: GameMode) = mode.ordinal <= CLASSIC_CRYSTAL.ordinal
    }
}
