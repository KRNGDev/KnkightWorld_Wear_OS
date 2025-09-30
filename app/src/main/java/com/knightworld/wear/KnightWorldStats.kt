package com.knightworld.wear

import kotlin.math.max
import kotlin.math.min

/**
 * Domain model that converts raw sensor values into the medieval RPG stats shown on the watch face.
 */
data class KnightWorldStats(
    val steps: Int,
    val battlesWon: Int,
    val battlesLost: Int,
    val potions: Int,
    val healthPercent: Float,
    val energyPercent: Float,
    val soldierName: String,
    val soldierRank: String,
    val mission: String,
    val mapZone: String,
    val narrative: String
) {
    companion object {
        fun fromSensors(batteryLevel: Int, totalSteps: Int): KnightWorldStats {
            val clampedBattery = batteryLevel.coerceIn(0, 100)
            val safeSteps = max(0, totalSteps)
            val battles = safeSteps / 200
            val battlesWon = battles / 2
            val battlesLost = max(0, battles - battlesWon)
            val potions = min(8, safeSteps / 500)
            val healthPercent = clampedBattery / 100f
            val energyPercent = (safeSteps % 1000) / 1000f

            return KnightWorldStats(
                steps = safeSteps,
                battlesWon = battlesWon,
                battlesLost = battlesLost,
                potions = potions,
                healthPercent = healthPercent.coerceIn(0f, 1f),
                energyPercent = energyPercent.coerceIn(0f, 1f),
                soldierName = resolveName(),
                soldierRank = resolveRank(safeSteps, battlesWon),
                mission = resolveMission(potions, battlesWon),
                mapZone = resolveZone(safeSteps),
                narrative = buildNarrative(safeSteps, battlesWon, potions)
            )
        }

        private fun buildNarrative(steps: Int, battlesWon: Int, potions: Int): String = when {
            steps == 0 -> "Da un paseo para comenzar tu aventura."
            steps < 500 -> "Calientas tus botas recorriendo el reino."
            battlesWon < 3 -> "Has derrotado a $battlesWon enemigos. Sigue avanzando."
            potions == 0 -> "Busca pociones para prepararte para el siguiente combate."
            potions in 1..3 -> "Tus alforjas tienen $potions pociones listas."
            else -> "¡Eres una leyenda! El reino canta tus hazañas."
        }

        private fun resolveName(): String = "Sir Solaris"

        private fun resolveRank(steps: Int, battlesWon: Int): String = when {
            battlesWon >= 20 -> "General del Amanecer"
            battlesWon >= 12 -> "Comandante"
            steps >= 5_000 -> "Capitán"
            steps >= 2_500 -> "Teniente"
            steps >= 1_000 -> "Caballero"
            steps >= 250 -> "Escudero"
            else -> "Recluta"
        }

        private fun resolveMission(potions: Int, battlesWon: Int): String = when {
            battlesWon >= 18 -> "Defender el Bastión Celestial"
            battlesWon >= 10 -> "Reconquistar la fortaleza del norte"
            potions >= 5 -> "Escoltar caravanas mágicas"
            potions >= 2 -> "Reforzar el puesto de vigilancia"
            else -> "Explorar el bosque brumoso"
        }

        private fun resolveZone(steps: Int): String = when {
            steps >= 8_000 -> "Montañas Escarlata"
            steps >= 5_000 -> "Cañón de los Dragones"
            steps >= 3_500 -> "Murallas de Liria"
            steps >= 2_000 -> "Páramo de los Ecos"
            steps >= 1_000 -> "Bosque Brumoso"
            steps >= 300 -> "Llanuras de Aegis"
            else -> "Campamento Inicial"
        }
    }
}
