package ru.albertabdullin.todooshka.data.db.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import ru.albertabdullin.todooshka.data.db.entity.Subtask
import ru.albertabdullin.todooshka.data.db.entity.Task
import ru.albertabdullin.todooshka.data.db.relation.TaskRelation
import java.time.LocalDate

@Dao
interface TaskDao {

    @Transaction
    @Query(" SELECT * FROM tasks WHERE startOn = :date")
    fun getTasks(date: LocalDate): Flow<List<TaskRelation>>

    @Insert
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTasks(tasks: List<Task>)

    @Delete
    suspend fun deleteTasks(tasks: List<Task>)

    @Insert
    suspend fun insertSubtasks(tasks: List<Subtask>)

    @Update
    suspend fun updateSubtasks(tasks: List<Subtask>)

    @Delete
    suspend fun deleteSubtasks(tasks: List<Subtask>)

    @Transaction
    suspend fun saveTasks(
        tasksToAdd: List<Task>,
        tasksToUpdate: List<Task>,
        tasksToDelete: List<Task>,
        subtasksToAdd: List<Subtask>,
        subtasksToUpdate: List<Subtask>,
        subtasksToDelete: List<Subtask>,
    ) {
        insertTasks(tasksToAdd)
        updateTasks(tasksToUpdate)
        deleteTasks(tasksToDelete)
        insertSubtasks(subtasksToAdd)
        updateSubtasks(subtasksToUpdate)
        deleteSubtasks(subtasksToDelete)
    }
}