package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "nova_tv_preferences")

class TvPreferences(private val context: Context) {

    companion object {
        private val KEY_COUNTRY = stringPreferencesKey("selected_country")
        private val KEY_LANGUAGE = stringPreferencesKey("selected_language")
        private val KEY_SESSION_ID = stringPreferencesKey("anonymous_session_id")
        private val KEY_LAST_AD_TIMESTAMP = longPreferencesKey("last_ad_timestamp")
        private val KEY_INTRO_SHOWN = booleanPreferencesKey("intro_shown_once")
        private val KEY_PLAY_INTRO_PREF = booleanPreferencesKey("play_intro_preference")
    }

    // Default country is empty string to trigger country selection on first run, default language is "es" (Spanish)
    val countryFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_COUNTRY] ?: ""
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "es"
    }

    val sessionIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SESSION_ID] ?: run {
            val newUuid = UUID.randomUUID().toString()
            // We can't suspend write here, but the repository or viewmodel can generate and store it.
            newUuid
        }
    }

    val lastAdTimestampFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_AD_TIMESTAMP] ?: 0L
    }

    val introShownFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_INTRO_SHOWN] ?: false
    }

    val playIntroPrefFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_PLAY_INTRO_PREF] ?: true
    }

    suspend fun saveCountry(countryCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COUNTRY] = countryCode
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = languageCode
        }
    }

    suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SESSION_ID] = sessionId
        }
    }

    suspend fun saveLastAdTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_AD_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveIntroShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INTRO_SHOWN] = shown
        }
    }

    suspend fun savePlayIntroPref(playIntro: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PLAY_INTRO_PREF] = playIntro
        }
    }

    // Helper to get or generate session ID
    suspend fun getOrGenerateSessionId(): String {
        var id = ""
        context.dataStore.edit { preferences ->
            val currentId = preferences[KEY_SESSION_ID]
            if (currentId.isNullOrEmpty()) {
                val newId = UUID.randomUUID().toString()
                preferences[KEY_SESSION_ID] = newId
                id = newId
            } else {
                id = currentId
            }
        }
        return id
    }
}
