package com.odak.app.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.odak.app.data.PlanBlock
import com.odak.app.util.DateUtils
import java.util.Calendar

@Composable
fun PlanScreen(vm: PlanViewModel) {
    val blocks by vm.blocks.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PlanBlock?>(null) }

    val isToday = vm.selectedDay == DateUtils.today()
    val nowMinute = if (isToday) currentMinuteOfDay() else -1
    val plannedMinutes = blocks.sumOf { it.durationMinutes }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlanHeader(
                dayLabel = DateUtils.label(vm.selectedDay),
                fullDate = DateUtils.fullDate(vm.selectedDay),
                count = blocks.size,
                plannedMinutes = plannedMinutes,
                onPrev = { vm.changeDay(-1) },
                onNext = { vm.changeDay(1) },
                onToday = { vm.goToday() }
            )

            if (blocks.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(blocks, key = { it.id }) { block ->
                        val ongoing = nowMinute in block.startMinute until block.endMinute
                        PlanCard(
                            block = block,
                            ongoing = ongoing,
                            onToggleDone = { vm.toggleDone(block) },
                            onClick = { editing = block; showSheet = true },
                            onDelete = { vm.delete(block) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { editing = null; showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Plan ekle")
        }
    }

    if (showSheet) {
        PlanEditorSheet(
            existing = editing,
            onDismiss = { showSheet = false },
            onSave = { start, end, title, note ->
                vm.save(editing, start, end, title, note)
                showSheet = false
            }
        )
    }
}

@Composable
private fun PlanHeader(
    dayLabel: String,
    fullDate: String,
    count: Int,
    plannedMinutes: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dayLabel, style = MaterialTheme.typography.headlineMedium)
                Text(
                    fullDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPrev) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Önceki gün")
            }
            IconButton(onClick = onToday) {
                Icon(Icons.Filled.Today, contentDescription = "Bugün")
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki gün")
            }
        }
        if (count > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "$count blok • toplam ${hourLabel(plannedMinutes)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlanCard(
    block: PlanBlock,
    ongoing: Boolean,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val border = if (ongoing) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
    } else Modifier
    Card(
        onClick = onClick,
        modifier = border,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                modifier = Modifier.width(54.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "%02d:%02d".format(block.startMinute / 60, block.startMinute % 60),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (ongoing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "%02d:%02d".format(block.endMinute / 60, block.endMinute % 60),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))
            DoneButton(done = block.done, onClick = onToggleDone)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ongoing) {
                        Text(
                            "ŞİMDİ  ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (block.done) TextDecoration.LineThrough else null,
                        color = if (block.done) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (block.note.isNotBlank()) {
                    Text(
                        text = block.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    hourLabel(block.durationMinutes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DoneButton(done: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .then(
                if (done) Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Tamamlandı",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗓️", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Bu gün için plan yok",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Sağ alttaki + ile saat aralığı ekle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun hourLabel(minutes: Int): String {
    if (minutes <= 0) return "0 dk"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "$h sa $m dk"
        h > 0 -> "$h sa"
        else -> "$m dk"
    }
}

private fun currentMinuteOfDay(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
}
