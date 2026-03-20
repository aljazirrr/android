package com.radiowave.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.radiowave.app.core.ui.theme.RadioWaveTheme
import com.radiowave.app.features.profile.presentation.ProfileViewModel
import com.radiowave.app.navigation.RadioWaveNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val isDarkTheme by profileViewModel.isDarkTheme.collectAsState()

            RadioWaveTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                RadioWaveNavGraph(
                    navController = navController,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
