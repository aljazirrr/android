package com.bariatric.assistant.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bariatric_prefs")

enum class SurgeryType(val displayName: String) {
    SLEEVE("Sleeve Gastrectomy"),
    BYPASS("Bypass Gastric"),
    BAND("Bandă Gastrică"),
    DUODENAL_SWITCH("Duodenal Switch"),
    MINI_BYPASS("Mini Bypass Gastric")
}

enum class PostOpStage(val displayName: String, val description: String) {
    IMMEDIATE("Post-operator imediat", "0-2 săptămâni"),
    ONE_TO_THREE_MONTHS("1-3 luni", "Tranziție la alimente moi"),
    THREE_TO_SIX_MONTHS("3-6 luni", "Reintroducere treptată"),
    SIX_TO_TWELVE_MONTHS("6-12 luni", "Consolidare"),
    BEYOND_TWELVE("12+ luni", "Menținere pe termen lung")
}

data class UserProfile(
    val userName: String = "",
    val surgeryType: SurgeryType? = null,
    val postOpStage: PostOpStage? = null,
    val onboardingCompleted: Boolean = false,
    val dailyWaterGoalMl: Int = 1500
)

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val SURGERY_TYPE = stringPreferencesKey("surgery_type")
        val POST_OP_STAGE = stringPreferencesKey("post_op_stage")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DAILY_WATER_GOAL_ML = intPreferencesKey("daily_water_goal_ml")
    }

    val userProfile: Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            userName = prefs[Keys.USER_NAME] ?: "",
            surgeryType = prefs[Keys.SURGERY_TYPE]?.let {
                try { SurgeryType.valueOf(it) } catch (_: Exception) { null }
            },
            postOpStage = prefs[Keys.POST_OP_STAGE]?.let {
                try { PostOpStage.valueOf(it) } catch (_: Exception) { null }
            },
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            dailyWaterGoalMl = prefs[Keys.DAILY_WATER_GOAL_ML] ?: 1500
        )
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun saveSurgeryType(type: SurgeryType) {
        dataStore.edit { it[Keys.SURGERY_TYPE] = type.name }
    }

    suspend fun savePostOpStage(stage: PostOpStage) {
        dataStore.edit { it[Keys.POST_OP_STAGE] = stage.name }
    }

    suspend fun completeOnboarding() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    suspend fun saveDailyWaterGoal(goalMl: Int) {
        dataStore.edit { it[Keys.DAILY_WATER_GOAL_ML] = goalMl }
    }
}
