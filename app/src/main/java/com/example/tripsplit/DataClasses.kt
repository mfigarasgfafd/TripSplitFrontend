package com.example.tripsplit

import com.google.gson.annotations.SerializedName

data class UserRegistration(
    @SerializedName("name") // Match JSON key
    val name: String,

    @SerializedName("email") // Match JSON key
    val email: String,

    @SerializedName("password") // Match JSON key
    val password: String
)

// Data classes
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String
)

data class GroupRequest(
    val name: String,
    val owner: Int
)

data class Group(
    val id: Int,
    val name: String,
    val owner: Int,
    val members: List<Int>,
    val expenses: List<Int> = emptyList()
)

data class CreateUserResponse(
    val id: Int,
    val success: Boolean
)
