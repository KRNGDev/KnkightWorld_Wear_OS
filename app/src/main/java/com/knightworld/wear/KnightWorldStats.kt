package com.knightworld.wear

import java.time.ZonedDateTime
import kotlin.math.max
import kotlin.math.min

/**
 * Represents the tactical snapshot rendered on the watch face.
 */
data class KnightWorldStats(
    val soldierName: String,
    val rank: String,
    val mission: String,
    val zone: String,
    val healthPercent: Int,
    val energyPercent: Int,
    val moralePercent: Int,
    val victories: Int,
    val defeats: Int,
    val potions: Int,
    val status: String
)

/**
 * Provides lightweight, reproducible data so the canvas renderer can paint an
 * engaging scene without requiring sensors during development or screenshots.
 */
object KnightWorldStatsProvider {

    private val callSigns = listOf(
        "A. Valiente",
        "C. Aguilar",
        "L. Dragón",
        "M. Centella",
        "R. Fantasma"
    )

    private val ranks = listOf(
        "Caballero",
        "Capitán",
        "Comandante",
        "Centinela"
    )

    private val missions = listOf(
        "Rescate Delta",
        "Operación Aurora",
        "Escudo Boreal",
        "Centinela Nocturna",
        "Lanza Carmesí"
    )

    private val zones = listOf(
        "Bosque Umbrío",
        "Fortaleza del Alba",
        "Mar de Cristal",
        "Desierto Heliotropo",
        "Cordillera Esmeralda"
    )

    private val fallbackStatuses = listOf(
        "Operación estable",
        "Avanzando al objetivo",
        "Vigilia estratégica",
        "Estrategia coordinada"
    )

    fun fromTime(zonedDateTime: ZonedDateTime): KnightWorldStats {
        val daySeed = zonedDateTime.dayOfYear
        val minuteSeed = zonedDateTime.minute
        val secondSeed = zonedDateTime.second

        val health = boundedPercent(62 + (daySeed + minuteSeed) % 34)
        val energy = boundedPercent(45 + (minuteSeed * 3 + secondSeed / 2) % 55)
        val morale = boundedPercent(50 + (daySeed + minuteSeed * 2) % 48)

        val victories = 10 + (daySeed % 18)
        val defeats = (minuteSeed / 12) % 4
        val potions = 1 + (secondSeed / 15) % 4

        val name = callSigns[daySeed % callSigns.size]
        val rank = ranks[(daySeed + zonedDateTime.hour) % ranks.size]
        val mission = missions[(daySeed + minuteSeed) % missions.size]
        val zone = zones[(daySeed + zonedDateTime.hour) % zones.size]

        val status = when {
            health < 65 -> "Requiere sanación"
            energy < 55 -> "Reabasteciendo energía"
            morale < 60 -> "Motivando escuadrón"
            else -> fallbackStatuses[(daySeed + minuteSeed) % fallbackStatuses.size]
        }

        return KnightWorldStats(
            soldierName = name,
            rank = rank,
            mission = mission,
            zone = zone,
            healthPercent = health,
            energyPercent = energy,
            moralePercent = morale,
            victories = victories,
            defeats = defeats,
            potions = potions,
            status = status
        )
    }

    private fun boundedPercent(value: Int): Int = min(100, max(0, value))
}
