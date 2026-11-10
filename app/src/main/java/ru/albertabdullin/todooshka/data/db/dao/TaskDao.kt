package ru.albertabdullin.todooshka.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import ru.albertabdullin.todooshka.data.db.relation.TaskRelation
import java.time.LocalDate

@Dao
interface TaskDao {

    @Transaction
    @Query(" SELECT * FROM tasks WHERE startOn = :date")
    fun getTasks(date: LocalDate): Flow<List<TaskRelation>>
}