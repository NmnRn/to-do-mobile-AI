package com.odak.app.ui.plan

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odak.app.OdakApp
import com.odak.app.data.PlanBlock
import com.odak.app.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as OdakApp).planRepository

    var selectedDay by mutableStateOf(DateUtils.today())
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val blocks: StateFlow<List<PlanBlock>> = snapshotFlow { selectedDay }
        .flatMapLatest { repo.blocksForDay(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun changeDay(delta: Int) {
        selectedDay = DateUtils.addDays(selectedDay, delta)
    }

    fun goToday() {
        selectedDay = DateUtils.today()
    }

    fun save(existing: PlanBlock?, startMinute: Int, endMinute: Int, title: String, note: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            val block = existing?.copy(
                startMinute = startMinute,
                endMinute = endMinute,
                title = cleanTitle,
                note = note.trim()
            ) ?: PlanBlock(
                dayDate = selectedDay,
                startMinute = startMinute,
                endMinute = endMinute,
                title = cleanTitle,
                note = note.trim()
            )
            repo.upsert(block)
        }
    }

    fun toggleDone(block: PlanBlock) {
        viewModelScope.launch { repo.upsert(block.copy(done = !block.done)) }
    }

    fun delete(block: PlanBlock) {
        viewModelScope.launch { repo.delete(block) }
    }
}
