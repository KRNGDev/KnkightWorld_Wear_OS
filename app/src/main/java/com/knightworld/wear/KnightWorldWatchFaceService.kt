package com.knightworld.wear

import android.content.Context
import androidx.annotation.XmlRes
import androidx.wear.watchface.format.WatchFaceFormat
import androidx.wear.watchface.format.WatchFaceFormatService
import androidx.wear.watchface.format.complications.ComplicationDataSourcePolicy
import androidx.wear.watchface.format.complications.ComplicationSlotPolicy
import androidx.wear.watchface.format.complications.ComplicationSlotRequirement
import androidx.wear.watchface.format.style.UserStyleSchema

class KnightWorldWatchFaceService : WatchFaceFormatService() {

    override fun createWatchFaceFormat(context: Context): WatchFaceFormat =
        context.inflateWatchFaceFormat(R.xml.knight_world_watchface)

    override fun createComplicationDataSourcePolicy(context: Context): ComplicationDataSourcePolicy =
        ComplicationDataSourcePolicy(
            mapOf(
                "left_complication" to ComplicationSlotPolicy(
                    supportedTypes = listOf(ComplicationSlotRequirement.DigitalText),
                    fallbackProvider = null
                ),
                "right_complication" to ComplicationSlotPolicy(
                    supportedTypes = listOf(ComplicationSlotRequirement.DigitalText),
                    fallbackProvider = null
                )
            )
        )

    override fun createUserStyleSchema(context: Context): UserStyleSchema =
        UserStyleSchema(emptyList())
}

private fun Context.inflateWatchFaceFormat(@XmlRes formatResId: Int): WatchFaceFormat =
    WatchFaceFormat.inflateFromXml(this, formatResId)
