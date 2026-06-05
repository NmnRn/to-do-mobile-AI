package com.odak.app.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odak.app.ui.components.CircularTimer
import com.odak.app.ui.components.ControlRow
import com.odak.app.ui.components.PrimaryControl
import com.odak.app.ui.components.SecondaryControl
import com.odak.app.ui.components.Stepper
import com.odak.app.util.TimeFormat

@Composable
fun PomodoroScreen(vm: PomodoroViewModel) {
    val isWork = vm.phase == PomoPhase.WORK
    val ringColor =
        if (isWork) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    // The end-of-phase alert is fired by TimerService so it works in the
    // background too; the screen just keeps the UI in sync.
    val onPhaseEnd: (PomoPhase) -> Unit = { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            vm.phase.label,
            style = MaterialTheme.typography.titleMedium,
            color = ringColor
        )
        Spacer(Modifier.height(20.dp))

        CircularTimer(progress = vm.progress, ringColor = ringColor) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = TimeFormat.clock(vm.remaining),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mod = vm.completedSessions % 4
                    val filled = if (mod == 0 && vm.completedSessions > 0) 4 else mod
                    repeat(4) { i ->
                        val on = i < filled
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (on) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Tamamlanan: ${vm.completedSessions} seans",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
        ControlRow {
            PrimaryControl(
                label = if (vm.running) "Duraklat" else "Başlat",
                icon = if (vm.running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                onClick = { vm.toggle(onPhaseEnd) }
            )
            SecondaryControl("Atla", Icons.Filled.SkipNext, onClick = { vm.skip(onPhaseEnd) })
        }
        Spacer(Modifier.height(12.dp))
        SecondaryControl("Sıfırla", Icons.Filled.Refresh, onClick = { vm.resetAll() })

        if (!vm.running) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Süreler (dakika)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Stepper("Odak", "${vm.workMin}", { vm.changeWork(-1) }, { vm.changeWork(1) })
            Stepper("Kısa mola", "${vm.shortMin}", { vm.changeShort(-1) }, { vm.changeShort(1) })
            Stepper("Uzun mola", "${vm.longMin}", { vm.changeLong(-1) }, { vm.changeLong(1) })
        }
    }
}
