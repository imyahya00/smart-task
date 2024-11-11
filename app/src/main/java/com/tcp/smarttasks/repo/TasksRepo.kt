package com.tcp.smarttasks.repo

import com.tcp.smarttasks.data.Task
import com.tcp.smarttasks.data.TaskResponse
import com.tcp.smarttasks.db.dao.TaskDao
import com.tcp.smarttasks.network.ApiService
import com.tcp.smarttasks.network.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TasksRepo(private val apiService: ApiService) {
    suspend fun getTasks(taskDao: TaskDao): Flow<ApiState<TaskResponse>> {
        return flow {
            val tasks = apiService.getTaskList()
            getTasksFromApiAndUpdateDb(taskDao, tasks.tasks)
            taskDao.getAllTasks().collect{ tasksDb ->
                emit(ApiState.success(TaskResponse(tasksDb)))
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun getTasksFromApiAndUpdateDb(taskDao: TaskDao, tasksFromApi: List<Task>?) {

        val existingTasks = taskDao.getAllTasks().first()

        val newTasks = tasksFromApi?.filterNot { apiTask ->
            existingTasks.any { dbTask -> dbTask.id == apiTask.id }
        } ?: emptyList()

        if (newTasks.isNotEmpty()) {
            taskDao.insertTasks(newTasks)
        }
    }


    fun getAllTasksFromDb(taskDao: TaskDao): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
}