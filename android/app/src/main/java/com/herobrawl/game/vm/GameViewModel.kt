package com.herobrawl.game.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.herobrawl.game.engine.Achievements
import com.herobrawl.game.engine.DailyLogin
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.model.GameMessage
import com.herobrawl.game.model.GameState
import com.herobrawl.game.store.SaveStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow<GameState?>(null)
    val state: StateFlow<GameState?> = _state.asStateFlow()

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            val loaded = SaveStore.load(getApplication())
            val now = System.currentTimeMillis()
            val initial = loaded ?: SaveStore.newGame(now)
            val rolled = Quests.ensureRollover(Achievements.evaluate(initial), now)
            val greeted = DailyLogin.checkIn(rolled, now)
            _state.value = greeted
        }
    }

    fun update(fn: (GameState) -> GameState) {
        val cur = _state.value ?: return
        val next = Achievements.evaluate(
            Quests.ensureRollover(fn(cur), System.currentTimeMillis())
        )
        _state.value = next
        scheduleSave(next)
    }

    fun notify(text: String, kind: String = "info") {
        update { s ->
            val msg = GameMessage(
                id = UUID.randomUUID().toString(),
                at = System.currentTimeMillis(),
                text = text,
                kind = kind,
            )
            s.copy(messages = (listOf(msg) + s.messages).take(30))
        }
    }

    fun reset() {
        val now = System.currentTimeMillis()
        val fresh = SaveStore.newGame(now)
        _state.value = fresh
        scheduleSave(fresh)
    }

    private fun scheduleSave(state: GameState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            SaveStore.save(getApplication(), state)
        }
    }
}
