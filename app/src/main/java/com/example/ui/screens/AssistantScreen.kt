package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(viewModel: StudentViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAssistantLoading.collectAsStateWithLifecycle()
    val hintLevel by viewModel.currentHintLevel.collectAsStateWithLifecycle()
    val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    var inputPrompt by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQuizDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Android Photo Picker (zero broad storage permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                selectedImageBitmap = bitmap
            } catch (e: Exception) {
                viewModel.showFeedback("تعذر تحميل الصورة المحددة")
            }
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // If an active quiz is running, render the Interactive Quiz Screen!
    if (activeQuiz != null) {
        QuizRunnerView(viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المساعد الدراسي الذكي 🎓",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "اسأل، افهم بالخطوات، وتدرّب",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showQuizDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("open_quiz_dialog_button")
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أنشئ اختبارًا", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat conversation messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(chatMessages, key = { it.id }) { msg ->
                    ChatBubble(msg = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "المساعد يفكر ويعد لك شرحًا تربويًا...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick suggestion chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.askGemini(
                                prompt = inputPrompt,
                                bitmap = selectedImageBitmap,
                                isHintRequest = true
                            )
                            inputPrompt = ""
                            selectedImageBitmap = null
                        },
                        label = { Text("💡 أعطني تلميحًا") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
                item {
                    SuggestionChip(
                        onClick = { inputPrompt = "اشرح لي بالتفصيل مفهوم: " },
                        label = { Text("اشرح لي هذا الدرس") }
                    )
                }
                item {
                    SuggestionChip(
                        onClick = { inputPrompt = "حل هذه المسألة خطوة بخطوة مع توضيح القانون: " },
                        label = { Text("حل مسألة خطوة بخطوة") }
                    )
                }
                item {
                    SuggestionChip(
                        onClick = { showQuizDialog = true },
                        label = { Text("اختبرني في موضوع") }
                    )
                }
            }

            // Selected image preview if attached
            if (selectedImageBitmap != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            bitmap = selectedImageBitmap!!.asImageBitmap(),
                            contentDescription = "صورة المسألة",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "صورة المسألة جاهزة للإرسال",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedImageBitmap = null }) {
                        Icon(Icons.Default.Close, contentDescription = "حذف الصورة")
                    }
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "إرفاق صورة مسألة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text("اكتب سؤالك أو اطلب شرحًا...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_input_field"),
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputPrompt.isNotBlank() || selectedImageBitmap != null) {
                                viewModel.askGemini(
                                    prompt = inputPrompt.trim(),
                                    bitmap = selectedImageBitmap,
                                    isHintRequest = false
                                )
                                inputPrompt = ""
                                selectedImageBitmap = null
                            }
                        },
                        enabled = !isLoading && (inputPrompt.isNotBlank() || selectedImageBitmap != null),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (!isLoading && (inputPrompt.isNotBlank() || selectedImageBitmap != null))
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("assistant_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "إرسال",
                            tint = if (!isLoading && (inputPrompt.isNotBlank() || selectedImageBitmap != null))
                                Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Quiz Creation Dialog
    if (showQuizDialog) {
        var quizSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "الرياضيات") }
        var quizTopic by remember { mutableStateOf("") }
        var quizDifficulty by remember { mutableStateOf("متوسط") }
        var quizCount by remember { mutableStateOf(4) }

        AlertDialog(
            onDismissRequest = { showQuizDialog = false },
            title = { Text("إنشاء اختبار تدريبي 📝", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("المادة الدراسية:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = quizSubject,
                        onValueChange = { quizSubject = it },
                        label = { Text("اسم المادة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = quizTopic,
                        onValueChange = { quizTopic = it },
                        label = { Text("الموضوع أو الدرس (مثال: قوانين الحركة، النهايات)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_topic_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("مستوى الصعوبة:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("سهل", "متوسط", "صعب").forEach { diff ->
                            FilterChip(
                                selected = quizDifficulty == diff,
                                onClick = { quizDifficulty = diff },
                                label = { Text(diff) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("عدد الأسئلة: $quizCount أسئلة", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = quizCount.toFloat(),
                        onValueChange = { quizCount = it.toInt() },
                        valueRange = 2f..6f,
                        steps = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quizSubject.isNotBlank() && quizTopic.isNotBlank()) {
                            showQuizDialog = false
                            viewModel.generateQuiz(
                                subject = quizSubject.trim(),
                                topic = quizTopic.trim(),
                                count = quizCount,
                                difficulty = quizDifficulty
                            )
                        }
                    },
                    modifier = Modifier.testTag("generate_quiz_confirm_button")
                ) {
                    Text("ابدأ الاختبار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuizDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(msg: com.example.ui.ChatMessage) {
    val isUser = msg.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser && msg.hintLevel > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "💡 تلميح تدريجي (${msg.hintLevel} من 3)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (msg.imageBitmap != null) {
                    Image(
                        bitmap = msg.imageBitmap.asImageBitmap(),
                        contentDescription = "صورة مرفقة",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizRunnerView(viewModel: StudentViewModel) {
    val quiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
    val qIndex by viewModel.quizQuestionIndex.collectAsStateWithLifecycle()
    val selectedAns by viewModel.quizSelectedAnswer.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val isCompleted by viewModel.quizCompleted.collectAsStateWithLifecycle()

    val currentQuiz = quiz ?: return
    val questions = currentQuiz.questions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentQuiz.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeQuiz() }) {
                        Icon(Icons.Default.Close, contentDescription = "إنهاء الاختبار")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isCompleted && qIndex < questions.size) {
                val q = questions[qIndex]
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "السؤال ${qIndex + 1} من ${questions.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الدرجة: $score",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (qIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(18.dp),
                            lineHeight = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        q.options.forEachIndexed { idx, optionText ->
                            val isSelected = selectedAns == idx
                            val hasAnswered = selectedAns != null
                            val isCorrect = idx == q.correctIndex

                            val bgColor = when {
                                !hasAnswered -> MaterialTheme.colorScheme.surface
                                isCorrect -> Color(0xFFDCFCE7)
                                isSelected -> Color(0xFFFEE2E2)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !hasAnswered) {
                                        viewModel.answerQuizQuestion(idx)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${('أ'.code + idx).toChar()})",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedAns != null && q.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "💡 الشرح: ${q.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (selectedAns != null) {
                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (qIndex + 1 == questions.size) "عرض النتيجة" else "السؤال التالي",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Completed Screen
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "انتهى الاختبار التدريبي!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "نتيجتك: $score من ${questions.size}",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "تم تسجيل الأخطاء تلقائيًا في قسم «نقاط تحتاج مراجعة» لتتمكن من التدرب عليها لاحقًا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.closeQuiz() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("العودة إلى المساعد")
                }
            }
        }
    }
}
