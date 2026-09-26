package ru.albertabdullin.todooshka.infrastructure.di

import ru.albertabdullin.todooshka.data.repository.TaskRepositoryImpl
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import ru.albertabdullin.todooshka.domain.useCase.TasksUseCase

class DiContainer {
    private var taskRepository: TaskRepository? = null

    fun getTaskRepositorySingleton(): TaskRepository {
        if (taskRepository == null) {
            taskRepository = createTaskRepository()
        }
        return taskRepository!!
    }

    private fun createTaskRepository(): TaskRepository {
        return TaskRepositoryImpl()
    }


    fun getTasksUseCaseInstance(): TasksUseCase {
        return TasksUseCase(
            getTaskRepositorySingleton()
        )
    }

}