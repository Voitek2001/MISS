package agh.edu.pl.backend.simulation.utils

import java.io.File

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class QTableEntry(
    val state: List<Int>,
    val qValues: List<Int>
)


fun saveQTableToFile(qTable: Map<List<Int>, IntArray>, filename: String) {
    val entries = qTable.map { (state, qValues) -> QTableEntry(state, qValues.toList()) }
    val jsonString = Json.encodeToString(entries)
    File(filename).writeText(jsonString)
}

fun loadQTableFromFile(filename: String): MutableMap<List<Int>, IntArray> {
    val jsonString = File(filename).readText()
    val entries = Json.decodeFromString<List<QTableEntry>>(jsonString)
    return entries.associate { it.state to it.qValues.toIntArray() } as MutableMap<List<Int>, IntArray>
}