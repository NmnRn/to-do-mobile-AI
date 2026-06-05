package com.odak.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.odak.app.MainActivity
import com.odak.app.R
import com.odak.app.data.AppDatabase
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import com.odak.app.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Home-screen widget listing today's tasks and completion progress. */
class TodayWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                renderAll(context, manager, appWidgetIds)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val ROW_IDS = intArrayOf(
            R.id.widget_row_1, R.id.widget_row_2,
            R.id.widget_row_3, R.id.widget_row_4
        )

        /** Rebuilds every placed widget; safe to call from any thread. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, TodayWidget::class.java)
            )
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                renderAll(context, manager, ids)
            }
        }

        private suspend fun renderAll(
            context: Context,
            manager: AppWidgetManager,
            ids: IntArray
        ) {
            val dao = AppDatabase.get(context).taskDao()
            val today = DateUtils.today()
            val tasks = dao.between(today, today)
            val views = buildViews(context, tasks)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun buildViews(context: Context, tasks: List<Task>): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_today)

            val done = tasks.count { it.status == TaskStatus.DONE }
            views.setTextViewText(R.id.widget_count, "$done / ${tasks.size}")

            val pending = tasks
                .filter { it.status != TaskStatus.DONE }
                .sortedWith(compareBy({ it.dueMinute < 0 }, { it.dueMinute }))

            ROW_IDS.forEachIndexed { index, rowId ->
                val task = pending.getOrNull(index)
                if (task == null) {
                    views.setViewVisibility(rowId, View.GONE)
                } else {
                    views.setViewVisibility(rowId, View.VISIBLE)
                    views.setTextViewText(rowId, rowLabel(task))
                }
            }

            val emptyVisible = pending.isEmpty()
            views.setViewVisibility(
                R.id.widget_empty,
                if (emptyVisible) View.VISIBLE else View.GONE
            )

            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp)
            return views
        }

        private fun rowLabel(task: Task): String {
            val time = if (task.dueMinute >= 0) {
                "%02d:%02d  ".format(task.dueMinute / 60, task.dueMinute % 60)
            } else ""
            return "• $time${task.title}"
        }
    }
}
