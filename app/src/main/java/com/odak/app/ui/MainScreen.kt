package com.odak.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odak.app.ui.tasks.TaskViewModel
import com.odak.app.ui.tasks.TasksScreen
import com.odak.app.ui.theme.ThemeMode
import com.odak.app.ui.theme.ThemeViewModel
import com.odak.app.ui.timer.CountdownViewModel
import com.odak.app.ui.timer.PomodoroScreen
import com.odak.app.ui.timer.PomodoroViewModel
import com.odak.app.ui.timer.StopwatchScreen
import com.odak.app.ui.timer.StopwatchViewModel
import com.odak.app.ui.timer.TimerScreen

private data class Tab(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(themeVm: ThemeViewModel) {
    val tabs = remember {
        listOf(
            Tab("Görevler", Icons.Filled.TaskAlt),
            Tab("Kronometre", Icons.Filled.Timer),
            Tab("Zamanlayıcı", Icons.Filled.HourglassEmpty),
            Tab("Pomodoro", Icons.Filled.Spa)
        )
    }
    var selected by remember { mutableIntStateOf(0) }

    // Hoisted here so timers keep running while switching tabs.
    val taskVm: TaskViewModel = viewModel()
    val stopwatchVm: StopwatchViewModel = viewModel()
    val countdownVm: CountdownViewModel = viewModel()
    val pomodoroVm: PomodoroViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs[selected].label) },
                actions = {
                    IconButton(onClick = { themeVm.cycle() }) {
                        val (icon, desc) = when (themeVm.mode) {
                            ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto to "Tema: Sistem"
                            ThemeMode.LIGHT -> Icons.Filled.LightMode to "Tema: Aydınlık"
                            ThemeMode.DARK -> Icons.Filled.DarkMode to "Tema: Karanlık"
                        }
                        Icon(icon, contentDescription = desc)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selected) {
                0 -> TasksScreen(taskVm)
                1 -> StopwatchScreen(stopwatchVm)
                2 -> TimerScreen(countdownVm)
                else -> PomodoroScreen(pomodoroVm)
            }
        }
    }
}
