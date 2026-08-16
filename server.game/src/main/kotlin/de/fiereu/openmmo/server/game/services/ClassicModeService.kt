package de.fiereu.openmmo.server.game.services

import de.fiereu.openmmo.common.enums.GameMode
import de.fiereu.openmmo.server.game.storage.CharacterStore
import javax.inject.Inject
import javax.inject.Singleton

// Manages per-character classic mode selection and Yellow mode toggle.
// Mode is stored as storyVars[GameMode.VAR_KEY] (ordinal).
@Singleton
class ClassicModeService @Inject constructor(private val store: CharacterStore) {

    fun getMode(charId: Long): GameMode {
        val char = store.getCharacter(charId) ?: return GameMode.REMAKE
        return GameMode.fromVar(char.storyVars[GameMode.VAR_KEY])
    }

    fun setMode(charId: Long, mode: GameMode) {
        val char = store.getCharacter(charId) ?: return
        char.storyVars[GameMode.VAR_KEY] = mode.ordinal
        store.flushCharacterAsync(charId)
    }

    // Called at new-game start. Red/Blue path: normal starters.
    // Yellow path: lock starter to Pikachu, set follower flag.
    fun applyStarterBranch(charId: Long, mode: GameMode) {
        if (mode != GameMode.CLASSIC_YELLOW) return
        val char = store.getCharacter(charId) ?: return
        char.storyVars["yellow_pikachu_follower"] = 1
        char.storyFlags.add("FLAG_PIKACHU_STARTER")
        store.flushCharacterAsync(charId)
    }

    fun hasYellowFollower(charId: Long): Boolean =
        store.getCharacter(charId)?.storyVars?.get("yellow_pikachu_follower") == 1
}
