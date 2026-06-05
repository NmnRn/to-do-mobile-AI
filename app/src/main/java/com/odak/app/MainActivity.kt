package com.odak.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odak.app.ui.MainScreen
import com.odak.app.ui.theme.OdakTheme
import com.odak.app.ui.theme.ThemeMode
import com.odak.app.ui.theme.ThemeViewModel
import com.odak.app.util.Alert

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAlarmWindowFlags(intent)
        maybeRequestNotificationPermission()
        maybeRequestBatteryExemption()
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyAlarmWindowFlags(intent)
    }

    /** When launched by the finish alarm, wake the screen and show over the lock screen. */
    private fun applyAlarmWindowFlags(intent: Intent?) {
        if (intent?.getBooleanExtra(Alert.EXTRA_FROM_ALARM, false) != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            keyguard?.requestDismissKeyguard(this, null)
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Asks the user once to exempt Odak from battery optimization so reminders and
     * the finish alarm keep firing when the phone is locked / dozing.
     */
    private fun maybeRequestBatteryExemption() {
        val prefs = getSharedPreferences("odak_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("battery_opt_asked", false)) return
        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        if (pm.isIgnoringBatteryOptimizations(packageName)) return
        prefs.edit().putBoolean("battery_opt_asked", true).apply()
        runCatching {
            startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }
}
