package com.odak.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.odak.app.data.Priority
import com.odak.app.data.RepeatRule
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onToggleStatus: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val done = task.status == TaskStatus.DONE
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriorityBar(task.priority)
            Spacer(Modifier.width(10.dp))

            StatusButton(status = task.status, onClick = onToggleStatus)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.note.isNotBlank()) {
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.size(4.dp))
                MetaRow(task)
            }

            if (task.photoPath != null) {
                Spacer(Modifier.width(10.dp))
                AsyncImage(
                    model = File(task.photoPath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
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
private fun StatusButton(status: TaskStatus, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .then(
                when (status) {
                    TaskStatus.DONE -> Modifier.background(primary)
                    TaskStatus.IN_PROGRESS -> Modifier.background(secondary)
                    TaskStatus.WAITING -> Modifier.border(2.dp, outline, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            TaskStatus.DONE -> Icon(
                Icons.Filled.Check,
                contentDescription = "Yapıldı",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            TaskStatus.IN_PROGRESS -> Icon(
                Icons.Outlined.MoreHoriz,
                contentDescription = "Devam ediyor",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            TaskStatus.WAITING -> {}
        }
    }
}

@Composable
private fun PriorityBar(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
        Priority.LOW -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
private fun MetaRow(task: Task) {
    val statusText = when (task.status) {
        TaskStatus.WAITING -> "Bekliyor"
        TaskStatus.IN_PROGRESS -> "Devam ediyor"
        TaskStatus.DONE -> "Yapıldı"
    }
    val statusColor = when (task.status) {
        TaskStatus.WAITING -> MaterialTheme.colorScheme.onSurfaceVariant
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        TaskStatus.DONE -> MaterialTheme.colorScheme.primary
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(statusText, style = MaterialTheme.typography.labelLarge, color = statusColor)
        if (task.dueMinute >= 0) {
            MetaText("🕘 %02d:%02d".format(task.dueMinute / 60, task.dueMinute % 60))
        }
        if (task.repeat != RepeatRule.NONE) {
            MetaText("🔁")
        }
        if (task.subtasks.isNotEmpty()) {
            val done = task.subtasks.count { it.done }
            MetaText("☑ $done/${task.subtasks.size}")
        }
        if (task.category.isNotBlank()) {
            MetaText("🏷 ${task.category}")
        }
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
