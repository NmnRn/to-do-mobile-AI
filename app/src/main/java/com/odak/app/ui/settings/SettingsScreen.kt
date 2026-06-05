package com.odak.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.odak.app.ui.theme.ThemeMode
import com.odak.app.ui.theme.ThemeViewModel

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
