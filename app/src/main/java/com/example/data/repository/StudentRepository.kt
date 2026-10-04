package com.example.data.repository

import com.example.data.local.dao.StudentDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class StudentRepository(private val dao: StudentDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = dao.getPendingTasks()
    val allStudySessions: Flow<List<StudySessionEntity>> = dao.getAllStudySessions()
    val allWeakPoints: Flow<List<WeakPointEntity>> = dao.getAllWeakPoints()
    val allReviewItems: Flow<List<ReviewItemEntity>> = dao.getAllReviewItems()
    val pendingReviewItems: Flow<List<ReviewItemEntity>> = dao.getPendingReviewItems()
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()
    val allAchievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()

    suspend fun getProfileOnce(): UserProfileEntity? = dao.getUserProfileOnce()

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun addSubject(name: String, icon: String, colorHex: String, desc: String): Long {
        return dao.insertSubject(
            SubjectEntity(
                name = name,
                iconName = icon,
                colorHex = colorHex,
                description = desc
            )
        )
    }

    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    suspend fun addTask(
        subjectId: Long?,
        title: String,
        description: String,
        dueDateMillis: Long,
        estimatedMinutes: Int,
        priority: String,
        taskType: String
    ): Long {
        return dao.insertTask(
            TaskEntity(
                subjectId = subjectId,
                title = title,
                description = description,
                dueDateMillis = dueDateMillis,
                estimatedMinutes = estimatedMinutes,
                priority = priority,
                taskType = taskType
            )
        )
    }

    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        val newStatus = if (task.status == "COMPLETED") "TODO" else "COMPLETED"
        val completedAt = if (newStatus == "COMPLETED") System.currentTimeMillis() else null
        dao.updateTaskStatus(task.id, newStatus, completedAt)
    }

    suspend fun rescheduleTask(taskId: Long, newDueDate: Long) {
        dao.rescheduleTask(taskId, newDueDate)
    }

    suspend fun recordStudySession(
        subjectId: Long?,
        subjectName: String,
        taskName: String,
        plannedMinutes: Int,
        actualSeconds: Int,
        sessionType: String,
        rating: String,
        achievedGoal: String,
        notes: String
    ): Long {
        val id = dao.insertStudySession(
            StudySessionEntity(
                subjectId = subjectId,
                subjectName = subjectName,
                taskName = taskName,
                plannedMinutes = plannedMinutes,
                actualSeconds = actualSeconds,
                sessionType = sessionType,
                rating = rating,
                achievedGoal = achievedGoal,
                notes = notes,
                timestampMillis = System.currentTimeMillis()
            )
        )
        // Check and update streak
        updateStudyStreak()
        return id
    }

    private suspend fun updateStudyStreak() {
        val currentProfile = dao.getUserProfileOnce() ?: UserProfileEntity()
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = currentProfile.lastStudyDateMillis }

        val diffDays = (calNow.get(Calendar.DAY_OF_YEAR) - calLast.get(Calendar.DAY_OF_YEAR)) +
                (calNow.get(Calendar.YEAR) - calLast.get(Calendar.YEAR)) * 365

        val newStreak = when (diffDays) {
            0 -> currentProfile.studyStreakDays // Same day, keep streak
            1 -> currentProfile.studyStreakDays + 1 // Consecutive day, increase
            else -> 1 // Broken streak, restart
        }

        dao.insertOrUpdateProfile(
            currentProfile.copy(
                studyStreakDays = newStreak,
                lastStudyDateMillis = now
            )
        )
    }

    suspend fun addWeakPoint(
        subjectId: Long?,
        subjectName: String,
        topic: String,
        mistakeDesc: String
    ): Long {
        return dao.insertWeakPoint(
            WeakPointEntity(
                subjectId = subjectId,
                subjectName = subjectName,
                topic = topic,
                mistakeDescription = mistakeDesc
            )
        )
    }

    suspend fun updateWeakPoint(weakPoint: WeakPointEntity) = dao.updateWeakPoint(weakPoint)
    suspend fun deleteWeakPoint(weakPoint: WeakPointEntity) = dao.deleteWeakPoint(weakPoint)

    suspend fun addReviewItem(
        subjectId: Long?,
        subjectName: String,
        topic: String,
        intervalDays: Int
    ): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, intervalDays)
        return dao.insertReviewItem(
            ReviewItemEntity(
                subjectId = subjectId,
                subjectName = subjectName,
                topic = topic,
                scheduledDateMillis = cal.timeInMillis,
                intervalDays = intervalDays,
                isCompleted = false
            )
        )
    }

    suspend fun completeReviewItem(item: ReviewItemEntity) {
        dao.updateReviewItem(item.copy(isCompleted = true))
    }

    suspend fun deleteReviewItem(item: ReviewItemEntity) = dao.deleteReviewItem(item)

    suspend fun addNote(subjectId: Long?, subjectName: String, title: String, content: String): Long {
        return dao.insertNote(
            NoteEntity(
                subjectId = subjectId,
                subjectName = subjectName,
                title = title,
                content = content,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note.copy(updatedAtMillis = System.currentTimeMillis()))
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)

    suspend fun addGoal(title: String, targetValue: Int, unit: String, deadlineMillis: Long): Long {
        return dao.insertGoal(
            GoalEntity(
                title = title,
                targetValue = targetValue,
                currentValue = 0,
                unit = unit,
                deadlineMillis = deadlineMillis,
                isCompleted = false
            )
        )
    }

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    suspend fun initializeDefaultAchievements() {
        val defaults = listOf(
            AchievementEntity("first_session", "أول خطوة", "أكملت أول جلسة دراسة بنجاح", "timer"),
            AchievementEntity("five_sessions", "طالب ملتزم", "أكملت 5 جلسات دراسية", "star"),
            AchievementEntity("ten_tasks", "إنجاز حقيقي", "أنجزت 10 مهام دراسية", "check"),
            AchievementEntity("focus_100", "سيد التركيز", "درست أكثر من 100 دقيقة تركيز", "bolt"),
            AchievementEntity("first_quiz", "مستعد للتحدي", "خضت أول اختبار تدريبي مع المساعد", "quiz"),
            AchievementEntity("streak_3", "عادة مستمرة", "حافظت على سلسلة دراسة لمدة 3 أيام متتالية", "flame")
        )
        dao.insertAchievements(defaults)
    }

    suspend fun seedSampleData() {
        // Clear first
        dao.clearSubjects()
        dao.clearTasks()
        dao.clearWeakPoints()
        dao.clearReviewItems()
        dao.clearNotes()
        dao.clearGoals()

        val mathId = dao.insertSubject(
            SubjectEntity(name = "الرياضيات", iconName = "math", colorHex = "#2563EB", description = "الجبر، التفاضل، والهندسة")
        )
        val physicsId = dao.insertSubject(
            SubjectEntity(name = "الفيزياء", iconName = "science", colorHex = "#7C3AED", description = "الميكانيكا وقوانين نيوتن والكهرباء")
        )
        val arabicId = dao.insertSubject(
            SubjectEntity(name = "اللغة العربية", iconName = "book", colorHex = "#059669", description = "النحو، البلاغة، والأدب")
        )
        val englishId = dao.insertSubject(
            SubjectEntity(name = "اللغة الإنجليزية", iconName = "language", colorHex = "#D97706", description = "القواعد، المفردات، والتعبير")
        )

        val cal = Calendar.getInstance()
        // Today task
        cal.add(Calendar.HOUR_OF_DAY, 4)
        dao.insertTask(
            TaskEntity(
                subjectId = mathId,
                title = "حل 10 مسائل على النهايات",
                description = "صفحة 42 تمارين الكتاب المدرسي",
                dueDateMillis = cal.timeInMillis,
                estimatedMinutes = 30,
                priority = "HIGH",
                taskType = "HOMEWORK"
            )
        )
        // Today review
        cal.add(Calendar.HOUR_OF_DAY, 3)
        dao.insertTask(
            TaskEntity(
                subjectId = physicsId,
                title = "مراجعة درس قوانين نيوتن للحركة",
                description = "التركيز على القانون الثاني والمسائل",
                dueDateMillis = cal.timeInMillis,
                estimatedMinutes = 25,
                priority = "MEDIUM",
                taskType = "REVISION"
            )
        )
        // Overdue task to demonstrate smart reschedule
        cal.add(Calendar.DAY_OF_YEAR, -2)
        dao.insertTask(
            TaskEntity(
                subjectId = arabicId,
                title = "إعراب قصيدة المتنبي",
                description = "الأسئلة من 1 إلى 5",
                dueDateMillis = cal.timeInMillis,
                estimatedMinutes = 20,
                priority = "HIGH",
                taskType = "HOMEWORK"
            )
        )

        // Weak point
        dao.insertWeakPoint(
            WeakPointEntity(
                subjectId = mathId,
                subjectName = "الرياضيات",
                topic = "المعادلات الخطية والجذور",
                mistakeDescription = "الخلط بين إشارة الجذر السالب والموجب عند نقل الحدود",
                mistakeCount = 2
            )
        )

        // Spaced Review Item
        val reviewCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        dao.insertReviewItem(
            ReviewItemEntity(
                subjectId = physicsId,
                subjectName = "الفيزياء",
                topic = "حساب كمية الحركة والتصادمات",
                scheduledDateMillis = reviewCal.timeInMillis,
                intervalDays = 1
            )
        )

        // Sample Goal
        val goalCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
        dao.insertGoal(
            GoalEntity(
                title = "إنجاز 10 ساعات دراسة هذا الأسبوع",
                targetValue = 10,
                currentValue = 4,
                unit = "ساعة",
                deadlineMillis = goalCal.timeInMillis
            )
        )

        // Sample Note
        dao.insertNote(
            NoteEntity(
                subjectId = physicsId,
                subjectName = "الفيزياء",
                title = "ملخص قوانين الحركة بتسارع ثابت",
                content = "v = u + at\ns = ut + 0.5 a t^2\nv^2 = u^2 + 2as"
            )
        )
    }

    suspend fun clearAllData() {
        dao.clearUserProfile()
        dao.clearSubjects()
        dao.clearTasks()
        dao.clearStudySessions()
        dao.clearWeakPoints()
        dao.clearReviewItems()
        dao.clearNotes()
        dao.clearGoals()
        dao.clearAchievements()
        initializeDefaultAchievements()
    }

    // Export all to JSON String
    suspend fun exportDataToJson(): String {
        val root = JSONObject()
        val profile = dao.getUserProfileOnce()
        if (profile != null) {
            val pJson = JSONObject()
            pJson.put("name", profile.name)
            pJson.put("gradeLevel", profile.gradeLevel)
            pJson.put("studyGoal", profile.studyGoal)
            pJson.put("dailyTargetMinutes", profile.dailyTargetMinutes)
            root.put("profile", pJson)
        }
        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", 1)
        return root.toString(2)
    }

    // Import from JSON String with validation
    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val existing = dao.getUserProfileOnce() ?: UserProfileEntity()
                val updated = existing.copy(
                    name = p.optString("name", existing.name),
                    gradeLevel = p.optString("gradeLevel", existing.gradeLevel),
                    studyGoal = p.optString("studyGoal", existing.studyGoal),
                    dailyTargetMinutes = p.optInt("dailyTargetMinutes", existing.dailyTargetMinutes)
                )
                dao.insertOrUpdateProfile(updated)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
