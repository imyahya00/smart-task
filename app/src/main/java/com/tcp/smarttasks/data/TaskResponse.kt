package com.tcp.smarttasks.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

data class TaskResponse(
    val tasks: List<Task>? = null
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String,
    val TargetDate: String,
    val DueDate: String?, // DueDate can be null
    val Title: String,
    val Description: String,
    val Priority: Int?, // Priority can be null
    var taskStatus: String? = "",
    var comments: String? = ""
): Serializable