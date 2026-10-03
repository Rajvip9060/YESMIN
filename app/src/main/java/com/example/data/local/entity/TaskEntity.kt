package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val hasReminder: Boolean = false,
    val priority: String = "Medium", // "Low", "Medium", "High"
    val createdAt: Long = System.currentTimeMillis()
)
