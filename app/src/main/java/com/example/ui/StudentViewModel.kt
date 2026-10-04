package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiResult
import com.example.data.ai.GeminiService
import com.example.data.ai.GeneratedQuiz
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    HOME,
    SUBJECTS,
    TASKS,
    ASSISTANT,
    MORE
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val imageBitmap: Bitmap? = null,
    val hintLevel: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository
    private val geminiService = GeminiService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudentRepository(db.studentDao())

        viewModelScope.launch {
            repository.initializeDefaultAchievements()
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active sub-screen or modal overlays
    private val _activeSubjectDetail = MutableStateFlow<SubjectEntity?>(null)
    val activeSubjectDetail: StateFlow<SubjectEntity?> = _activeSubjectDetail.asStateFlow()

    private val _showStudyTimer = MutableStateFlow(false)
    val showStudyTimer: StateFlow<Boolean> = _showStudyTimer.asStateFlow()

    // Feedback message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun showFeedback(msg: String) {
        _userMessage.value = msg
    }

    fun clearFeedback() {
        _userMessage.value = null
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _activeSubjectDetail.value = null
    }

    fun openSubjectDetail(subject: SubjectEntity) {
        _activeSubjectDetail.value = subject
    }

    fun closeSubjectDetail() {
        _activeSubjectDetail.value = null
    }

    fun openStudyTimer(preselectedSubject: SubjectEntity? = null) {
        if (preselectedSubject != null) {
            _timerSelectedSubject.value = preselectedSubject
        }
        _showStudyTimer.value = true
    }

    fun closeStudyTimer() {
        _showStudyTimer.value = false
    }

    // Repository Flows
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weakPoints: StateFlow<List<WeakPointEntity>> = repository.allWeakPoints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviewItems: StateFlow<List<ReviewItemEntity>> = repository.allReviewItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile actions
    fun saveProfile(
        name: String,
        gradeLevel: String,
        schoolOrTrack: String,
        studyGoal: String,
        dailyTargetMinutes: Int
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveProfile(
                current.copy(
                    name = name.ifBlank { "طالب مجتهد" },
                    gradeLevel = gradeLevel,
                    schoolOrTrack = schoolOrTrack,
                    studyGoal = studyGoal,
                    dailyTargetMinutes = dailyTargetMinutes,
                    hasCompletedOnboarding = true
                )
            )
            showFeedback("تم حفظ الملف الشخصي بنجاح")
        }
    }

    // Subject actions
    fun addSubject(name: String, icon: String, colorHex: String, description: String) {
        viewModelScope.launch {
            repository.addSubject(name, icon, colorHex, description)
            showFeedback("تمت إضافة مادة $name")
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            showFeedback("تم تحديث المادة")
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            if (_activeSubjectDetail.value?.id == subject.id) {
                _activeSubjectDetail.value = null
            }
            showFeedback("تم حذف المادة")
        }
    }

    // Task actions
    fun addTask(
        subjectId: Long?,
        title: String,
        description: String,
        dueDateMillis: Long,
        estimatedMinutes: Int,
        priority: String,
        taskType: String
    ) {
        viewModelScope.launch {
            repository.addTask(
                subjectId = subjectId,
                title = title,
                description = description,
                dueDateMillis = dueDateMillis,
                estimatedMinutes = estimatedMinutes,
                priority = priority,
                taskType = taskType
            )
            showFeedback("تمت إضافة المهمة: $title")
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            showFeedback("تم حفظ التعديلات")
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
            val msg = if (task.status == "COMPLETED") "تمت إعادة المهمة إلى قائمة العمل" else "رائع! تم إنجاز المهمة 🎉"
            showFeedback(msg)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showFeedback("تم حذف المهمة")
        }
    }

    fun rescheduleTask(task: TaskEntity, daysToAdd: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, daysToAdd)
            }
            repository.rescheduleTask(task.id, cal.timeInMillis)
            showFeedback("تمت إعادة جدولة المهمة بنجاح")
        }
    }

    // Study Timer State & Actions
    private val _timerSelectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val timerSelectedSubject: StateFlow<SubjectEntity?> = _timerSelectedSubject.asStateFlow()

    private val _timerTaskName = MutableStateFlow("")
    val timerTaskName: StateFlow<String> = _timerTaskName.asStateFlow()

    private val _timerDurationMinutes = MutableStateFlow(25)
    val timerDurationMinutes: StateFlow<Int> = _timerDurationMinutes.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSessionType = MutableStateFlow("POMODORO") // FREE, POMODORO
    val timerSessionType: StateFlow<String> = _timerSessionType.asStateFlow()

    private val _showReflectionDialog = MutableStateFlow(false)
    val showReflectionDialog: StateFlow<Boolean> = _showReflectionDialog.asStateFlow()

    private var timerJob: Job? = null

    fun setTimerConfig(subject: SubjectEntity?, taskName: String, durationMin: Int, type: String) {
        _timerSelectedSubject.value = subject
        _timerTaskName.value = taskName
        _timerDurationMinutes.value = durationMin
        _timerRemainingSeconds.value = durationMin * 60
        _timerSessionType.value = type
        pauseTimer()
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerRemainingSeconds.value > 0) {
                delay(1000L)
                _timerRemainingSeconds.value -= 1
            }
            if (_timerRemainingSeconds.value <= 0) {
                _isTimerRunning.value = false
                _showReflectionDialog.value = true
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        pauseTimer()
        _timerRemainingSeconds.value = _timerDurationMinutes.value * 60
    }

    fun finishSessionEarly() {
        pauseTimer()
        _showReflectionDialog.value = true
    }

    fun dismissReflectionDialog() {
        _showReflectionDialog.value = false
    }

    fun saveCompletedSession(rating: String, achievedGoal: String, notes: String) {
        viewModelScope.launch {
            val plannedMin = _timerDurationMinutes.value
            val actualSec = (plannedMin * 60) - _timerRemainingSeconds.value
            val sub = _timerSelectedSubject.value
            repository.recordStudySession(
                subjectId = sub?.id,
                subjectName = sub?.name ?: "مذاكرة عامة",
                taskName = _timerTaskName.value,
                plannedMinutes = plannedMin,
                actualSeconds = if (actualSec > 0) actualSec else plannedMin * 60,
                sessionType = _timerSessionType.value,
                rating = rating,
                achievedGoal = achievedGoal,
                notes = notes
            )
            _showReflectionDialog.value = false
            _showStudyTimer.value = false
            showFeedback("أحسنت! تم تسجيل جلسة الدراسة وإضافتها لسجلك 🌟")
            resetTimer()
        }
    }

    // Weak Points and Spaced Reviews
    fun addWeakPoint(subject: SubjectEntity?, topic: String, mistakeDesc: String) {
        viewModelScope.launch {
            repository.addWeakPoint(
                subjectId = subject?.id,
                subjectName = subject?.name ?: "عام",
                topic = topic,
                mistakeDesc = mistakeDesc
            )
            showFeedback("تم تسجيل نقطة الضعف للمراجعة لاحقًا")
        }
    }

    fun deleteWeakPoint(item: WeakPointEntity) {
        viewModelScope.launch {
            repository.deleteWeakPoint(item)
            showFeedback("تم حذف نقطة الضعف")
        }
    }

    fun scheduleSpacedReview(subject: SubjectEntity?, topic: String, days: Int) {
        viewModelScope.launch {
            repository.addReviewItem(
                subjectId = subject?.id,
                subjectName = subject?.name ?: "عام",
                topic = topic,
                intervalDays = days
            )
            showFeedback("تمت جدولة مراجعة الموضوع بعد $days أيام")
        }
    }

    fun completeReview(item: ReviewItemEntity) {
        viewModelScope.launch {
            repository.completeReviewItem(item)
            showFeedback("تمت مراجعة الموضوع بنجاح 👏")
        }
    }

    // Notes
    fun addNote(subject: SubjectEntity?, title: String, content: String) {
        viewModelScope.launch {
            repository.addNote(
                subjectId = subject?.id,
                subjectName = subject?.name ?: "",
                title = title,
                content = content
            )
            showFeedback("تم حفظ الملاحظة")
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
            showFeedback("تم تعديل الملاحظة")
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            showFeedback("تم حذف الملاحظة")
        }
    }

    // Goals
    fun addGoal(title: String, targetVal: Int, unit: String, deadlineMillis: Long) {
        viewModelScope.launch {
            repository.addGoal(title, targetVal, unit, deadlineMillis)
            showFeedback("تمت إضافة الهدف بنجاح")
        }
    }

    fun updateGoalProgress(goal: GoalEntity, newCurrentVal: Int) {
        viewModelScope.launch {
            val completed = newCurrentVal >= goal.targetValue
            repository.updateGoal(goal.copy(currentValue = newCurrentVal, isCompleted = completed))
            if (completed) {
                showFeedback("مبروك! لقد حققت هدفك 🎉")
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            showFeedback("تم حذف الهدف")
        }
    }

    // Gemini AI Assistant State & Actions
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "أهلاً بك يا بطل! أنا مساعدك الدراسي الذكي 🎓. يمكنك سؤالي عن أي مفهوم، أو رفع مسألة لحلها خطوة بخطوة، أو طلب تلميحات تدريجية بدون حرق الإجابة، أو إنشاء اختبار تدريبي لأي مادة!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAssistantLoading = MutableStateFlow(false)
    val isAssistantLoading: StateFlow<Boolean> = _isAssistantLoading.asStateFlow()

    private val _currentHintLevel = MutableStateFlow(0)
    val currentHintLevel: StateFlow<Int> = _currentHintLevel.asStateFlow()

    private val _lastUserQuestion = MutableStateFlow("")

    fun askGemini(prompt: String, bitmap: Bitmap? = null, isHintRequest: Boolean = false) {
        if (prompt.isBlank() && bitmap == null) return

        val nextHintLevel = if (isHintRequest) {
            val next = _currentHintLevel.value + 1
            if (next > 3) 3 else next
        } else {
            _currentHintLevel.value = 0
            0
        }
        _currentHintLevel.value = nextHintLevel

        val questionText = if (isHintRequest && prompt.isBlank()) {
            _lastUserQuestion.value
        } else {
            prompt
        }
        _lastUserQuestion.value = questionText

        val userMsg = if (isHintRequest) {
            "طلب تلميح ($nextHintLevel من 3)"
        } else {
            questionText
        }

        _chatMessages.value = _chatMessages.value + ChatMessage(
            isUser = true,
            text = userMsg,
            imageBitmap = bitmap,
            hintLevel = nextHintLevel
        )

        _isAssistantLoading.value = true

        viewModelScope.launch {
            when (val result = geminiService.askAssistant(questionText, bitmap, nextHintLevel)) {
                is GeminiResult.Success -> {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        isUser = false,
                        text = result.text,
                        hintLevel = nextHintLevel
                    )
                }
                is GeminiResult.Error -> {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        isUser = false,
                        text = "⚠️ ${result.message}"
                    )
                }
                else -> Unit
            }
            _isAssistantLoading.value = false
        }
    }

    // Interactive Quiz Generator State
    private val _activeQuiz = MutableStateFlow<GeneratedQuiz?>(null)
    val activeQuiz: StateFlow<GeneratedQuiz?> = _activeQuiz.asStateFlow()

    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    private val _quizSelectedAnswer = MutableStateFlow<Int?>(null)
    val quizSelectedAnswer: StateFlow<Int?> = _quizSelectedAnswer.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    fun generateQuiz(subject: String, topic: String, count: Int, difficulty: String) {
        _isAssistantLoading.value = true
        viewModelScope.launch {
            when (val result = geminiService.generateQuiz(subject, topic, count, difficulty)) {
                is GeminiResult.QuizSuccess -> {
                    _activeQuiz.value = result.quiz
                    _quizQuestionIndex.value = 0
                    _quizSelectedAnswer.value = null
                    _quizScore.value = 0
                    _quizCompleted.value = false
                }
                is GeminiResult.Error -> {
                    showFeedback("⚠️ ${result.message}")
                }
                else -> Unit
            }
            _isAssistantLoading.value = false
        }
    }

    fun answerQuizQuestion(selectedIdx: Int) {
        val quiz = _activeQuiz.value ?: return
        if (_quizSelectedAnswer.value != null) return // Already answered
        _quizSelectedAnswer.value = selectedIdx

        val currentQ = quiz.questions.getOrNull(_quizQuestionIndex.value) ?: return
        val isCorrect = (selectedIdx == currentQ.correctIndex)
        if (isCorrect) {
            _quizScore.value += 1
        } else {
            // Automatically record as weak point!
            viewModelScope.launch {
                val subEntity = subjects.value.firstOrNull { it.name == quiz.subject }
                repository.addWeakPoint(
                    subjectId = subEntity?.id,
                    subjectName = quiz.subject,
                    topic = quiz.title,
                    mistakeDesc = "خطأ في سؤال: ${currentQ.questionText.take(60)}... (${currentQ.explanation.take(80)})"
                )
            }
        }
    }

    fun nextQuizQuestion() {
        val quiz = _activeQuiz.value ?: return
        if (_quizQuestionIndex.value + 1 < quiz.questions.size) {
            _quizQuestionIndex.value += 1
            _quizSelectedAnswer.value = null
        } else {
            _quizCompleted.value = true
        }
    }

    fun closeQuiz() {
        _activeQuiz.value = null
        _quizQuestionIndex.value = 0
        _quizSelectedAnswer.value = null
        _quizScore.value = 0
        _quizCompleted.value = false
    }

    // Seed Sample Data / Reset Data
    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            showFeedback("تمت إضافة بيانات تجريبية دراسية بنجاح ✨")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showFeedback("تم مسح كافة البيانات وإعادة التطبيق إلى حالته الأولى")
        }
    }

    suspend fun exportJson(): String = repository.exportDataToJson()
    suspend fun importJson(json: String): Boolean = repository.importDataFromJson(json)
}
