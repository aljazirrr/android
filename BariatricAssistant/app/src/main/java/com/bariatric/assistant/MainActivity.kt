package com.bariatric.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bariatric.assistant.core.data.preferences.UserPreferences
import com.bariatric.assistant.core.ui.theme.BariatricAssistantTheme
import com.bariatric.assistant.navigation.BariatricNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BariatricAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userProfile by userPreferences.userProfile.collectAsState(
                        initial = null
                    )

                    userProfile?.let { profile ->
                        BariatricNavGraph(
                            startOnboarding = !profile.onboardingCompleted
                        )
                    }
                }
            }
        }
    }
}
