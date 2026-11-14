package ru.albertabdullin.todooshka.data.mapper

import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.data.db.entity.Task as DbTask

fun Task.toDbEntity(isNew: Boolean = false): DbTask {
    return DbTask(
        id = if (isNew) 0 else id,
        description = description,
        completedOn = completedOn,
        startOn = startOn,
        durationDays = durationDays,
        sort = sort
    )
}