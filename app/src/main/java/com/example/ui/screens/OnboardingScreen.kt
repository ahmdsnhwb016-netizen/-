package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: StudentViewModel) {
    var step by remember { mutableStateOf(0) }

    // Profile input states
    var name by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("المرحلة الثانوية") }
    var selectedGoal by remember { mutableStateOf("تنظيم وقتي وتحسين مستواي") }
    var dailyTarget by remember { mutableStateOf("60") }

    val stepsData = listOf(
        Triple(
            Icons.Default.Schedule,
            "نظّم يومك الدراسي",
            "خطط لمهامك وواجباتك ومواعيد اختباراتك في مكان واحد منظم يوضح لك دائمًا ما يجب فعله الآن."
        ),
        Triple(
            Icons.Default.Timer,
            "ذاكر بذكاء مع مؤقت التركيز",
            "استخدم تقنية بومودورو أو المؤقت الحر لبناء عادات دراسية متينة وتتبع ساعات إنجازك اليومية."
        ),
        Triple(
            Icons.Default.AutoAwesome,
            "افهم دروسك مع المساعد التعليمي",
            "مساعد دراسي مدعوم بـ Gemini يقدم لك حلولًا خطوة بخطوة، تلميحات تدريجية واختبارات تقيم مستواك."
        )
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مساعد الطالب",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "نظّم دراستك، افهم دروسك، وحقق أهدافك.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                if (step < 3) {
                    val (icon, title, desc) = stepsData[step]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // Step indicators
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(if (step == index) 24.dp else 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (step == index) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }
                } else {
                    // Profile Setup Form
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "إعداد ملفك الدراسي 📝",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "معلومات بسيطة لمساعدتك في تخصيص خطتك الدراسية",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("الاسم أو اللقب الدراسي") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_name_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "المرحلة الدراسية:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("المتوسطة", "الثانوية", "الجامعية").forEach { grade ->
                                    FilterChip(
                                        selected = selectedGrade == grade,
                                        onClick = { selectedGrade = grade },
                                        label = { Text(grade) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "هدفك الدراسي الأساسي:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            val goalsList = listOf(
                                "تنظيم وقتي وتحسين مستواي",
                                "الاستعداد للاختبارات النهائية",
                                "رفع معدلي التراكمي",
                                "فهم المواد الصعبة وحل المسائل"
                            )
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                goalsList.forEach { goalOption ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        RadioButton(
                                            selected = selectedGoal == goalOption,
                                            onClick = { selectedGoal = goalOption }
                                        )
                                        Text(
                                            text = goalOption,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = dailyTarget,
                                onValueChange = { dailyTarget = it.filter { ch -> ch.isDigit() } },
                                label = { Text("الهدف اليومي للمذاكرة (بالدقائق)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (step < 3) {
                    Button(
                        onClick = { step++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_next_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (step == 2) "إعداد ملف الطالب" else "التالي",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (step < 2) {
                        TextButton(
                            onClick = { step = 3 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Text("تخطي المقدمة")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val targetMins = dailyTarget.toIntOrNull() ?: 60
                            viewModel.saveProfile(
                                name = name.ifBlank { "طالب مجتهد" },
                                gradeLevel = selectedGrade,
                                schoolOrTrack = "عام",
                                studyGoal = selectedGoal,
                                dailyTargetMinutes = targetMins
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_finish_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "لنبدأ رحلة التفوق 🚀",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
