package com.odak.app.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.odak.app.OdakApp
import com.odak.app.reminder.Reminders
import com.odak.app.task.TaskAlarms
import com.odak.app.ui.theme.ThemeMode
import com.odak.app.ui.theme.ThemeViewModel
import com.odak.app.util.Backup
import com.odak.app.util.DateUtils
import com.odak.app.util.Reports
import com.odak.app.util.WeeklyReport
import com.odak.app.widget.TodayWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(themeVm: ThemeViewModel, updateVm: UpdateViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // ---- Görünüm ----
        SectionTitle("Görünüm")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip("Sistem", themeVm.mode == ThemeMode.SYSTEM) { themeVm.set(ThemeMode.SYSTEM) }
            ThemeChip("Aydınlık", themeVm.mode == ThemeMode.LIGHT) { themeVm.set(ThemeMode.LIGHT) }
            ThemeChip("Karanlık", themeVm.mode == ThemeMode.DARK) { themeVm.set(ThemeMode.DARK) }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        // ---- Bildirimler ----
        SectionTitle("Bildirimler")
        val context = LocalContext.current
        var remindersOn by remember { mutableStateOf(Reminders.isEnabled(context)) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Görev hatırlatmaları",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Bekleyen görevlerin için düzenli hatırlatma",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.size(12.dp))
            Switch(
                checked = remindersOn,
                onCheckedChange = {
                    remindersOn = it
                    Reminders.setEnabled(context, it)
                }
            )
        }

        if (remindersOn) {
            var interval by remember { mutableIntStateOf(Reminders.intervalHours(context)) }
            var quietStart by remember { mutableIntStateOf(Reminders.quietStart(context)) }
            var quietEnd by remember { mutableIntStateOf(Reminders.quietEnd(context)) }

            Spacer(Modifier.height(16.dp))
            Text(
                "Sıklık",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Reminders.INTERVAL_OPTIONS.forEach { hours ->
                    FilterChip(
                        selected = interval == hours,
                        onClick = {
                            interval = hours
                            Reminders.setIntervalHours(context, hours)
                        },
                        label = { Text("$hours saat") }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Sessiz saatler",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Bu aralıkta hatırlatma gönderilmez",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        showHourPicker(context, quietStart) { h ->
                            quietStart = h
                            Reminders.setQuietHours(context, h, quietEnd)
                        }
                    }) { Text(hourLabel(quietStart)) }
                    Text("–", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        showHourPicker(context, quietEnd) { h ->
                            quietEnd = h
                            Reminders.setQuietHours(context, quietStart, h)
                        }
                    }) { Text(hourLabel(quietEnd)) }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        // ---- Odak raporu ----
        SectionTitle("Bu hafta")
        val app = context.applicationContext as OdakApp
        val scope = rememberCoroutineScope()
        var reloadKey by remember { mutableIntStateOf(0) }
        var report by remember { mutableStateOf<WeeklyReport?>(null) }
        LaunchedEffect(reloadKey) {
            report = Reports.weekly(app.repository)
        }
        report?.let { r ->
            Text(
                "${r.totalDone}/${r.totalTasks} görev tamamlandı · ${r.focusMinutes} dk odak",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            WeekBars(r)
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        // ---- Yedekleme ----
        SectionTitle("Yedekleme")
        val exportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null) scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        val json = Backup.toJson(app.repository.all())
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(json.toByteArray())
                        }
                        true
                    }.getOrDefault(false)
                }
                Toast.makeText(
                    context,
                    if (ok) "Yedek kaydedildi" else "Yedek alınamadı",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        val text = context.contentResolver.openInputStream(uri)?.use {
                            it.readBytes().decodeToString()
                        } ?: return@runCatching false
                        app.repository.replaceAll(Backup.fromJson(text))
                        TaskAlarms.rescheduleAll(context, app.repository.timedTasks())
                        true
                    }.getOrDefault(false)
                }
                TodayWidget.refresh(context)
                reloadKey++
                Toast.makeText(
                    context,
                    if (ok) "Yedek geri yüklendi" else "Geri yükleme başarısız",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        Text(
            "Görevlerini JSON dosyası olarak dışa aktar ya da geri yükle (fotoğraflar hariç).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { exportLauncher.launch("odak-yedek.json") },
                modifier = Modifier.weight(1f)
            ) { Text("Dışa aktar") }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.weight(1f)
            ) { Text("Geri yükle") }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        // ---- Güncelleme ----
        SectionTitle("Güncelleme")
        Text(
            "Sürüm ${updateVm.versionName} · #${updateVm.currentSha}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        when (val s = updateVm.state) {
            is UpdateUiState.Checking -> StatusRow(loading = true, text = "Kontrol ediliyor…")
            is UpdateUiState.Downloading ->
                StatusRow(loading = true, text = "İndiriliyor, birazdan kurulum açılacak…")
            is UpdateUiState.UpToDate ->
                Text(
                    "✓ En güncel sürümü kullanıyorsun",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            is UpdateUiState.Available -> {
                Text(
                    "Yeni sürüm mevcut · #${s.sha}",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = { updateVm.downloadAndInstall() }) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Text("  İndir ve kur")
                }
            }
            is UpdateUiState.Error ->
                Text(
                    "⚠ ${s.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            UpdateUiState.Idle -> Unit
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { updateVm.check() },
            enabled = updateVm.state !is UpdateUiState.Checking &&
                updateVm.state !is UpdateUiState.Downloading
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Text("  Güncellemeleri kontrol et")
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        // ---- Hakkında ----
        SectionTitle("Hakkında")
        Text(
            "Odak — sakin görev & odaklanma uygulaması",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "github.com/NmnRn/to-do-mobile-AI",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeekBars(report: WeeklyReport) {
    val max = report.maxDone.coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        report.days.forEach { day ->
            val frac = day.done.toFloat() / max
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day.done.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((6 + frac * 64).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (day.done > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    DateUtils.shortWeekday(day.dayStart),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun hourLabel(hour: Int): String = "%02d:00".format(hour)

private fun showHourPicker(context: Context, current: Int, onPick: (Int) -> Unit) {
    TimePickerDialog(
        context,
        { _, hourOfDay, _ -> onPick(hourOfDay) },
        current, 0, true
    ).show()
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun StatusRow(loading: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.size(10.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
