package com.knightworld.wear

import android.content.Context
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.format.WatchFaceFormat
import androidx.wear.watchface.format.WatchFaceFormatRenderer
import androidx.wear.watchface.style.CurrentUserStyleRepository

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
        val format = WatchFaceFormat.loadFromXmlResource(
            context = applicationContext,
            resourceId = R.xml.knight_world_format
        )

        val renderer = KnightWorldFormatRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            currentUserStyleRepository = currentUserStyleRepository,
            watchState = watchState,
            watchFaceFormat = format
        )

        return WatchFace(WatchFaceType.DIGITAL, renderer, complicationSlotsManager)
    }
}

private class KnightWorldFormatRenderer(
    context: Context,
    surfaceHolder: SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    watchState: WatchState,
    watchFaceFormat: WatchFaceFormat
) : WatchFaceFormatRenderer(
    context,
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    watchFaceFormat,
    CanvasType.HARDWARE,
    INTERACTIVE_UPDATE_RATE_MS,
    false
)
