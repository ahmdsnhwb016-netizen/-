package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.StudentViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.TaskItemCard
import java.util.Calendar

enum class TaskFilterTab(val label: String) {
    ALL("الكل"),
    TODAY("اليوم"),
    OVERDUE("متأخرة ⚠️"),
    UPCOMING("القادمة"),
    COMPLETED("المكتملة ✅")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: StudentViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }

    var selectedTab by remember { mutableStateOf(TaskFilterTab.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    val nowMillis = System.currentTimeMillis()
    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }
    val endOfTodayMillis = todayCal.timeInMillis

    val filteredTasks = remember(tasks, selectedTab) {
        when (selectedTab) {
            TaskFilterTab.ALL -> tasks
            TaskFilterTab.TODAY -> tasks.filter { it.status != "COMPLETED" && it.dueDateMillis <= endOfTodayMillis && it.dueDateMillis >= (endOfTodayMillis - 24 * 60 * 60 * 1000L) }
            TaskFilterTab.OVERDUE -> tasks.filter { it.status != "COMPLETED" && it.dueDateMillis < nowMillis }
            TaskFilterTab.UPCOMING -> tasks.filter { it.status != "COMPLETED" && it.dueDateMillis > endOfTodayMillis }
            TaskFilterTab.COMPLETED -> tasks.filter { it.status == "COMPLETED" }
        }
    }

    val overdueCount = remember(tasks, nowMillis) {
        tasks.count { it.status != "COMPLETED" && it.dueDateMillis < nowMillis }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة جديدة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "المهام والواجبات 📋",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "نظّم ما يجب إنجازه اليوم وبقية الأسبوع",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_task_header_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مهمة جديدة")
                    }
                }
            }

            // Filter Tabs Bar
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TaskFilterTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    text = if (tab == TaskFilterTab.OVERDUE && overdueCount > 0)
                                        "${tab.label} ($overdueCount)"
                                    else tab.label
                                )
                            },
                            colors = if (tab == TaskFilterTab.OVERDUE && overdueCount > 0) {
                                FilterChipDefaults.filterChipColors(
                                    labelColor = Color(0xFFDC2626),
                                    selectedContainerColor = Color(0xFFFEE2E2),
                                    selectedLabelColor = Color(0xFF991B1B)
                                )
                            } else FilterChipDefaults.filterChipColors()
                        )
                    }
                }
            }

            // Overdue Alert Banner if overdue tasks exist
            if (overdueCount > 0 && selectedTab != TaskFilterTab.OVERDUE) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "لديك $overdueCount مهام متأخرة عن موعدها!",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF991B1B)
                                )
                            }
                            TextButton(
                                onClick = { selectedTab = TaskFilterTab.OVERDUE },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("عرضها", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Task Items
            if (filteredTasks.isEmpty()) {
                item {
                    val emptyTitle = when (selectedTab) {
                        TaskFilterTab.OVERDUE -> "رائع! لا توجد مهام متأخرة 🎉"
                        TaskFilterTab.COMPLETED -> "لم تكتمل أي مهام بعد في هذا التصنيف"
                        TaskFilterTab.TODAY -> "يومك خالٍ من المهام المستحقة 🎉"
                        else -> "لا توجد مهام حاليًا"
                    }
                    EmptyState(
                        icon = Icons.Outlined.Assignment,
                        title = emptyTitle,
                        subtitle = "حافظ على تنظيم وقتك وأضف واجباتك ومشاريعك أولاً بأول.",
                        actionText = "+ أضف مهمة جديدة",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        subject = task.subjectId?.let { subjectsMap[it] },
                        onToggle = { viewModel.toggleTask(task) },
                        onReschedule = { viewModel.rescheduleTask(task, 1) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var selectedSubjectId by remember { mutableStateOf<Long?>(subjects.firstOrNull()?.id) }
        var selectedPriority by remember { mutableStateOf("MEDIUM") }
        var selectedType by remember { mutableStateOf("HOMEWORK") }
        var estimatedMinutes by remember { mutableStateOf("30") }
        var dueDaysOffset by remember { mutableStateOf(0) } // 0 = Today, 1 = Tomorrow, 3 = +3 days

        val priorities = listOf("HIGH" to "عالية", "MEDIUM" to "متوسطة", "LOW" to "منخفضة")
        val types = listOf(
            "HOMEWORK" to "واجب",
            "REVISION" to "مراجعة",
            "STUDY" to "مذاكرة",
            "PROJECT" to "مشروع",
            "EXAM" to "اختبار",
            "PERSONAL" to "شخصي"
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة مهمة دراسية جديدة") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان المهمة (مثال: حل مسائل التفاضل)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject Selector
                    if (subjects.isNotEmpty()) {
                        Text("المادة الدراسية:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(subjects) { sub ->
                                FilterChip(
                                    selected = selectedSubjectId == sub.id,
                                    onClick = { selectedSubjectId = sub.id },
                                    label = { Text(sub.name) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Due date offset chips
                    Text("موعد الاستحقاق:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "اليوم", 1 to "غدًا", 3 to "بعد 3 أيام", 7 to "الأسبوع القادم").forEach { (offset, label) ->
                            FilterChip(
                                selected = dueDaysOffset == offset,
                                onClick = { dueDaysOffset = offset },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Priority
                    Text("الأولوية:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        priorities.forEach { (key, label) ->
                            FilterChip(
                                selected = selectedPriority == key,
                                onClick = { selectedPriority = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Task Type
                    Text("نوع المهمة:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(types) { (key, label) ->
                            FilterChip(
                                selected = selectedType == key,
                                onClick = { selectedType = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = estimatedMinutes,
                        onValueChange = { estimatedMinutes = it.filter { ch -> ch.isDigit() } },
                        label = { Text("الوقت المتوقع (بالدقائق)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val cal = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, dueDaysOffset)
                                set(Calendar.HOUR_OF_DAY, 20)
                                set(Calendar.MINUTE, 0)
                            }
                            viewModel.addTask(
                                subjectId = selectedSubjectId,
                                title = title.trim(),
                                description = desc.trim(),
                                dueDateMillis = cal.timeInMillis,
                                estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 30,
                                priority = selectedPriority,
                                taskType = selectedType
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_task_button")
                ) {
                    Text("حفظ المهمة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
