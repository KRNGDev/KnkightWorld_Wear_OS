package com.knightworld.wear

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val INTERACTIVE_UPDATE_RATE_MS = 60_000L

class KnightWorldWatchFaceService : WatchFaceService() {

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager =
        ComplicationSlotsManager(emptyList(), currentUserStyleRepository)

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = KnightWorldRenderer(
            applicationContext,
            surfaceHolder,
            currentUserStyleRepository,
            watchState
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer, complicationSlotsManager)
    }
}

private class KnightWorldRenderer(
    private val appContext: Context,
    surfaceHolder: SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    watchState: WatchState
) : Renderer.CanvasRenderer2(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    CanvasType.HARDWARE,
    INTERACTIVE_UPDATE_RATE_MS,
    false
) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(appContext, R.color.knight_background)
        isAntiAlias = true
    }

    private val mapPaintLand = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(appContext, R.color.knight_map_land)
        isAntiAlias = true
    }

    private val mapPaintWater = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(appContext, R.color.knight_map_water)
        isAntiAlias = true
    }

    private val mapBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = ContextCompat.getColor(appContext, R.color.knight_map_border)
        isAntiAlias = true
    }

    private val pathPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = ContextCompat.getColor(appContext, R.color.knight_map_path)
        isAntiAlias = true
    }

    private val primaryTextPaint = Paint().apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
    }

    private val mutedTextPaint = Paint().apply {
        color = ContextCompat.getColor(appContext, R.color.knight_map_border)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        isAntiAlias = true
    }

    private val surfacePaint = Paint().apply {
        color = ContextCompat.getColor(appContext, R.color.knight_surface)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val healthPaint = Paint().apply {
        color = ContextCompat.getColor(appContext, R.color.knight_health)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val energyPaint = Paint().apply {
        color = ContextCompat.getColor(appContext, R.color.knight_energy)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val moralePaint = Paint().apply {
        color = ContextCompat.getColor(appContext, R.color.knight_morale)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override suspend fun createSharedAssets(): SharedAssets = KnightWorldSharedAssets()

    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        val stats = KnightWorldStatsProvider.fromTime(zonedDateTime)

        canvas.drawRect(bounds, backgroundPaint)
        drawMap(canvas, bounds, stats)
        drawTimeAndDate(canvas, bounds, zonedDateTime)
        drawSoldierInfo(canvas, bounds, stats)
        drawStatBars(canvas, bounds, stats)
        drawMissionStatus(canvas, bounds, stats)
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime
    ) {
        // No special highlight layer content; draw nothing.
        canvas.drawColor(Color.TRANSPARENT)
    }

    private fun drawMap(canvas: Canvas, bounds: Rect, stats: KnightWorldStats) {
        val mapWidth = bounds.width() * 0.75f
        val mapHeight = bounds.height() * 0.4f
        val mapRect = RectF(
            bounds.exactCenterX() - mapWidth / 2f,
            bounds.exactCenterY() - mapHeight * 0.05f,
            bounds.exactCenterX() + mapWidth / 2f,
            bounds.exactCenterY() + mapHeight * 0.95f
        )

        canvas.drawRoundRect(mapRect, 36f, 36f, mapPaintWater)
        val landRect = RectF(
            mapRect.left + 16f,
            mapRect.top + 16f,
            mapRect.right - 16f,
            mapRect.bottom - 16f
        )
        canvas.drawRoundRect(landRect, 28f, 28f, mapPaintLand)

        val route = Path().apply {
            moveTo(landRect.left + landRect.width() * 0.1f, landRect.bottom - landRect.height() * 0.15f)
            quadTo(
                landRect.centerX(),
                landRect.top + landRect.height() * 0.6f,
                landRect.right - landRect.width() * 0.15f,
                landRect.top + landRect.height() * 0.25f
            )
        }
        canvas.drawPath(route, pathPaint)
        canvas.drawRoundRect(mapRect, 36f, 36f, mapBorderPaint)

        mutedTextPaint.textSize = bounds.height() * 0.045f
        val zoneText = appContext.getString(R.string.watchface_zone, stats.zone)
        val zoneWidth = mutedTextPaint.measureText(zoneText)
        canvas.drawText(zoneText, mapRect.centerX() - zoneWidth / 2f, mapRect.bottom - bounds.height() * 0.02f, mutedTextPaint)
        primaryTextPaint.textSize = bounds.height() * 0.05f
        val missionText = stats.mission.uppercase(Locale.getDefault())
        val missionWidth = primaryTextPaint.measureText(missionText)
        canvas.drawText(missionText, mapRect.centerX() - missionWidth / 2f, mapRect.top + bounds.height() * 0.055f, primaryTextPaint)
    }

    private fun drawTimeAndDate(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        val centerX = bounds.exactCenterX()
        primaryTextPaint.textSize = bounds.height() * 0.18f
        val timeText = zonedDateTime.format(timeFormatter)
        val timeWidth = primaryTextPaint.measureText(timeText)
        canvas.drawText(timeText, centerX - timeWidth / 2f, bounds.height() * 0.28f, primaryTextPaint)

        mutedTextPaint.textSize = bounds.height() * 0.06f
        val dateText = zonedDateTime.format(dateFormatter).replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
        }
        val dateWidth = mutedTextPaint.measureText(dateText)
        canvas.drawText(dateText, centerX - dateWidth / 2f, bounds.height() * 0.36f, mutedTextPaint)
    }

    private fun drawSoldierInfo(canvas: Canvas, bounds: Rect, stats: KnightWorldStats) {
        val padding = bounds.width() * 0.08f
        primaryTextPaint.textSize = bounds.height() * 0.065f
        mutedTextPaint.textSize = bounds.height() * 0.045f

        val soldierNameLabel = appContext.getString(R.string.watchface_soldier_name)
        canvas.drawText(soldierNameLabel.uppercase(Locale.getDefault()), padding, bounds.height() * 0.12f, mutedTextPaint)
        canvas.drawText(stats.soldierName, padding, bounds.height() * 0.18f, primaryTextPaint)

        val rankLabel = appContext.getString(R.string.watchface_soldier_rank).uppercase(Locale.getDefault())
        canvas.drawText(rankLabel, padding, bounds.height() * 0.24f, mutedTextPaint)
        primaryTextPaint.textSize = bounds.height() * 0.055f
        canvas.drawText(stats.rank, padding, bounds.height() * 0.29f, primaryTextPaint)
        mutedTextPaint.textSize = bounds.height() * 0.045f
        val missionLabel = appContext.getString(R.string.watchface_soldier_mission).uppercase(Locale.getDefault())
        canvas.drawText(missionLabel, padding, bounds.height() * 0.34f, mutedTextPaint)
        primaryTextPaint.textSize = bounds.height() * 0.05f
        canvas.drawText(stats.mission, padding, bounds.height() * 0.39f, primaryTextPaint)
    }

    private fun drawStatBars(canvas: Canvas, bounds: Rect, stats: KnightWorldStats) {
        val barLeft = bounds.width() * 0.1f
        val barRight = bounds.width() * 0.9f
        val barHeight = bounds.height() * 0.045f
        val verticalSpacing = bounds.height() * 0.03f
        val topStart = bounds.height() * 0.68f

        drawStatBar(
            canvas,
            appContext.getString(R.string.watchface_health),
            barLeft,
            barRight,
            topStart,
            barHeight,
            stats.healthPercent / 100f,
            healthPaint
        )

        drawStatBar(
            canvas,
            appContext.getString(R.string.watchface_energy),
            barLeft,
            barRight,
            topStart + barHeight + verticalSpacing,
            barHeight,
            stats.energyPercent / 100f,
            energyPaint
        )

        drawStatBar(
            canvas,
            appContext.getString(R.string.watchface_morale),
            barLeft,
            barRight,
            topStart + (barHeight + verticalSpacing) * 2,
            barHeight,
            stats.moralePercent / 100f,
            moralePaint
        )
    }

    private fun drawStatBar(
        canvas: Canvas,
        label: String,
        left: Float,
        right: Float,
        top: Float,
        height: Float,
        progress: Float,
        fillPaint: Paint
    ) {
        mutedTextPaint.textSize = height * 0.6f
        canvas.drawText(label.uppercase(Locale.getDefault()), left, top - height * 0.3f, mutedTextPaint)

        val trackRect = RectF(left, top, right, top + height)
        canvas.drawRoundRect(trackRect, height / 2f, height / 2f, surfacePaint)
        val indicatorRect = RectF(left, top, left + (right - left) * progress, top + height)
        canvas.drawRoundRect(indicatorRect, height / 2f, height / 2f, fillPaint)

        primaryTextPaint.textSize = height * 0.7f
        val percentageText = "${(progress * 100).toInt()}%"
        val textWidth = primaryTextPaint.measureText(percentageText)
        canvas.drawText(percentageText, right - textWidth, top + height * 0.8f, primaryTextPaint)
    }

    private fun drawMissionStatus(canvas: Canvas, bounds: Rect, stats: KnightWorldStats) {
        mutedTextPaint.textSize = bounds.height() * 0.038f
        val reportLabel = appContext.getString(R.string.watchface_soldier_report_label).uppercase(Locale.getDefault())
        val labelWidth = mutedTextPaint.measureText(reportLabel)
        val labelY = bounds.height() * 0.83f
        canvas.drawText(reportLabel, bounds.exactCenterX() - labelWidth / 2f, labelY, mutedTextPaint)

        mutedTextPaint.textSize = bounds.height() * 0.042f
        val reportText = appContext.getString(
            R.string.watchface_soldier_report,
            stats.victories,
            stats.defeats,
            stats.potions
        )
        val reportWidth = mutedTextPaint.measureText(reportText)
        val reportY = bounds.height() * 0.88f
        canvas.drawText(reportText, bounds.exactCenterX() - reportWidth / 2f, reportY, mutedTextPaint)

        primaryTextPaint.textSize = bounds.height() * 0.05f
        val statusText = appContext.getString(R.string.watchface_status, stats.status)
        val statusWidth = primaryTextPaint.measureText(statusText)
        canvas.drawText(statusText, bounds.exactCenterX() - statusWidth / 2f, bounds.height() * 0.95f, primaryTextPaint)
    }
}

private class KnightWorldSharedAssets : Renderer.SharedAssets {
    override fun onDestroy() {
        // No-op
    }
}
