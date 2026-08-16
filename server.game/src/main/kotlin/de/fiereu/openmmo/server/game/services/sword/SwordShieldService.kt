package de.fiereu.openmmo.server.game.services.sword

import de.fiereu.network.SessionContext
import de.fiereu.openmmo.server.game.services.BattleService
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject
import javax.inject.Singleton

private val log = KotlinLogging.logger {}

// Galar / Sword-Shield integration.
// Delegates wild encounter data to the native pokesword bridge when available;
// falls back to a stub table so Galar is playable without the native lib.
@Singleton
class SwordShieldService @Inject constructor(private val battleService: BattleService) {

    private val bridgeLoaded: Boolean = tryLoadBridge()

    // Called from encounter service when the player is in Galar.
    suspend fun startWildEncounter(session: SessionContext, areaId: Int) {
        val (dexId, level) = if (bridgeLoaded) {
            bridgePickEncounter(areaId)
        } else {
            stubEncounter(areaId)
        }
        battleService.startWildBattle(session, dexId, level)
    }

    // pokesword bridge symbols. Populated by loadBridge() via System.loadLibrary.
    private external fun bridgePickEncounter(areaId: Int): IntArray
    private external fun bridgeInit(): Boolean

    // Stub Galar encounter table — replace with real data from pokesword bridge.
    // areaId maps to Galar route numbers (Route 1 = 101, Wild Area = 200, etc.)
    private fun stubEncounter(areaId: Int): Pair<Int, Int> = when (areaId) {
        101 -> Pair(831, 3)   // Wooloo
        102 -> Pair(835, 5)   // Yamper
        200 -> Pair(884, 25)  // Duraludon (Wild Area)
        else -> Pair(810, 5)  // Grookey fallback
    }

    private fun tryLoadBridge(): Boolean = runCatching {
        System.loadLibrary("sword_bridge")
        bridgeInit()
    }.getOrElse {
        log.info { "pokesword native bridge not loaded (${it.message}), using stub encounters" }
        false
    }
}
