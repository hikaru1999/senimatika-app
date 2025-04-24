package com.LambdaProject.MathArt

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val KEY_INTRO_SHOWN = booleanPreferencesKey("intro_shown")
    }

    val isIntroShown: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_INTRO_SHOWN] ?: false
        }

    suspend fun setIntroShown(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INTRO_SHOWN] = value
        }
    }
}