package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "طالب مجتهد",
    val gradeLevel: String = "المرحلة الثانوية",
    val schoolOrTrack: String = "علمي",
    val studyGoal: String = "تحسين مستواي وتنظيم وقتي",
    val dailyTargetMinutes: Int = 60,
    val hasCompletedOnboarding: Boolean = false,
    val preferredTheme: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val studyStreakDays: Int = 1,
    val lastStudyDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "book", // book, math, science, language, art, code
    val colorHex: String = "#2563EB",
    val description: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long,
    val estimatedMinutes: Int = 30,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val status: String = "TODO", // TODO, IN_PROGRESS, COMPLETED
    val taskType: String = "HOMEWORK", // HOMEWORK, REVISION, STUDY, PROJECT, EXAM, PERSONAL
    val completedAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val subjectName: String,
    val taskName: String = "",
    val plannedMinutes: Int,
    val actualSeconds: Int,
    val sessionType: String = "FREE", // FREE, POMODORO
    val rating: String = "GOOD", // EXCELLENT, GOOD, MODERATE, HARD
    val achievedGoal: String = "YES", // YES, PARTIAL, NO
    val notes: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "weak_points")
data class WeakPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val subjectName: String,
    val topic: String,
    val mistakeDescription: String,
    val mistakeCount: Int = 1,
    val status: String = "NEEDS_REVIEW", // NEEDS_REVIEW, IN_PROGRESS, MASTERED
    val lastRecordedMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "review_items")
data class ReviewItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val subjectName: String,
    val topic: String,
    val scheduledDateMillis: Long,
    val intervalDays: Int = 1,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val subjectName: String = "",
    val title: String,
    val content: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val unit: String = "ساعة",
    val deadlineMillis: Long,
    val isCompleted: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAtMillis: Long? = null
)
