package ru.albertabdullin.todooshka.data.db

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import ru.albertabdullin.todooshka.data.db.dao.TaskDao
import ru.albertabdullin.todooshka.data.db.entity.Subtask
import ru.albertabdullin.todooshka.data.db.entity.Task

@Database(entities = [Task::class, Subtask::class], version = 1)
@ColumnTypeConverters(TypeConverters::class)
abstract class TodooshkaDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}