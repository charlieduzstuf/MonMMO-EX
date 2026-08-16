package de.fiereu.openmmo.server.game.services

import de.fiereu.openmmo.server.game.storage.StoredCharacter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// Exports/imports a character as a human-editable JSON file.
// Edit flags, vars, items, or party then /saveimport to apply.
@Singleton
class LocalSaveService @Inject constructor() {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun export(char: StoredCharacter, file: File) {
        file.writeText(json.encodeToString(SaveBlob.from(char)))
    }

    // Applies only the tweakable fields from the file onto the live character.
    // Identity (id, userId, name, timestamps) is never overwritten from file.
    fun import(char: StoredCharacter, file: File): StoredCharacter {
        val blob = json.decodeFromString<SaveBlob>(file.readText())
        return char.copy(
            items = blob.items.map { it.key.toInt() to it.value }.toMap().toMutableMap(),
            storyFlags = blob.storyFlags.toMutableSet(),
            storyVars = blob.storyVars.toMutableMap(),
        )
    }

    @Serializable
    data class SaveBlob(
        val name: String,
        val regionId: Int,
        // items: stringified Int key because JSON spec requires string keys
        val items: Map<String, Int>,
        val storyFlags: Set<String>,
        val storyVars: Map<String, Int>,
        // Compact party: "dexId,level,hp,nature,moves..." — enough to diff/tweak
        val party: List<String>,
    ) {
        companion object {
            fun from(char: StoredCharacter) = SaveBlob(
                name = char.info.name,
                regionId = char.info.positionRegionId.toInt(),
                items = char.items.map { it.key.toString() to it.value }.toMap(),
                storyFlags = char.storyFlags.toSet(),
                storyVars = char.storyVars.toMap(),
                party = char.pokemon.map { p ->
                    "${p.dexId},${p.level},${p.hp},${p.nature.name}," +
                        p.moves.joinToString("|") { "${it.id}:${it.pp}" }
                },
            )
        }
    }
}
