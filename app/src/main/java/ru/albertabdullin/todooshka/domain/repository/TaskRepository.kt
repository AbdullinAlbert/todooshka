package ru.albertabdullin.todooshka.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.albertabdullin.todooshka.domain.entity.Task
import java.time.LocalDate

interface TaskRepository {
    suspend fun getDateLeftBound(): Flow<LocalDate>

    suspend fun getTasks(selectedDate: LocalDate): List<Task>
}