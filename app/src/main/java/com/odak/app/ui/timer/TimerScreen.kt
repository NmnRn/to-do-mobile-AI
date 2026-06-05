package com.odak.app.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odak.app.ui.components.CircularTimer
import com.odak.app.ui.components.ControlRow
import com.odak.app.ui.components.PrimaryControl
import com.odak.app.ui.components.SecondaryControl
import com.odak.app.ui.components.Stepper
import com.odak.app.util.Alert
import com.odak.app.util.TimeFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(vm: CountdownViewModel) {
    val context = LocalContext.current
    val presets = listOf(1, 3, 5, 10, 15, 25, 45, 60)
    val isIdle = !vm.running && vm.remaining == vm.totalMillis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        CircularTimer(progress = vm.progress) {
            Text(
                text = TimeFormat.clock(vm.remaining),
                fontSize = 46.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(20.dp))

        if (isIdle) {
            Stepper(
                label = "Dakika",
                value = "${vm.totalSeconds / 60}",
                onMinus = { vm.changeSeconds(-60) },
                onPlus = { vm.changeSeconds(60) }
            )
            Stepper(
                label = "Saniye",
                value = "${vm.totalSeconds % 60}",
                onMinus = { vm.changeSeconds(-5) },
                onPlus = { vm.changeSeconds(5) }
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.forEach { min ->
                    FilterChip(
                        selected = vm.totalMillis == min * 60_000L,
                        onClick = { vm.setMinutes(min) },
                        label = { Text("$min dk") }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        ControlRow {
            PrimaryControl(
                label = if (vm.running) "Duraklat" else "Başlat",
                icon = if (vm.running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                onClick = {
                    vm.toggle {
                        Alert.fire(context, "Süre doldu", "Zamanlayıcı tamamlandı", id = 1001)
                    }
                }
            )
            SecondaryControl("Sıfırla", Icons.Filled.Refresh, onClick = { vm.reset() })
        }
    }
}
