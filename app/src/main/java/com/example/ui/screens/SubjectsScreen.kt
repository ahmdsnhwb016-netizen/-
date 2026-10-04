package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.AppScreen
import com.example.ui.StudentViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionTitle
import com.example.ui.components.TaskItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(viewModel: StudentViewModel) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val weakPoints by viewModel.weakPoints.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val activeSubject by viewModel.activeSubjectDetail.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }

    // If active subject detail is selected, show detail view
    if (activeSubject != null) {
        val currentSubject = activeSubject!!
        val subjectTasks = remember(tasks, currentSubject.id) {
            tasks.filter { it.subjectId == currentSubject.id }
        }
        val subjectWeakPoints = remember(weakPoints, currentSubject.id) {
            weakPoints.filter { it.subjectId == currentSubject.id }
        }
        val subjectNotes = remember(notes, currentSubject.id) {
            notes.filter { it.subjectId == currentSubject.id }
        }

        val completedCount = subjectTasks.count { it.status == "COMPLETED" }
        val progress = if (subjectTasks.isNotEmpty()) {
            (completedCount.toFloat() / subjectTasks.size.toFloat() * 100).toInt()
        } else 0

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentSubject.name,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.closeSubjectDetail() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = { editingSubject = currentSubject }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "تعديل المادة")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                item {
                    val subColor = try {
                        Color(android.graphics.Color.parseColor(currentSubject.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = subColor.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(subColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = currentSubject.name,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (currentSubject.description.isNotBlank()) {
                                            Text(
                                                text = currentSubject.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = subColor
                                ) {
                                    Text(
                                        text = "$progress%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = subColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3 Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openStudyTimer(currentSubject) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مذاكرة", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(AppScreen.ASSISTANT) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("المساعد", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(AppScreen.TASKS) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مهمة", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Tasks in this subject
                item {
                    SectionTitle(title = "المهام والواجبات", count = subjectTasks.size)
                }

                if (subjectTasks.isEmpty()) {
                    item {
                        EmptyState(
                            title = "لا توجد مهام لهذه المادة",
                            subtitle = "أضف واجباتك ومواعيد مذاكرتها لمتابعة إنجازك.",
                            actionText = "+ أضف مهمة لهذه المادة",
                            onActionClick = { viewModel.navigateTo(AppScreen.TASKS) }
                        )
                    }
                } else {
                    items(subjectTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            subject = currentSubject,
                            onToggle = { viewModel.toggleTask(task) },
                            onReschedule = { viewModel.rescheduleTask(task, 1) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }

                // Weak points in this subject
                if (subjectWeakPoints.isNotEmpty()) {
                    item {
                        SectionTitle(title = "نقاط تحتاج مراجعة", count = subjectWeakPoints.size)
                    }
                    items(subjectWeakPoints, key = { it.id }) { wp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = wp.topic,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wp.mistakeDescription,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Main Subjects List
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_subject_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة مادة")
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
                                text = "المواد الدراسية 📚",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "إدارة المواد وتتبع مستوى تقدمك في كل مقرر",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_subject_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة مادة")
                        }
                    }
                }

                if (subjects.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.MenuBook,
                            title = "قائمتك خالية من المواد",
                            subtitle = "أضف المواد التي تدرسها في هذا الفصل لبدء تنظيم واجباتك ومراجعاتك.",
                            actionText = "+ أضف أول مادة دراسية",
                            onActionClick = { showAddDialog = true }
                        )
                    }
                } else {
                    items(subjects, key = { it.id }) { subject ->
                        val subTasks = tasks.filter { it.subjectId == subject.id }
                        val completed = subTasks.count { it.status == "COMPLETED" }
                        val prog = if (subTasks.isNotEmpty()) (completed * 100 / subTasks.size) else 0

                        val subColor = try {
                            Color(android.graphics.Color.parseColor(subject.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("subject_card_${subject.id}"),
                            shape = RoundedCornerShape(18.dp),
                            onClick = { viewModel.openSubjectDetail(subject) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(subColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = subject.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${subTasks.size} مهام (${subTasks.size - completed} متبقية)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "عرض التفاصيل",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { prog / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = subColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Subject Dialog
    if (showAddDialog || editingSubject != null) {
        val isEdit = editingSubject != null
        var nameInput by remember { mutableStateOf(editingSubject?.name ?: "") }
        var descInput by remember { mutableStateOf(editingSubject?.description ?: "") }
        var selectedColor by remember { mutableStateOf(editingSubject?.colorHex ?: "#2563EB") }

        val palette = listOf("#2563EB", "#7C3AED", "#059669", "#D97706", "#DC2626", "#0D9488", "#4F46E5")

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingSubject = null
            },
            title = {
                Text(if (isEdit) "تعديل المادة" else "إضافة مادة جديدة")
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("اسم المادة (مثل: رياضيات، فيزياء)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("وصف أو موضوعات المادة (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("اختر لون المادة المميز:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        palette.forEach { colorHex ->
                            val c = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Blue }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColor = colorHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == colorHex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            if (isEdit) {
                                viewModel.updateSubject(
                                    editingSubject!!.copy(
                                        name = nameInput.trim(),
                                        description = descInput.trim(),
                                        colorHex = selectedColor
                                    )
                                )
                            } else {
                                viewModel.addSubject(
                                    name = nameInput.trim(),
                                    icon = "book",
                                    colorHex = selectedColor,
                                    description = descInput.trim()
                                )
                            }
                            showAddDialog = false
                            editingSubject = null
                        }
                    },
                    modifier = Modifier.testTag("save_subject_button")
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        editingSubject = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}
