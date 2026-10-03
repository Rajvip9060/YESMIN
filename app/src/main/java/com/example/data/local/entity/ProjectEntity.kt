package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Android App", "Website", "AI Assistant", "Coding Project", "Study Project", "Custom"
    val description: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "active" // "active", "completed", "archived"
)
