package ru.albertabdullin.todooshka.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    suspend fun getDateLeftBound(): Flow<LocalDate>
}