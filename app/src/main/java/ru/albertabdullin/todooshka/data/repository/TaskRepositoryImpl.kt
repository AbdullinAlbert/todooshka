package ru.albertabdullin.todooshka.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.albertabdullin.todooshka.data.db.dao.TaskDao
import ru.albertabdullin.todooshka.data.mapper.toDomainTask
import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import java.time.LocalDate

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {
    override suspend fun getDateLeftBound(): Flow<LocalDate> {
        return flowOf(LocalDate.now())
    }

    override fun getTasks(selectedDate: LocalDate): Flow<List<Task>> {
        return taskDao.getTasks(selectedDate).map { taskRelations ->
            taskRelations.map {
                it.toDomainTask()
            }
        }
    }
}