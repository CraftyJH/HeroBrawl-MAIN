package com.herobrawl.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herobrawl.game.ui.CrashLog
import com.herobrawl.game.ui.ErrorScreen
import com.herobrawl.game.ui.HeroBrawlShell
import com.herobrawl.game.ui.HeroBrawlTheme
import com.herobrawl.game.vm.GameViewModel

class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log any uncaught runtime crash before the process dies, so the user
        // can share the trace via `adb logcat | grep HeroBrawl` if the app
        // hard-crashes before composition.
        val priorHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            android.util.Log.e("HeroBrawl", "Uncaught in $t", e)
            CrashLog.last = e
            priorHandler?.uncaughtException(t, e)
        }

        setContent {
            HeroBrawlTheme {
                val fatal = CrashLog.last
                if (fatal != null) {
                    ErrorScreen(fatal)
                    return@HeroBrawlTheme
                }
                val state by vm.state.collectAsStateWithLifecycle()
                val current = state
                if (current != null) {
                    HeroBrawlShell(
                        state = current,
                        update = vm::update,
                        notify = vm::notify,
                        reset = vm::reset,
                    )
                }
            }
        }
    }
}
