package ru.albertabdullin.todooshka.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.albertabdullin.todooshka.domain.entity.Task
import java.time.LocalDate

interface TaskRepository {
    suspend fun getDateLeftBound(): Flow<LocalDate>

    fun getTasks(selectedDate: LocalDate): Flow<List<Task>>
}