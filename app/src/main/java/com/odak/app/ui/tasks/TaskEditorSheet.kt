package com.odak.app.ui.tasks

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.odak.app.R
import com.odak.app.data.Priority
import com.odak.app.data.RepeatRule
import com.odak.app.data.SubTask
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import com.odak.app.util.ImageStorage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    existing: Task?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        note: String,
        status: TaskStatus,
        photoPath: String?,
        dueMinute: Int,
        priority: Priority,
        category: String,
        repeat: RepeatRule,
        subtasks: List<SubTask>
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: TaskStatus.WAITING) }
    var photoPath by remember { mutableStateOf(existing?.photoPath) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    var dueMinute by remember { mutableStateOf(existing?.dueMinute ?: -1) }
    var priority by remember { mutableStateOf(existing?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(existing?.category ?: "") }
    var repeat by remember { mutableStateOf(existing?.repeat ?: RepeatRule.NONE) }
    val subtasks = remember { (existing?.subtasks ?: emptyList()).toMutableStateList() }
    var newSubtask by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            ImageStorage.importFromUri(context, uri)?.let { photoPath = it }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoPath = pendingCameraPath
        else ImageStorage.deleteQuietly(pendingCameraPath)
        pendingCameraPath = null
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
                text = stringResource(if (existing == null) R.string.new_task else R.string.edit_task),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.task_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // ---- Saat ----
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.reminder_time), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    val initial = if (dueMinute >= 0) dueMinute else 9 * 60
                    TimePickerDialog(
                        context,
                        { _, h, m -> dueMinute = h * 60 + m },
                        initial / 60, initial % 60, true
                    ).show()
                }) {
                    Icon(Icons.Filled.Schedule, contentDescription = null)
                    Text(
                        if (dueMinute >= 0)
                            "  %02d:%02d".format(dueMinute / 60, dueMinute % 60)
                        else "  " + stringResource(R.string.add_time)
                    )
                }
                if (dueMinute >= 0) {
                    TextButton(onClick = { dueMinute = -1 }) { Text(stringResource(R.string.remove)) }
                }
            }

            // ---- Öncelik ----
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.priority), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(stringResource(R.string.prio_low), priority == Priority.LOW) { priority = Priority.LOW }
                ChoiceChip(stringResource(R.string.prio_medium), priority == Priority.MEDIUM) { priority = Priority.MEDIUM }
                ChoiceChip(stringResource(R.string.prio_high), priority == Priority.HIGH) { priority = Priority.HIGH }
            }

            // ---- Kategori ----
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.category_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ---- Tekrar ----
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.repeat), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(stringResource(R.string.repeat_none), repeat == RepeatRule.NONE) { repeat = RepeatRule.NONE }
                ChoiceChip(stringResource(R.string.repeat_daily), repeat == RepeatRule.DAILY) { repeat = RepeatRule.DAILY }
                ChoiceChip(stringResource(R.string.repeat_weekdays), repeat == RepeatRule.WEEKDAYS) { repeat = RepeatRule.WEEKDAYS }
                ChoiceChip(stringResource(R.string.repeat_weekly), repeat == RepeatRule.WEEKLY) { repeat = RepeatRule.WEEKLY }
            }

            // ---- Alt görevler ----
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.subtasks), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            subtasks.forEachIndexed { index, sub ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sub.done,
                        onCheckedChange = { subtasks[index] = sub.copy(done = it) }
                    )
                    Text(
                        sub.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (sub.done) TextDecoration.LineThrough else null
                    )
                    IconButton(onClick = { subtasks.removeAt(index) }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSubtask,
                    onValueChange = { newSubtask = it },
                    label = { Text(stringResource(R.string.new_subtask)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val t = newSubtask.trim()
                    if (t.isNotEmpty()) {
                        subtasks.add(SubTask(t))
                        newSubtask = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            }

            // ---- Durum ----
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.status), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(stringResource(R.string.status_waiting), status == TaskStatus.WAITING) { status = TaskStatus.WAITING }
                ChoiceChip(stringResource(R.string.status_in_progress), status == TaskStatus.IN_PROGRESS) { status = TaskStatus.IN_PROGRESS }
                ChoiceChip(stringResource(R.string.status_done), status == TaskStatus.DONE) { status = TaskStatus.DONE }
            }

            // ---- Fotoğraf ----
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.photo), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            if (photoPath != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = File(photoPath!!),
                        contentDescription = stringResource(R.string.task_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(onClick = { photoPath = null }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text("  " + stringResource(R.string.remove_photo))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text("  " + stringResource(R.string.gallery))
                }
                OutlinedButton(
                    onClick = {
                        val (file, uri) = ImageStorage.newCameraTarget(context)
                        pendingCameraPath = file.absolutePath
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Text("  " + stringResource(R.string.camera))
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(
                        title, note, status, photoPath,
                        dueMinute, priority, category, repeat, subtasks.toList()
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Text("  " + stringResource(R.string.save), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
