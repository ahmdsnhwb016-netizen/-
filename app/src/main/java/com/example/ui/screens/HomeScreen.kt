package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.ui.AppScreen
import com.example.ui.StudentViewModel
import com.example.ui.components.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: StudentViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val sessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val weakPoints by viewModel.weakPoints.collectAsStateWithLifecycle()
    val reviewItems by viewModel.reviewItems.collectAsStateWithLifecycle()

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }

    // Today calculations
    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStartMillis = todayCal.timeInMillis
    val todayEndMillis = todayStartMillis + 24 * 60 * 60 * 1000L

    // Sessions today
    val todaySessions = remember(sessions, todayStartMillis) {
        sessions.filter { it.timestampMillis >= todayStartMillis }
    }
    val todayFocusMinutes = remember(todaySessions) {
        todaySessions.sumOf { it.actualSeconds } / 60
    }

    // Tasks due today or pending
    val todayTasks = remember(tasks, todayEndMillis) {
        tasks.filter { it.dueDateMillis <= todayEndMillis && it.status != "COMPLETED" }
            .sortedBy { if (it.priority == "HIGH") 0 else if (it.priority == "MEDIUM") 1 else 2 }
    }
    val completedTasksToday = remember(tasks, todayStartMillis) {
        tasks.count { it.status == "COMPLETED" && (it.completedAtMillis ?: 0L) >= todayStartMillis }
    }
    val totalActiveTasksCount = tasks.count { it.status != "COMPLETED" }

    // Due reviews today
    val dueReviews = remember(reviewItems, todayEndMillis) {
        reviewItems.filter { !it.isCompleted && it.scheduledDateMillis <= todayEndMillis }
    }

    val dailyTarget = profile?.dailyTargetMinutes ?: 60
    val progressPercent = if (dailyTarget > 0) {
        ((todayFocusMinutes.toFloat() / dailyTarget.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Smart recommendation logic based on actual data
    val smartRecommendation = remember(tasks, weakPoints, todayTasks, dueReviews) {
        val overdue = tasks.firstOrNull { it.status != "COMPLETED" && it.dueDateMillis < System.currentTimeMillis() }
        val topWeak = weakPoints.firstOrNull { it.status == "NEEDS_REVIEW" }
        when {
            overdue != null -> {
                "لديك مهمة متأخرة: «${overdue.title}». ننصحك بإنجازها أولاً أو إعادة جدولتها لتخفيف الضغط."
            }
            dueReviews.isNotEmpty() -> {
                "لديك ${dueReviews.size} موضوع مستحق للمراجعة المتباعدة اليوم. مراجعتها لـ 15 دقيقة تعزز حفظك!"
            }
            topWeak != null -> {
                "لاحظنا نقطة تحتاج مراجعة في «${topWeak.subjectName} - ${topWeak.topic}». هل ترغب بجلسة تدريب قصيرة؟"
            }
            todayTasks.isNotEmpty() -> {
                "خطتك لليوم جاهزة مع ${todayTasks.size} مهمة. ابدأ بالمهمة ذات الأولوية العالية لتحقيق أكبر إنجاز."
            }
            else -> {
                "أمورك منظمة وممتازة! خصص 25 دقيقة الآن لقراءة موضوع متقدم أو إنشاء اختبار مع المساعد."
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Header
        item {
            AppHeader(
                studentName = profile?.name ?: "طالب مجتهد",
                streakDays = profile?.studyStreakDays ?: 1,
                onTimerClick = { viewModel.openStudyTimer() }
            )
        }

        // Daily Summary Pill & Progress
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_progress_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Column {
                            Text(
                                text = "إنجاز اليوم 🎯",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (todayTasks.isNotEmpty() || dueReviews.isNotEmpty())
                                    "لديك اليوم ${todayTasks.size} مهام و ${dueReviews.size} مراجعات."
                                else "لا توجد التزامات متأخرة اليوم!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$todayFocusMinutes / $dailyTarget دقيقة",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "وقت التركيز",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$completedTasksToday / ${todayTasks.size + completedTasksToday}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "المهام المنجزة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Action: "ابدأ الدراسة"
        item {
            Button(
                onClick = { viewModel.openStudyTimer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_study_session_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ابدأ جلسة دراسة الآن",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Smart Recommendation Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_recommendation_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "توصية ذكية لمستواك",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = smartRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Due Spaced Reviews Section
        if (dueReviews.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "مراجعات مستحقة اليوم",
                    count = dueReviews.size
                )
            }
            items(dueReviews, key = { "rev_${it.id}" }) { rev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rev.topic,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = rev.subjectName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { viewModel.completeReview(rev) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.heightIn(min = 38.dp)
                        ) {
                            Text("أتممت المراجعة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Today's Plan Tasks Section
        item {
            SectionTitle(
                title = "خطة مهام اليوم",
                count = todayTasks.size,
                actionLabel = "جميع المهام",
                onActionClick = { viewModel.navigateTo(AppScreen.TASKS) }
            )
        }

        if (todayTasks.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.CheckCircle,
                    title = "لا توجد مهام مستحقة اليوم 🎉",
                    subtitle = "يمكنك إضافة واجب أو مراجعة أو البدء بمذاكرة حرة.",
                    actionText = "+ أضف مهمة جديدة",
                    onActionClick = { viewModel.navigateTo(AppScreen.TASKS) }
                )
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    subject = task.subjectId?.let { subjectsMap[it] },
                    onToggle = { viewModel.toggleTask(task) },
                    onReschedule = { viewModel.rescheduleTask(task, 1) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        // Subjects Snapshot / Quick access
        item {
            SectionTitle(
                title = "المواد الدراسية",
                count = subjects.size,
                actionLabel = "إدارة المواد",
                onActionClick = { viewModel.navigateTo(AppScreen.SUBJECTS) }
            )
        }

        if (subjects.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.MenuBook,
                    title = "لم تقم بإضافة مواد بعد",
                    subtitle = "أضف موادك (رياضيات، فيزياء، لغة عربية، إلخ) لمتابعة تقدمك في كل مادة.",
                    actionText = "+ أضف أول مادة",
                    onActionClick = { viewModel.navigateTo(AppScreen.SUBJECTS) }
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    subjects.take(3).forEach { sub ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(16.dp),
                            onClick = { viewModel.openSubjectDetail(sub) },
                            colors = CardDefaults.cardColors(
                                containerColor = try {
                                    Color(android.graphics.Color.parseColor(sub.colorHex)).copy(alpha = 0.12f)
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try { Color(android.graphics.Color.parseColor(sub.colorHex)) }
                                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
