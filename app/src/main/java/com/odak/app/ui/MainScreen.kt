package com.odak.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odak.app.ui.plan.PlanScreen
import com.odak.app.ui.plan.PlanViewModel
import com.odak.app.ui.settings.SettingsScreen
import com.odak.app.ui.settings.UpdateViewModel
import com.odak.app.ui.tasks.TaskViewModel
import com.odak.app.ui.tasks.TasksScreen
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
            Tab("Plan", Icons.Filled.CalendarMonth),
            Tab("Kronometre", Icons.Filled.Timer),
            Tab("Zamanlayıcı", Icons.Filled.HourglassEmpty),
            Tab("Pomodoro", Icons.Filled.Spa)
        )
    }
    var selected by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    // Hoisted here so timers keep running while switching tabs.
    val taskVm: TaskViewModel = viewModel()
    val planVm: PlanViewModel = viewModel()
    val stopwatchVm: StopwatchViewModel = viewModel()
    val countdownVm: CountdownViewModel = viewModel()
    val pomodoroVm: PomodoroViewModel = viewModel()
    val updateVm: UpdateViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showSettings) "Ayarlar" else tabs[selected].label) },
                navigationIcon = {
                    if (showSettings) {
                        IconButton(onClick = { showSettings = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                actions = {
                    if (!showSettings) {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index && !showSettings,
                        onClick = { selected = index; showSettings = false },
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
            if (showSettings) {
                SettingsScreen(themeVm, updateVm)
            } else {
                when (selected) {
                    0 -> TasksScreen(taskVm)
                    1 -> PlanScreen(planVm)
                    2 -> StopwatchScreen(stopwatchVm)
                    3 -> TimerScreen(countdownVm)
                    else -> PomodoroScreen(pomodoroVm)
                }
            }
        }
    }
}
