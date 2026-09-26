package ru.albertabdullin.todooshka.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import java.time.LocalDate

class TaskRepositoryImpl : TaskRepository {
    override suspend fun getDateLeftBound(): Flow<LocalDate> {
        return flowOf(LocalDate.now())
    }

    override suspend fun getTasks(selectedDate: LocalDate): List<Task> {
        return emptyList()
    }
}