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

    override suspend fun createComplicationSlotsManager(
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
        )

    override fun createUserStyleSchema(context: Context): UserStyleSchema =
        UserStyleSchema(emptyList())
}

private fun Context.inflateWatchFaceFormat(@XmlRes formatResId: Int): WatchFaceFormat =
    WatchFaceFormat.inflateFromXml(this, formatResId)