package ru.albertabdullin.todooshka.data.mapper

import ru.albertabdullin.todooshka.data.db.entity.Subtask
import ru.albertabdullin.todooshka.data.db.relation.TaskRelation
import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.domain.entity.Subtask as DomainSubtask

fun TaskRelation.toDomainTask(): Task {
    return Task(
        id = task.id,
        description = task.description,
        completedOn = task.completedOn,
        startOn = task.startOn,
        durationDays = task.durationDays,
        sort = task.sort,
        subtasks = subtasks.map { it.toDomain() }
    )
}

fun Subtask.toDomain(): DomainSubtask {
    return DomainSubtask(
        id = id,
        taskId = taskId,
        description = description,
        completedOn = completedOn,
        sort = sort
    )
}