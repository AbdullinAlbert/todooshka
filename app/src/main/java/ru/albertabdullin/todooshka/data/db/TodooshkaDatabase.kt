package ru.albertabdullin.todooshka.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import ru.albertabdullin.todooshka.data.db.entity.Task

@Database(entities = [Task::class], version = 1)
abstract class TodooshkaDatabase : RoomDatabase() {
}