package com.example.projectinventory.widget

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.*
import com.example.projectinventory.data.InventoryDatabase
import com.example.projectinventory.data.JobEntity
import java.text.SimpleDateFormat
import java.util.*

class InventoryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = InventoryDatabase.getDatabase(context)
        val dao = database.inventoryDao()
        
        provideContent {
            val jobs by dao.getAllJobs().collectAsState(initial = emptyList())
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Filter for today's jobs or upcoming jobs
            val todayJobs = jobs.filter { it.date == today }.sortedBy { it.teamTime }
            val displayJobs = if (todayJobs.isNotEmpty()) {
                todayJobs
            } else {
                jobs.filter { it.date > today }.sortedBy { it.date }.take(5)
            }

            val title = if (todayJobs.isNotEmpty()) "Today's Schedule" else "Upcoming Schedule"

            GlanceTheme {
                WidgetContent(displayJobs, title)
            }
        }
    }

    @Composable
    private fun WidgetContent(jobs: List<JobEntity>, title: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.primary
                )
            )
            
            Spacer(GlanceModifier.height(4.dp))
            
            if (jobs.isNotEmpty()) {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(jobs) { job ->
                        JobItem(job)
                    }
                }
            } else {
                Text(
                    text = "No upcoming jobs",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }

    @Composable
    private fun JobItem(job: JobEntity) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = job.teamTime,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.secondary
                    ),
                    modifier = GlanceModifier.width(45.dp)
                )
                Text(
                    text = job.name,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = "${job.date} | ${job.location}",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1,
                modifier = GlanceModifier.padding(start = 45.dp)
            )
            Spacer(GlanceModifier.height(2.dp))
            Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(GlanceTheme.colors.onSurfaceVariant)) {}
        }
    }
}

class InventoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = InventoryWidget()
}
