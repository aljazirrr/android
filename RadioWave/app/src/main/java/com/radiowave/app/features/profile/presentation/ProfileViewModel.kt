package com.radiowave.app.features.profile.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiowave.app.core.domain.model.UserProfile
import com.radiowave.app.features.auth.domain.repository.AuthRepository
import com.radiowave.app.features.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isDarkTheme: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = dataStore.data
        .map { prefs -> prefs[DARK_THEME_KEY] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val userProfile: StateFlow<UserProfile?> = profileRepository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleTheme() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[DARK_THEME_KEY] = !(prefs[DARK_THEME_KEY] ?: true)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
