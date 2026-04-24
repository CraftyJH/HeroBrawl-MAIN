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
import com.herobrawl.game.ui.HeroBrawlShell
import com.herobrawl.game.ui.HeroBrawlTheme
import com.herobrawl.game.vm.GameViewModel

class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeroBrawlTheme {
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
