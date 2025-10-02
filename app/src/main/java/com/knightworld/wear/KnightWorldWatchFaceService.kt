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
import androidx.wear.watchface.Renderer.SharedAssets
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.ListenableWatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val INTERACTIVE_UPDATE_RATE_MS = 60_000L

class KnightWorldWatchFaceService : ListenableWatchFaceService() {

    override fun createUserStyleSchema(): UserStyleSchema = UserStyleSchema(emptyList())

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

        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer,
            complicationSlotsManager = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository
        )
    }

}

private class KnightWorldRenderer(
    private val appContext: Context,
    surfaceHolder: SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    watchState: WatchState
) : Renderer.CanvasRenderer2<KnightWorldRenderer.KnightWorldSharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    CanvasType.HARDWARE,
    INTERACTIVE_UPDATE_RATE_MS,
    false
) {

    // CORRECCIÓN 2: Mover las propiedades de Paint a SharedAssets
    class KnightWorldSharedAssets(context: Context) : SharedAssets {
        val backgroundPaint: Paint = Paint().apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.knight_background)
            isAntiAlias = true
        }
        val mapPaintLand: Paint = Paint().apply { /* ... */ }
        val mapPaintWater: Paint = Paint().apply { /* ... */ }
        val mapBorderPaint: Paint = Paint().apply { /* ... */ }
        val pathPaint: Paint = Paint().apply { /* ... */ }
        val primaryTextPaint: Paint = Paint().apply { /* ... */ }
        val mutedTextPaint: Paint = Paint().apply { /* ... */ }
        val surfacePaint: Paint = Paint().apply { /* ... */ }
        val healthPaint: Paint = Paint().apply { /* ... */ }
        val energyPaint: Paint = Paint().apply { /* ... */ }
        val moralePaint: Paint = Paint().apply { /* ... */ }

        // Inicializa aquí todos tus objetos Paint
        init {
            mapPaintLand.apply {
                style = Paint.Style.FILL
                color = ContextCompat.getColor(context, R.color.knight_map_land)
                isAntiAlias = true
            }
            mapPaintWater.apply {
                style = Paint.Style.FILL
                color = ContextCompat.getColor(context, R.color.knight_map_water)
                isAntiAlias = true
            }
            mapBorderPaint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                color = ContextCompat.getColor(context, R.color.knight_map_border)
                isAntiAlias = true
            }
            pathPaint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 5f
                color = ContextCompat.getColor(context, R.color.knight_map_path)
                isAntiAlias = true
            }
            primaryTextPaint.apply {
                color = Color.WHITE
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            mutedTextPaint.apply {
                color = ContextCompat.getColor(context, R.color.knight_map_border)
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            //... y así con el resto
        }

        override fun onDestroy() {
            // No es necesario hacer nada aquí si solo usas objetos Paint
        }
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

    override suspend fun createSharedAssets(): KnightWorldSharedAssets = KnightWorldSharedAssets(appContext)

    // CORRECCIÓN 3: Eliminar los métodos 'render' y 'renderHighlightLayer' obsoletos.
    // Deja solo los que reciben 'sharedAssets'.

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: KnightWorldSharedAssets
    ) {
        val stats = KnightWorldStatsProvider.fromTime(zonedDateTime)

        // Usa los 'Paint' desde sharedAssets
        canvas.drawRect(bounds, sharedAssets.backgroundPaint)
        drawMap(canvas, bounds, stats, sharedAssets) // Pasa sharedAssets a tus funciones de dibujo
        // Dibuja el resto de elementos...
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: KnightWorldSharedAssets
    ) {
        // Implementa la lógica de resaltado si es necesario, o déjalo vacío.
        canvas.drawColor(Color.TRANSPARENT)
    }

    private fun drawMap(canvas: Canvas, bounds: Rect, stats: KnightWorldStats, assets: KnightWorldSharedAssets) {
        val mapWidth = bounds.width() * 0.75f
        val mapHeight = bounds.height() * 0.4f
        val mapRect = RectF(
            bounds.exactCenterX() - mapWidth / 2f,
            bounds.exactCenterY() - mapHeight * 0.05f,
            bounds.exactCenterX() + mapWidth / 2f,
            bounds.exactCenterY() + mapHeight * 0.95f
        )

        // Usa los 'Paint' desde assets
        canvas.drawRoundRect(mapRect, 36f, 36f, assets.mapPaintWater)
        val landRect = RectF(
            mapRect.left + 16f,
            mapRect.top + 16f,
            mapRect.right - 16f,
            mapRect.bottom - 16f
        )
        canvas.drawRoundRect(landRect, 28f, 28f, assets.mapPaintLand)

        val route = Path().apply {
            moveTo(landRect.left + landRect.width() * 0.1f, landRect.bottom - landRect.height() * 0.15f)
            quadTo(
                landRect.centerX(),
                landRect.top + landRect.height() * 0.6f,
                landRect.right - landRect.width() * 0.15f,
                landRect.top + landRect.height() * 0.25f
            )
        }
        // canvas.drawPath(route, assets.pathPaint)
    }
}
