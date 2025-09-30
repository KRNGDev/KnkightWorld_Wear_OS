package com.knightworld.wear

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.LinearProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.knightworld.wear.ui.theme.KnightWorldTheme
import com.knightworld.wear.ui.theme.Mana
import com.knightworld.wear.ui.theme.NightSky
import com.knightworld.wear.ui.theme.Vitality
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSensorPermissionsIfNeeded()

        setContent {
            KnightWorldTheme {
                KnightWorldApp()
            }
        }
    }

    private fun requestSensorPermissionsIfNeeded() {
        val permissions = buildList {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.BODY_SENSORS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.BODY_SENSORS)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@Composable
fun KnightWorldApp() {
    val context = LocalContext.current
    val timeState = rememberCurrentTime()
    val battery by rememberBatteryLevel(context)
    val steps by rememberStepCount(context)

    val stats = remember(battery, steps) {
        KnightWorldStats.fromSensors(batteryLevel = battery, totalSteps = steps)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NightSky)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TimeSection(timeState.value)
            Spacer(modifier = Modifier.height(4.dp))
            BattleStatsSection(stats)
            Spacer(modifier = Modifier.height(4.dp))
            ProgressSection(stats)
            Spacer(modifier = Modifier.height(4.dp))
            EventSection(stats)
        }
    }
}

@Composable
fun TimeSection(currentTime: ZonedDateTime) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMM") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = currentTime.format(timeFormatter),
            style = MaterialTheme.typography.display1,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = currentTime.format(dateFormatter),
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun BattleStatsSection(stats: KnightWorldStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colors.surface.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Text(
            text = "Caballero", // Static label
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip(
                icon = Icons.Default.Flag,
                label = "Victorias",
                value = stats.battlesWon.toString()
            )
            StatChip(
                icon = Icons.Default.Map,
                label = "Derrotas",
                value = stats.battlesLost.toString()
            )
            StatChip(
                icon = Icons.Default.LocalActivity,
                label = "Pociones",
                value = stats.potions.toString()
            )
        }
    }
}

@Composable
fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Chip(
        modifier = Modifier
            .width(72.dp)
            .height(48.dp),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )
        },
        label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        },
        colors = ChipDefaults.chipColors(backgroundColor = Color.Transparent),
        onClick = { }
    )
}

@Composable
fun ProgressSection(stats: KnightWorldStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colors.surface.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        ProgressBar(
            label = "Salud",
            value = stats.healthPercent,
            color = Vitality,
            icon = Icons.Default.Favorite
        )
        Spacer(modifier = Modifier.height(6.dp))
        ProgressBar(
            label = "Energía",
            value = stats.energyPercent,
            color = Mana,
            icon = Icons.Default.Bolt
        )
    }
}

@Composable
fun ProgressBar(
    label: String,
    value: Float,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.caption1,
                color = MaterialTheme.colors.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = value.coerceIn(0f, 1f),
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = color,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun EventSection(stats: KnightWorldStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colors.surface.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Text(
            text = "${stats.steps} pasos",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stats.narrative,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun rememberCurrentTime(zoneId: ZoneId = ZoneId.systemDefault()): State<ZonedDateTime> =
    produceState(initialValue = ZonedDateTime.now(zoneId)) {
        while (true) {
            value = ZonedDateTime.now(zoneId)
            delay(1_000L)
        }
    }

@Composable
fun rememberBatteryLevel(context: Context): State<Int> {
    val levelState = remember { mutableStateOf(100) }
    val currentContext by rememberUpdatedState(newValue = context)

    DisposableEffect(currentContext) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val batteryIntent = intent ?: return
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    levelState.value = ((level / scale.toFloat()) * 100).toInt()
                }
            }
        }

        val sticky = currentContext.registerReceiver(receiver, filter)
        sticky?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                levelState.value = ((level / scale.toFloat()) * 100).toInt()
            }
        }

        onDispose {
            try {
                currentContext.unregisterReceiver(receiver)
            } catch (ignored: IllegalArgumentException) {
                // Receiver was not registered, ignore.
            }
        }
    }

    return levelState
}

@Composable
fun rememberStepCount(context: Context): State<Int> {
    val stepState = remember { mutableStateOf(0) }
    val currentContext by rememberUpdatedState(context)

    DisposableEffect(currentContext) {
        val sensorManager = currentContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            stepState.value = 0
            return@DisposableEffect onDispose { }
        }

        var baseSteps: Float? = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val value = event?.values?.firstOrNull() ?: return
                if (baseSteps == null) {
                    baseSteps = value
                }
                val total = max(0f, value - (baseSteps ?: 0f))
                stepState.value = total.toInt()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return stepState
}

data class KnightWorldStats(
    val steps: Int,
    val battlesWon: Int,
    val battlesLost: Int,
    val potions: Int,
    val healthPercent: Float,
    val energyPercent: Float,
    val narrative: String
) {
    companion object {
        fun fromSensors(batteryLevel: Int, totalSteps: Int): KnightWorldStats {
            val battles = totalSteps / 200
            val battlesWon = battles / 2
            val battlesLost = max(0, battles - battlesWon)
            val potions = min(8, totalSteps / 500)
            val healthPercent = (batteryLevel / 100f).coerceIn(0f, 1f)
            val energyPercent = ((totalSteps % 1000) / 1000f).coerceIn(0f, 1f)
            val narrative = buildNarrative(totalSteps, battlesWon, potions)

            return KnightWorldStats(
                steps = totalSteps,
                battlesWon = battlesWon,
                battlesLost = battlesLost,
                potions = potions,
                healthPercent = healthPercent,
                energyPercent = energyPercent,
                narrative = narrative
            )
        }

        private fun buildNarrative(steps: Int, battlesWon: Int, potions: Int): String {
            return when {
                steps == 0 -> "Da un paseo para comenzar tu aventura."
                steps < 500 -> "Calientas tus botas recorriendo el reino."
                battlesWon < 3 -> "Has derrotado a $battlesWon enemigos. Sigue avanzando."
                potions == 0 -> "Busca pociones para prepararte para el siguiente combate."
                potions in 1..3 -> "Tus alforjas tienen $potions pociones listas."
                else -> "¡Eres una leyenda! El reino canta tus hazañas."
            }
        }
    }
}
