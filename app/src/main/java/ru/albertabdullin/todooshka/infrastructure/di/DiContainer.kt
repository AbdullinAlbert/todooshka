package ru.albertabdullin.todooshka.infrastructure.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import ru.albertabdullin.todooshka.data.db.TodooshkaDatabase
import ru.albertabdullin.todooshka.data.repository.TaskRepositoryImpl
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import ru.albertabdullin.todooshka.domain.useCase.TasksUseCase

class DiContainer(context: Context) {
    val taskRepository: TaskRepository by lazy {
        createTaskRepository()
    }

    private val db: TodooshkaDatabase by lazy {
        Room
            .databaseBuilder<TodooshkaDatabase>(context, "todooshka.db")
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    private fun createTaskRepository(): TaskRepository {
        return TaskRepositoryImpl()
    }


    fun getTasksUseCaseInstance(): TasksUseCase {
        return TasksUseCase(taskRepository)
    }

}