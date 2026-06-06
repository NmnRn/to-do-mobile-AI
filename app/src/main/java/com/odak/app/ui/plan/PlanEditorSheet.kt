package com.odak.app.ui.plan

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.odak.app.data.PlanBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorSheet(
    existing: PlanBlock?,
    onDismiss: () -> Unit,
    onSave: (startMinute: Int, endMinute: Int, title: String, note: String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var start by remember { mutableIntStateOf(existing?.startMinute ?: 9 * 60) }
    var end by remember { mutableIntStateOf(existing?.endMinute ?: 10 * 60) }

    fun pick(initial: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, h, m -> onPicked(h * 60 + m) },
            initial / 60, initial % 60, true
        ).show()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (existing == null) "Yeni Plan" else "Planı Düzenle",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            Text("Saat aralığı", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        pick(start) {
                            start = it
                            // Keep end after start.
                            if (end <= start) end = (start + 60).coerceAtMost(23 * 60 + 59)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null)
                    Text("  %02d:%02d".format(start / 60, start % 60))
                }
                Text("→")
                OutlinedButton(
                    onClick = { pick(end) { if (it > start) end = it } },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null)
                    Text("  %02d:%02d".format(end / 60, end % 60))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                durationLabel(end - start),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Ne yapılacak?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Not (isteğe bağlı)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSave(start, end, title, note) },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Text("  Kaydet", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun durationLabel(minutes: Int): String {
    if (minutes <= 0) return "Bitiş, başlangıçtan sonra olmalı"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "Süre: $h sa $m dk"
        h > 0 -> "Süre: $h sa"
        else -> "Süre: $m dk"
    }
}
