package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY dueDateMillis ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId ORDER BY dueDateMillis ASC")
    fun getTasksBySubject(subjectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, completedAtMillis = :completedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: String, completedAt: Long?)

    @Query("UPDATE tasks SET dueDateMillis = :newDueDate WHERE id = :id")
    suspend fun rescheduleTask(id: Long, newDueDate: Long)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestampMillis DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE timestampMillis >= :fromTimestampMillis ORDER BY timestampMillis DESC")
    fun getStudySessionsSince(fromTimestampMillis: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    // Weak Points
    @Query("SELECT * FROM weak_points ORDER BY lastRecordedMillis DESC")
    fun getAllWeakPoints(): Flow<List<WeakPointEntity>>

    @Query("SELECT * FROM weak_points WHERE subjectId = :subjectId ORDER BY lastRecordedMillis DESC")
    fun getWeakPointsBySubject(subjectId: Long): Flow<List<WeakPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeakPoint(weakPoint: WeakPointEntity): Long

    @Update
    suspend fun updateWeakPoint(weakPoint: WeakPointEntity)

    @Delete
    suspend fun deleteWeakPoint(weakPoint: WeakPointEntity)

    // Review Items
    @Query("SELECT * FROM review_items ORDER BY scheduledDateMillis ASC")
    fun getAllReviewItems(): Flow<List<ReviewItemEntity>>

    @Query("SELECT * FROM review_items WHERE isCompleted = 0 ORDER BY scheduledDateMillis ASC")
    fun getPendingReviewItems(): Flow<List<ReviewItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewItem(item: ReviewItemEntity): Long

    @Update
    suspend fun updateReviewItem(item: ReviewItemEntity)

    @Delete
    suspend fun deleteReviewItem(item: ReviewItemEntity)

    // Notes
    @Query("SELECT * FROM notes ORDER BY updatedAtMillis DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subjectId = :subjectId ORDER BY updatedAtMillis DESC")
    fun getNotesBySubject(subjectId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // Goals
    @Query("SELECT * FROM goals ORDER BY deadlineMillis ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // Achievements
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Database Reset
    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM study_sessions")
    suspend fun clearStudySessions()

    @Query("DELETE FROM weak_points")
    suspend fun clearWeakPoints()

    @Query("DELETE FROM review_items")
    suspend fun clearReviewItems()

    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()
}
