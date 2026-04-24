package com.herobrawl.game.store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.herobrawl.game.engine.Idle
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.IdleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "herobrawl_save_v2")

object SaveStore {
    private val SAVE_KEY: Preferences.Key<String> = stringPreferencesKey("save")
    private val json = Json { ignoreUnknownKeys = true }

    fun observe(context: Context): Flow<GameState?> =
        context.dataStore.data.map { prefs ->
            prefs[SAVE_KEY]?.let { runCatching { json.decodeFromString<GameState>(it) }.getOrNull() }
        }

    suspend fun load(context: Context): GameState? =
        observe(context).first()

    suspend fun save(context: Context, state: GameState) {
        val encoded = json.encodeToString(state)
        context.dataStore.edit { it[SAVE_KEY] = encoded }
    }

    suspend fun wipe(context: Context) {
        context.dataStore.edit { it.remove(SAVE_KEY) }
    }

    fun newGame(now: Long): GameState {
        return GameState(
            createdAt = now,
            idle = IdleState(
                startedAt = now,
                tickedAt = now,
                ratePerHour = Idle.upgradedRates(1, 1),
            ),
        )
    }
}
