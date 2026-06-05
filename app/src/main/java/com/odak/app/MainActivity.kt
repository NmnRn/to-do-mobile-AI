package com.odak.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odak.app.ui.MainScreen
import com.odak.app.ui.theme.OdakTheme
import com.odak.app.ui.theme.ThemeMode
import com.odak.app.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeVm: ThemeViewModel = viewModel()
            val dark = when (themeVm.mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            OdakTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(themeVm)
                }
            }
        }
    }
}
