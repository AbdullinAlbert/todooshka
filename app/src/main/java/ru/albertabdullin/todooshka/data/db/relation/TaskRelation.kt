package ru.albertabdullin.todooshka.data.db.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import ru.albertabdullin.todooshka.data.db.entity.Subtask
import ru.albertabdullin.todooshka.data.db.entity.Task

data class TaskRelation(
    @Embedded val task: Task,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["taskId"]
    )
    val subtasks: List<Subtask>
)
