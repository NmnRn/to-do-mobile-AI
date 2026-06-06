package com.odak.app.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odak.app.R
import com.odak.app.ui.components.ControlRow
import com.odak.app.ui.components.CircularTimer
import com.odak.app.ui.components.PrimaryControl
import com.odak.app.ui.components.SecondaryControl
import com.odak.app.util.TimeFormat

@Composable
fun StopwatchScreen(vm: StopwatchViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        CircularTimer(progress = (vm.elapsed % 60_000L) / 60_000f) {
            Text(
                text = TimeFormat.stopwatch(vm.elapsed),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(36.dp))
        ControlRow {
            PrimaryControl(
                label = stringResource(if (vm.running) R.string.pause else R.string.start),
                icon = if (vm.running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                onClick = { vm.toggle() }
            )
            if (vm.running) {
                SecondaryControl(stringResource(R.string.lap), Icons.Filled.Flag, onClick = { vm.lap() })
            } else {
                SecondaryControl(stringResource(R.string.reset), Icons.Filled.Refresh, onClick = { vm.reset() })
            }
        }

        if (vm.laps.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            LazyColumn(Modifier.fillMaxWidth()) {
                itemsIndexed(vm.laps) { index, lap ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.lap_n, vm.laps.size - index),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            TimeFormat.stopwatch(lap),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}
