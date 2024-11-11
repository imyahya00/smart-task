package com.tcp.smarttasks.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcp.smarttasks.data.TaskResponse
import com.tcp.smarttasks.db.dao.TaskDao
import com.tcp.smarttasks.network.ApiState
import com.tcp.smarttasks.network.RestClient
import com.tcp.smarttasks.network.Status
import com.tcp.smarttasks.repo.TasksRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TasksViewModel: ViewModel() {
    private val repository = TasksRepo(
        RestClient.getInstance()
    )

    val taskState = MutableStateFlow(
        ApiState(
            Status.LOADING,
            TaskResponse(), ""
        )
    )


    fun getAllTasks(taskDao: TaskDao) {
        taskState.value = ApiState.loading()

        viewModelScope.launch {
            repository.getTasks(taskDao)
                // in case error, set the State to Error
                .catch {
                    taskState.value = ApiState.error(it.message.toString())
                }
                // in case succeeded, set the State to Success
                .collect {
                    taskState.value = ApiState.success(it.data)
                }
        }
    }

    fun loadTasksFromDb(taskDao: TaskDao) {
        viewModelScope.launch {
            repository.getAllTasksFromDb(taskDao)
                .collect { tasks ->
                    taskState.value = ApiState.success(TaskResponse(tasks))
                }
        }
    }

}
