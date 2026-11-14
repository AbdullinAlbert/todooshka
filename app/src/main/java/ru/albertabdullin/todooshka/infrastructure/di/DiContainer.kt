package ru.albertabdullin.todooshka.infrastructure.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import ru.albertabdullin.todooshka.data.db.TodooshkaDatabase
import ru.albertabdullin.todooshka.data.repository.TaskRepositoryImpl
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import ru.albertabdullin.todooshka.domain.useCase.CrudTaskResolver
import ru.albertabdullin.todooshka.domain.useCase.TasksUseCase

class DiContainer(context: Context) {
    val taskRepository: TaskRepository by lazy {
        createTaskRepository(db)
    }

    private val db: TodooshkaDatabase by lazy {
        Room
            .databaseBuilder<TodooshkaDatabase>(context, "todooshka.db")
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    private fun createTaskRepository(db: TodooshkaDatabase): TaskRepository {
        return TaskRepositoryImpl(db.taskDao())
    }


    fun getTasksUseCaseInstance(): TasksUseCase {
        return TasksUseCase(taskRepository, CrudTaskResolver())
    }

}