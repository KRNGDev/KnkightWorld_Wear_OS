package com.knightworld.wear

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.knightworld.wear.R
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceLayer
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

class KnightWorldWatchFaceService : WatchFaceService() {

    override suspend fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager = ComplicationSlotsManager(emptyList(), currentUserStyleRepository)

    override suspend fun createWatchFace(
        surfaceHolder: android.view.SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = KnightWorldCanvasRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            currentUserStyleRepository = currentUserStyleRepository,
            watchState = watchState
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer, complicationSlotsManager)
    }
}

private class KnightWorldCanvasRenderer(
    private val context: Context,
    surfaceHolder: android.view.SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    watchState: WatchState
) : Renderer.CanvasRenderer2<KnightWorldCanvasRenderer.SharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    CanvasType.HARDWARE,
    INTERACTIVE_UPDATE_RATE_MS,
    false
) {

    private val palette = Palette(context)

    private val timePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.primary
        textSize = context.resources.displayMetrics.density * 44f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.onSurface
        textSize = context.resources.displayMetrics.density * 18f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    private val infoPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.onSurface
        textSize = context.resources.displayMetrics.density * 16f
        textAlign = Paint.Align.CENTER
    }
    private val statPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.onSurface
        textSize = context.resources.displayMetrics.density * 15f
        textAlign = Paint.Align.CENTER
    }
    private val narrativePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.onSurfaceMuted
        textSize = context.resources.displayMetrics.density * 14f
        textAlign = Paint.Align.LEFT
    }

    private val barBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.surface
    }
    private val barFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val batteryLevel = AtomicInteger(100)
    @Volatile private var steps: Int = 0
    @Volatile private var stats: KnightWorldStats = KnightWorldStats.fromSensors(100, 0)

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: return
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                val percent = ((level / scale.toFloat()) * 100).toInt()
                batteryLevel.set(percent)
                updateStats()
            }
        }
    }

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    @Volatile private var baseSteps: Float? = null

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values.firstOrNull() ?: return
            if (baseSteps == null) {
                baseSteps = value
            }
            val total = max(0f, value - (baseSteps ?: 0f))
            steps = total.toInt()
            updateStats()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    init {
        registerReceivers()
        registerStepSensor()
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        if (!renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE)) {
            return
        }
        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT
        val backgroundColor = if (isAmbient) palette.ambientBackground else palette.background
        canvas.drawColor(backgroundColor)

        val centerX = bounds.exactCenterX().toFloat()
        val primaryColor = if (isAmbient) Color.WHITE else palette.primary
        val textColor = if (isAmbient) Color.WHITE else palette.onSurface
        timePaint.color = primaryColor
        datePaint.color = if (isAmbient) Color.LTGRAY else palette.onSurface
        infoPaint.color = textColor
        statPaint.color = textColor
        narrativePaint.color = if (isAmbient) Color.LTGRAY else palette.onSurfaceMuted
        timePaint.isAntiAlias = !isAmbient
        datePaint.isAntiAlias = !isAmbient
        infoPaint.isAntiAlias = !isAmbient
        statPaint.isAntiAlias = !isAmbient
        narrativePaint.isAntiAlias = !isAmbient
        barBackgroundPaint.isAntiAlias = !isAmbient

        val statsSnapshot = stats
        val lineSpacing = context.resources.displayMetrics.density * 12f
        var currentTop = bounds.top + context.resources.displayMetrics.density * 28f

        currentTop = drawCenteredText(canvas, zonedDateTime.format(timeFormatter), centerX, currentTop, timePaint)
        currentTop += lineSpacing * 0.5f
        currentTop = drawCenteredText(canvas, zonedDateTime.format(dateFormatter).uppercase(Locale.getDefault()), centerX, currentTop, datePaint)
        currentTop += lineSpacing

        val headerText = context.getString(
            R.string.watchface_header,
            statsSnapshot.steps,
            batteryLevel.get().coerceIn(0, 100)
        )
        currentTop = drawCenteredText(canvas, headerText, centerX, currentTop, infoPaint)
        currentTop += lineSpacing

        val victoryLine = context.getString(
            R.string.watchface_stats,
            statsSnapshot.battlesWon,
            statsSnapshot.battlesLost,
            statsSnapshot.potions
        )
        currentTop = drawCenteredText(canvas, victoryLine, centerX, currentTop, statPaint)
        currentTop += lineSpacing

        if (!isAmbient) {
            currentTop = drawProgressBar(
                canvas = canvas,
                centerX = centerX,
                top = currentTop,
                width = bounds.width() * 0.7f,
                label = context.getString(R.string.watchface_health),
                progress = statsSnapshot.healthPercent,
                color = palette.health
            )
            currentTop += lineSpacing
            currentTop = drawProgressBar(
                canvas = canvas,
                centerX = centerX,
                top = currentTop,
                width = bounds.width() * 0.7f,
                label = context.getString(R.string.watchface_energy),
                progress = statsSnapshot.energyPercent,
                color = palette.energy
            )
            currentTop += lineSpacing
        }

        val narrative = statsSnapshot.narrative
        drawNarrative(canvas, narrative, bounds, currentTop)
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        // No highlight layer rendering required for this watch face.
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver was already unregistered.
        }
        sensorManager.unregisterListener(stepListener)
    }

    override fun createSharedAssets(): SharedAssets = SharedAssets()

    class SharedAssets : Renderer.SharedAssets {
        override fun onDestroy() = Unit
    }

    private fun registerReceivers() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = context.registerReceiver(batteryReceiver, filter)
        if (sticky != null) {
            val level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                val percent = ((level / scale.toFloat()) * 100).toInt()
                batteryLevel.set(percent)
                updateStats()
            }
        }
    }

    private fun registerStepSensor() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            return
        }
        stepSensor ?: return
        try {
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } catch (security: SecurityException) {
            // Permission revoked while registering; ignore and continue without steps.
        }
    }

    private fun updateStats() {
        stats = KnightWorldStats.fromSensors(batteryLevel.get(), steps)
        invalidate()
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        centerX: Float,
        top: Float,
        paint: TextPaint
    ): Float {
        val metrics = paint.fontMetrics
        val baseline = top - metrics.ascent
        canvas.drawText(text, centerX, baseline, paint)
        return top + (metrics.descent - metrics.ascent)
    }

    private fun drawProgressBar(
        canvas: Canvas,
        centerX: Float,
        top: Float,
        width: Float,
        label: String,
        progress: Float,
        color: Int
    ): Float {
        val barHeight = context.resources.displayMetrics.density * 12f
        val left = centerX - width / 2f
        val right = centerX + width / 2f
        val labelBaseline = top - context.resources.displayMetrics.density * 6f
        canvas.drawText(label, centerX, labelBaseline, statPaint)
        val rect = RectF(left, top, right, top + barHeight)
        canvas.drawRoundRect(rect, barHeight / 2f, barHeight / 2f, barBackgroundPaint)
        barFillPaint.color = color
        rect.right = left + width * progress.coerceIn(0f, 1f)
        canvas.drawRoundRect(rect, barHeight / 2f, barHeight / 2f, barFillPaint)
        return top + barHeight
    }

    private fun drawNarrative(canvas: Canvas, text: String, bounds: Rect, top: Float) {
        if (text.isBlank()) return
        val availableWidth = (bounds.width() * 0.78f).toInt()
        val x = bounds.left + (bounds.width() - availableWidth) / 2f
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, narrativePaint, availableWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .build()
        canvas.save()
        canvas.translate(x, top)
        layout.draw(canvas)
        canvas.restore()
    }

    companion object {
        private const val INTERACTIVE_UPDATE_RATE_MS = 1_000L
    }
}

private class Palette(context: Context) {
    val background: Int = ContextCompat.getColor(context, R.color.knight_background)
    val ambientBackground: Int = Color.BLACK
    val primary: Int = ContextCompat.getColor(context, R.color.knight_primary)
    val surface: Int = ContextCompat.getColor(context, R.color.knight_surface)
    val onSurface: Int = Color.WHITE
    val onSurfaceMuted: Int = 0xCCFFFFFF.toInt()
    val health: Int = ContextCompat.getColor(context, R.color.knight_health)
    val energy: Int = ContextCompat.getColor(context, R.color.knight_energy)
}
