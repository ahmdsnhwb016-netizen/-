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
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.WeakPointEntity
import com.example.ui.StudentViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.util.Calendar

enum class MoreSubTab(val title: String) {
    PROGRESS("تقدمي 📊"),
    WEAK_POINTS("نقاط الضعف 🎯"),
    GOALS("الأهداف 🏁"),
    NOTES("الملاحظات 📝"),
    SETTINGS("الإعدادات ⚙️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(viewModel: StudentViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var selectedSubTab by remember { mutableStateOf(MoreSubTab.PROGRESS) }

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val sessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val weakPoints by viewModel.weakPoints.collectAsStateWithLifecycle()
    val reviewItems by viewModel.reviewItems.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المزيد والمتابعة", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sub-tabs scrollable header
            ScrollableTabRow(
                selectedTabIndex = selectedSubTab.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                MoreSubTab.values().forEach { tab ->
                    Tab(
                        selected = selectedSubTab == tab,
                        onClick = { selectedSubTab = tab },
                        text = { Text(tab.title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedSubTab) {
                    MoreSubTab.PROGRESS -> {
                        val totalMinutes = sessions.sumOf { it.actualSeconds } / 60
                        val completedTasksCount = tasks.count { it.status == "COMPLETED" }

                        item {
                            Text(
                                text = "ملخص تقدمك الدراسي 📈",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "مقارنة تقدمك مع أهدافك السابقة فقط لتحفيز نموك المستمر",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Stats Grid
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "$totalMinutes دقيقة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "إجمالي التركيز", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Icon(Icons.Default.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "$completedTasksCount مهمة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "المهام المنجزة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "${profile?.studyStreakDays ?: 1} أيام", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "سلسلة الالتزام", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "${sessions.size} جلسة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "جلسات الدراسة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Achievements
                        item {
                            SectionTitle(title = "الأوسمة والإنجازات 🏅", count = achievements.size)
                        }

                        items(achievements) { ach ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = ach.title, fontWeight = FontWeight.Bold)
                                        Text(text = ach.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    MoreSubTab.WEAK_POINTS -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "نقاط تحتاج مراجعة 🎯", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "تُسجل تلقائيًا عند الخطأ في الاختبارات لتثبيتها بالمراجعة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (weakPoints.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Outlined.CheckCircle,
                                    title = "لا توجد نقاط ضعف مسجلة",
                                    subtitle = "عندما تخوض اختبارًا مع المساعد الذكي، سيتم تسجيل الموضوعات التي أخطأت بها هنا تلقائيًا لتراجعها."
                                )
                            }
                        } else {
                            items(weakPoints, key = { it.id }) { wp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = wp.subjectName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            IconButton(onClick = { viewModel.deleteWeakPoint(wp) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Text(text = wp.topic, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = wp.mistakeDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.scheduleSpacedReview(null, wp.topic, 1)
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.heightIn(min = 32.dp)
                                            ) {
                                                Text("جدولة مراجعة (+1 يوم)", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MoreSubTab.GOALS -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "أهدافي الدراسية 🏁", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "حدد أهدافًا واقعية وقس إنجازك خطوة بخطوة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { showAddGoalDialog = true }, shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("هدف جديد")
                                }
                            }
                        }

                        if (goals.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Outlined.EmojiEvents,
                                    title = "لا توجد أهداف حالية",
                                    subtitle = "ضع هدفًا صغيرًا وابدأ (مثل: إنهاء 5 ساعات دراسة هذا الأسبوع).",
                                    actionText = "+ أضف أول هدف",
                                    onActionClick = { showAddGoalDialog = true }
                                )
                            }
                        } else {
                            items(goals, key = { it.id }) { goal ->
                                val prog = (goal.currentValue.toFloat() / goal.targetValue.toFloat()).coerceIn(0f, 1f)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = goal.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            IconButton(onClick = { viewModel.deleteGoal(goal) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "${goal.currentValue} / ${goal.targetValue} ${goal.unit}", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.updateGoalProgress(goal, goal.currentValue + 1) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.heightIn(min = 32.dp)
                                            ) {
                                                Text("+1 تقدم", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MoreSubTab.NOTES -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "ملاحظاتي 📝", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "ملخصات سريعة وقوانين وملاحظات مهمة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { showAddNoteDialog = true }, shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ملاحظة جديدة")
                                }
                            }
                        }

                        if (notes.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Outlined.StickyNote2,
                                    title = "لا توجد ملاحظات مسجلة",
                                    subtitle = "احتفظ بأفكارك وقوانينك وملاحظاتك الدراسية هنا.",
                                    actionText = "+ أضف ملاحظة",
                                    onActionClick = { showAddNoteDialog = true }
                                )
                            }
                        } else {
                            items(notes, key = { it.id }) { note ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = note.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            IconButton(onClick = { viewModel.deleteNote(note) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        if (note.subjectName.isNotBlank()) {
                                            Text(text = note.subjectName, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(text = note.content, style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp)
                                    }
                                }
                            }
                        }
                    }

                    MoreSubTab.SETTINGS -> {
                        item {
                            Text(text = "إعدادات التطبيق والبيانات ⚙️", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "الملف الشخصي", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "الاسم: ${profile?.name ?: "طالب مجتهد"}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "المرحلة: ${profile?.gradeLevel ?: "الثانوية"}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "الهدف: ${profile?.studyGoal ?: "التفوق والالتزام"}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Data Management actions
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "النسخ الاحتياطي والبيانات", fontWeight = FontWeight.Bold)

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                exportedJsonText = viewModel.exportJson()
                                                showExportDialog = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تصدير بياناتي (JSON)")
                                    }

                                    OutlinedButton(
                                        onClick = { showImportDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("استيراد بيانات من JSON")
                                    }

                                    Button(
                                        onClick = { viewModel.seedSampleData() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("إضافة بيانات تجريبية دراسية")
                                    }
                                }
                            }
                        }

                        // Danger zone
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "منطقة الحذف", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "حذف جميع المواد والمهام وسجلات المذاكرة محليًا.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF991B1B))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showClearConfirmDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        modifier = Modifier.fillMaxWidth().testTag("delete_all_data_button")
                                    ) {
                                        Text("حذف جميع البيانات")
                                    }
                                }
                            }
                        }

                        // About App
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "عن تطبيق مساعد الطالب", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "تطبيق دراسي متكامل يساعدك على تنظيم يومك الدراسي، التركيز بجلسات بومودورو، وفهم الدروس والمسائل بالذكاء الاصطناعي مع Gemini.\nالإصدار 1.0",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        var gTitle by remember { mutableStateOf("") }
        var gTarget by remember { mutableStateOf("10") }
        var gUnit by remember { mutableStateOf("ساعات") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("إضافة هدف دراسي جديد") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gTitle,
                        onValueChange = { gTitle = it },
                        label = { Text("الهدف (مثال: دراسة 10 ساعات هذا الأسبوع)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = gTarget,
                        onValueChange = { gTarget = it.filter { ch -> ch.isDigit() } },
                        label = { Text("القيمة المستهدفة (مثال: 10)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = gUnit,
                        onValueChange = { gUnit = it },
                        label = { Text("الوحدة (ساعة، واجب، درس)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (gTitle.isNotBlank()) {
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
                        viewModel.addGoal(gTitle.trim(), gTarget.toIntOrNull() ?: 10, gUnit.trim(), cal.timeInMillis)
                        showAddGoalDialog = false
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var nTitle by remember { mutableStateOf("") }
        var nContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("إضافة ملاحظة جديدة") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nTitle,
                        onValueChange = { nTitle = it },
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nContent,
                        onValueChange = { nContent = it },
                        label = { Text("المحتوى") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nTitle.isNotBlank()) {
                        viewModel.addNote(null, nTitle.trim(), nContent.trim())
                        showAddNoteDialog = false
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("بياناتك المصدرة (JSON)") },
            text = {
                OutlinedTextField(
                    value = exportedJsonText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            },
            confirmButton = {
                Button(onClick = { showExportDialog = false }) {
                    Text("تم النسخ")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("استيراد البيانات") },
            text = {
                OutlinedTextField(
                    value = importInputText,
                    onValueChange = { importInputText = it },
                    placeholder = { Text("ألصق نص JSON هنا...") },
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val ok = viewModel.importJson(importInputText)
                        if (ok) {
                            viewModel.showFeedback("تم استيراد البيانات بنجاح")
                        } else {
                            viewModel.showFeedback("صيغة البيانات غير صحيحة")
                        }
                        showImportDialog = false
                    }
                }) {
                    Text("استيراد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Clear Confirm Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("تأكيد حذف جميع البيانات", color = MaterialTheme.colorScheme.error) },
            text = {
                Text("سيؤدي ذلك إلى حذف جميع المواد والمهام وسجلات المذاكرة محليًا ولا يمكن التراجع عن هذه العملية.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد الحذف النهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
