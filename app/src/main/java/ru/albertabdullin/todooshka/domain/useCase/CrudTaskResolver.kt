package ru.albertabdullin.todooshka.domain.useCase

import ru.albertabdullin.todooshka.domain.entity.Task

class CrudTaskResolver {
    fun resolve(
        currentDbSnapshot: List<Task>,
        tasksFromUser: List<Task>,
    ): CrudTaskResolverResult {
        val taskWithValidData = tasksFromUser.filter { it.description.isNotBlank() }
        if (taskWithValidData.isEmpty() && currentDbSnapshot.isEmpty()) return NothingToSave

        val invalidTaskIndex = tasksFromUser.indexOfFirst { it.description.isBlank() }
        if (invalidTaskIndex != -1) return InvalidData(index = invalidTaskIndex)

        val helperCurrentDbSnapshotMap = currentDbSnapshot.associateBy { it.id }
        require(currentDbSnapshot.size == helperCurrentDbSnapshotMap.size) {
            "Duplicate task IDs in the database snapshot"
        }
        val helperTasksFromUserMap = tasksFromUser.associateBy { it.id }
        require(tasksFromUser.size == helperTasksFromUserMap.size) {
            "Duplicate task IDs in the user snapshot"
        }
        val tasksToAdd = resolveTasksToAdd(
            currentDbSnapshot = helperCurrentDbSnapshotMap,
            tasksFromUser = helperTasksFromUserMap
        )
        val tasksToUpdate = resolveTasksToUpdate(
            currentDbSnapshot = helperCurrentDbSnapshotMap,
            tasksFromUser = helperTasksFromUserMap
        )
        val tasksToDelete = resolveTasksToDelete(
            currentDbSnapshot = helperCurrentDbSnapshotMap,
            tasksFromUser = helperTasksFromUserMap
        )
        return AllowedForDb(
            tasksToAdd = tasksToAdd,
            tasksToUpdate = tasksToUpdate,
            tasksToDelete = tasksToDelete
        )
    }

    private fun resolveTasksToAdd(
        currentDbSnapshot: Map<Int, Task>,
        tasksFromUser: Map<Int, Task>,
    ): List<Task> {
        return tasksFromUser.minus(currentDbSnapshot.keys).values.toList()
    }

    private fun resolveTasksToUpdate(
        currentDbSnapshot: Map<Int, Task>,
        tasksFromUser: Map<Int, Task>,
    ): List<Task> {
        val commonTasksIds = currentDbSnapshot.keys.intersect(tasksFromUser.keys)
        return tasksFromUser.filter { commonTasksIds.contains(it.key) }.values.toList()
    }

    private fun resolveTasksToDelete(
        currentDbSnapshot: Map<Int, Task>,
        tasksFromUser: Map<Int, Task>,
    ): List<Task> {
        return currentDbSnapshot.minus(tasksFromUser.keys).values.toList()
    }
}

sealed interface CrudTaskResolverResult

data class AllowedForDb(
    val tasksToAdd: List<Task>,
    val tasksToUpdate: List<Task>,
    val tasksToDelete: List<Task>
) : CrudTaskResolverResult

data object NothingToSave: CrudTaskResolverResult

data class InvalidData(val index: Int): CrudTaskResolverResult

