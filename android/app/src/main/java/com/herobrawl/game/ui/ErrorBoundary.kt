package com.herobrawl.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shown if the app caught a top-level unhandled exception pre-composition.
 *
 * Compose does not support try/catch around composable functions, so this is
 * rendered by MainActivity only when `CrashLog.lastFatal` was populated before
 * `setContent` ran. Runtime exceptions after composition starts are logged to
 * logcat and the user can share via `adb logcat | grep HeroBrawl`.
 */
@Composable
fun ErrorScreen(t: Throwable) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B14))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "⚠️ HeroBrawl crashed",
            color = Color(0xFFFF9A9A),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            "Please screenshot this and open an issue so we can fix it. " +
                "Your save is untouched — try reopening the app.",
            color = Color(0xFFA8AED1),
            fontSize = 12.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF12131F))
                .border(1.dp, Color(0xFF2A2D45), RoundedCornerShape(10.dp))
                .padding(10.dp),
        ) {
            Text(
                t.stackTraceToString(),
                color = Color(0xFFE9ECFF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

/** Process-wide last-fatal capture used by MainActivity. */
object CrashLog {
    @Volatile var last: Throwable? = null
}
