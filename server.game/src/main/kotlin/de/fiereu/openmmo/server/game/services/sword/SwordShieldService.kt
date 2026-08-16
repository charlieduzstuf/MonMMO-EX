package de.fiereu.openmmo.server.game.services.sword

import de.fiereu.network.SessionContext
import de.fiereu.openmmo.server.game.services.BattleService
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject
import javax.inject.Singleton

private val log = KotlinLogging.logger {}

// Galar / Sword-Shield encounter dispatcher.
// When libsword_bridge.so is on LD_LIBRARY_PATH the native bridge is used
// (real pkNX-derived encounter tables compiled into the .so).
// Without the lib, an identical Kotlin stub table is used so Galar is
// playable without requiring the native build.
@Singleton
class SwordShieldService @Inject constructor(private val battleService: BattleService) {

    // Area ID constants mirror openmmo/GalarEncounters.h
    object Area {
        const val ROUTE_1           = 1
        const val ROUTE_2           = 2
        const val ROUTE_3           = 3
        const val MINE_1            = 4
        const val ROUTE_4           = 5
        const val ROUTE_5           = 6
        const val MINE_2            = 7
        const val ROUTE_6           = 8
        const val ROUTE_7           = 9
        const val ROUTE_8           = 10
        const val ROUTE_9           = 11
        const val ROUTE_10          = 12
        const val WILD_SOUTH        = 20
        const val WILD_EAST         = 21
        const val WILD_NORTH        = 22
        const val WILD_LAKE         = 23
        const val WILD_GIANT_SEAT   = 24
        const val WILD_HAMMERLOCK   = 25
        const val WILD_DUSTY_BOWL   = 26
        const val WILD_GIANT_CAP    = 27
        const val WILD_SNOWFIELDS   = 28
        const val WILD_WATCHTOWER   = 29
        const val WILD_BRIDGE       = 30
    }

    private val bridgeLoaded: Boolean = tryLoadBridge()

    suspend fun startWildEncounter(session: SessionContext, areaId: Int) {
        val (dexId, level) = if (bridgeLoaded) {
            val arr = bridgePickEncounter(areaId)
            Pair(arr[0], arr[1])
        } else {
            stubEncounter(areaId)
        }
        battleService.startWildBattle(session, dexId, level)
    }

    private external fun bridgePickEncounter(areaId: Int): IntArray
    private external fun bridgeInit(): Boolean

    // Kotlin mirror of GalarEncounters.cpp — same data, same probabilities.
    // ponytail: flat map, no weather variants; mirrors the C table's first subtable.
    private fun stubEncounter(areaId: Int): Pair<Int, Int> {
        data class Slot(val dex: Int, val prob: Int)
        data class Table(val lvMin: Int, val lvMax: Int, val slots: List<Slot>)

        val tables: Map<Int, Table> = mapOf(
            Area.ROUTE_1         to Table(3, 5,  listOf(Slot(821,30), Slot(819,30), Slot(827,20), Slot(829,20))),
            Area.ROUTE_2         to Table(5, 8,  listOf(Slot(831,35), Slot(835,30), Slot(829,20), Slot(827,15))),
            Area.ROUTE_3         to Table(8, 12, listOf(Slot(831,30), Slot(833,25), Slot(835,20), Slot(52,15),  Slot(829,10))),
            Area.MINE_1          to Table(12,16, listOf(Slot(837,50), Slot(852,30))),
            Area.ROUTE_4         to Table(14,18, listOf(Slot(831,25), Slot(840,20), Slot(835,20), Slot(829,20), Slot(843,15))),
            Area.ROUTE_5         to Table(18,22, listOf(Slot(856,30), Slot(859,30), Slot(854,20), Slot(840,20))),
            Area.MINE_2          to Table(22,26, listOf(Slot(837,40), Slot(850,35), Slot(852,25))),
            Area.ROUTE_6         to Table(26,30, listOf(Slot(843,30), Slot(850,25), Slot(848,20), Slot(870,15), Slot(554,10))),
            Area.ROUTE_7         to Table(35,38, listOf(Slot(77,30),  Slot(856,25), Slot(872,25), Slot(854,20))),
            Area.ROUTE_8         to Table(38,42, listOf(Slot(872,35), Slot(875,30), Slot(877,20), Slot(871,15))),
            Area.ROUTE_9         to Table(38,43, listOf(Slot(845,40), Slot(871,30), Slot(79,30))),
            Area.ROUTE_10        to Table(42,46, listOf(Slot(872,35), Slot(875,30), Slot(618,25), Slot(885,10))),
            Area.WILD_SOUTH      to Table(15,55, listOf(Slot(133,10), Slot(831,20), Slot(829,20), Slot(52,10),  Slot(835,15), Slot(848,10), Slot(833,15))),
            Area.WILD_EAST       to Table(20,55, listOf(Slot(845,20), Slot(840,20), Slot(843,20), Slot(856,15), Slot(859,15), Slot(877,10))),
            Area.WILD_NORTH      to Table(25,55, listOf(Slot(870,20), Slot(850,20), Slot(871,20), Slot(618,15), Slot(877,15), Slot(885,10))),
            Area.WILD_GIANT_SEAT to Table(28,60, listOf(Slot(859,25), Slot(856,25), Slot(877,20), Slot(884,10), Slot(885,10), Slot(870,10))),
        )

        val tbl = tables[areaId] ?: tables[Area.WILD_SOUTH]!!
        val roll = (1..100).random()
        var acc = 0
        for (slot in tbl.slots) {
            acc += slot.prob
            if (roll <= acc) return Pair(slot.dex, (tbl.lvMin..tbl.lvMax).random())
        }
        return Pair(tbl.slots.last().dex, (tbl.lvMin..tbl.lvMax).random())
    }

    private fun tryLoadBridge(): Boolean = runCatching {
        System.loadLibrary("sword_bridge")
        bridgeInit()
    }.getOrElse {
        log.info { "pokesword bridge absent (${it.message}), using Kotlin stub encounters" }
        false
    }
}
