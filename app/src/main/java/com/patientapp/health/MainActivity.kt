package com.patientapp.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.patientapp.health.data.AuthRepository
import com.patientapp.health.data.DailyFormRepository
import com.patientapp.health.data.UserRepository
import com.patientapp.health.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = AuthRepository(applicationContext)
        val userRepository = UserRepository()
        val dailyFormRepository = DailyFormRepository()
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme(
                    primary = Color(0xFFD0BCFF),
                    primaryContainer = Color(0xFF4F378B),
                    onPrimary = Color(0xFF381E72)
                ) else lightColorScheme(
                    primary = Color(0xFF6750A4),
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimary = Color.White
                )
            ) {
                NavGraph(
                    authRepository = authRepository,
                    userRepository = userRepository,
                    dailyFormRepository = dailyFormRepository
                )
            }
        }
    }
}
