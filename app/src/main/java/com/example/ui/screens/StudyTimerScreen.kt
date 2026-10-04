package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudentViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTimerScreen(viewModel: StudentViewModel) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.timerSelectedSubject.collectAsStateWithLifecycle()
    val taskName by viewModel.timerTaskName.collectAsStateWithLifecycle()
    val durationMinutes by viewModel.timerDurationMinutes.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val sessionType by viewModel.timerSessionType.collectAsStateWithLifecycle()
    val showReflection by viewModel.showReflectionDialog.collectAsStateWithLifecycle()

    val totalSeconds = durationMinutes * 60
    val progress = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    var reflectionRating by remember { mutableStateOf("GOOD") }
    var reflectionAchieved by remember { mutableStateOf("YES") }
    var reflectionNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جلسة التركيز ⏱️", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeStudyTimer() }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Configuration when timer is NOT running
            if (!isRunning && remainingSeconds == totalSeconds) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "إعداد الجلسة الدراسية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject Selection
                        Text("المادة:", style = MaterialTheme.typography.labelMedium)
                        if (subjects.isEmpty()) {
                            Text("مذاكرة عامة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(subjects) { sub ->
                                    FilterChip(
                                        selected = selectedSubject?.id == sub.id,
                                        onClick = {
                                            viewModel.setTimerConfig(
                                                subject = sub,
                                                taskName = taskName,
                                                durationMin = durationMinutes,
                                                type = sessionType
                                            )
                                        },
                                        label = { Text(sub.name) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Session Duration options
                        Text("المدة المحددة:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(25 to "25 د (بومودورو)", 45 to "45 د", 60 to "60 د", 90 to "90 د").forEach { (min, label) ->
                                FilterChip(
                                    selected = durationMinutes == min,
                                    onClick = {
                                        viewModel.setTimerConfig(
                                            subject = selectedSubject,
                                            taskName = taskName,
                                            durationMin = min,
                                            type = if (min == 25) "POMODORO" else "FREE"
                                        )
                                    },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Info header when running or paused
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = selectedSubject?.name ?: "مذاكرة عامة",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (sessionType == "POMODORO") "جلسة بومودورو مركزة" else "جلسة حرة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Big Circular Timer Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .testTag("circular_timer_container"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) "في وضع التركيز 🔥" else "جاهز للبدء",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action Buttons (Play / Pause / Reset / Finish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (remainingSeconds < totalSeconds) {
                    OutlinedIconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .widthIn(min = 160.dp)
                        .testTag("timer_toggle_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "إيقاف مؤقت" else "ابدأ الآن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (remainingSeconds < totalSeconds) {
                    Spacer(modifier = Modifier.width(16.dp))
                    FilledTonalButton(
                        onClick = { viewModel.finishSessionEarly() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("إنهاء الجلسة")
                    }
                }
            }
        }
    }

    // Post-session Reflection Dialog
    if (showReflection) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReflectionDialog() },
            title = {
                Text("أحسنت! انتهت جلسة الدراسة 🎉", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "كيف كانت الجلسة؟",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "EXCELLENT" to "ممتازة ⭐",
                            "GOOD" to "جيدة 👍",
                            "MODERATE" to "متوسطة 😐",
                            "HARD" to "صعبة 😣"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = reflectionRating == key,
                                onClick = { reflectionRating = key },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "هل أنجزت الهدف المخطط له؟",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "YES" to "نعم بالكامل ✅",
                            "PARTIAL" to "جزئيًا ⏳",
                            "NO" to "لا ❌"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = reflectionAchieved == key,
                                onClick = { reflectionAchieved = key },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reflectionNotes,
                        onValueChange = { reflectionNotes = it },
                        label = { Text("ملاحظات سريعة عن الجلسة (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCompletedSession(
                            rating = reflectionRating,
                            achievedGoal = reflectionAchieved,
                            notes = reflectionNotes.trim()
                        )
                    },
                    modifier = Modifier.testTag("save_session_reflection_button")
                ) {
                    Text("حفظ الجلسة")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReflectionDialog() }) {
                    Text("تجاهل")
                }
            }
        )
    }
}
