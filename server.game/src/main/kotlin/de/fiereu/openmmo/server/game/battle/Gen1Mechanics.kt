package de.fiereu.openmmo.server.game.battle

import de.fiereu.openmmo.common.Pokemon
import de.fiereu.openmmo.pokemon.SpeciesDef
import kotlin.math.sqrt

// Gen 1/2 stat formula: DVs (IV/2, 0-15), Stat Exp (sqrt of scaled EV), no natures.
// Special = one stat used for both SpAtk and SpDef (Gen1 only; Gen2 splits them but uses same type split).
object Gen1StatCalculator {

    // Map our 0-252 EVs → Gen1 Stat Exp range (0-65535), take floor sqrt → 0-255.
    private fun statExp(ev: Int): Int = sqrt(ev * 65535.0 / 252).toInt().coerceAtMost(255)

    fun maxHp(base: Int, iv: Int, ev: Int, level: Int): Int {
        val dv = iv / 2
        return ((base + dv) * 2 + statExp(ev)) * level / 100 + level + 10
    }

    fun stat(base: Int, iv: Int, ev: Int, level: Int): Int {
        val dv = iv / 2
        return ((base + dv) * 2 + statExp(ev)) * level / 100 + 5
    }

    fun computeAll(species: SpeciesDef, pokemon: Pokemon, splitSpecial: Boolean = false): ComputedStats {
        val level = pokemon.level.toInt()
        val ivs = pokemon.iVs
        val evs = pokemon.eVs
        val special = stat(species.baseSpAttack, ivs.spAtk, evs.spAtk, level)
        return ComputedStats(
            hp = maxHp(species.baseHp, ivs.hp, evs.hp, level),
            atk = stat(species.baseAttack, ivs.atk, evs.atk, level),
            def = stat(species.baseDefense, ivs.def, evs.def, level),
            // Gen1: SpDef = SpAtk. Gen2: split but still type-based category.
            spAtk = special,
            spDef = if (splitSpecial) stat(species.baseSpDefense, ivs.spDef, evs.spDef, level) else special,
            spd = stat(species.baseSpeed, ivs.spd, evs.spd, level),
        )
    }
}

// Gen1 crit rate: Speed / 2, capped at 255, out of 256.
// Gen3 uses flat 1/16. This object lets BattleRng pick the right formula.
object Gen1CritRate {
    fun threshold(speed: Int): Int = (speed / 2).coerceAtMost(255)
    // Returns true if roll (0-255) < threshold
    fun isCrit(roll: Int, speed: Int): Boolean = roll < threshold(speed)
}
