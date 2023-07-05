package com.example.estsharabot.utility

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreHelper(context: Context) {

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "USER_KEY"
    )
    private val dataStore = context.userPreferencesDataStore

    suspend fun setUserData(email: String, password: String) {
        try {
            Log.d("DATA_STORE", "Data Stored!")
            dataStore.edit { USER_KEY ->
                USER_KEY[USER_EMAIL] = email
                USER_KEY[USER_PASSWORD] = password
            }

        } catch (e: Exception) {
            Log.d("DATA_STORE", "Storing Failed due to : ${e.message}")

        }

    }


    fun getUserEmail(): Flow<String> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val email = preferences[USER_EMAIL] ?: "N/A"
            email
        }
    }

    fun getUserPassword(): Flow<String> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
            .map { preferences ->
                val password = preferences[USER_PASSWORD] ?: "N/A"
                password
            }
    }


    companion object {
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_PASSWORD = stringPreferencesKey("user_password")
    }
}