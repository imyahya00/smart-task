package com.tcp.smarttasks.network

import com.tcp.smarttasks.data.Task
import com.tcp.smarttasks.data.TaskResponse
import retrofit2.http.GET

interface ApiService {
    @GET("/")
    suspend fun getTaskList(): TaskResponse
}