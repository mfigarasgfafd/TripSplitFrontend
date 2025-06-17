package com.example.tripsplit

import com.google.gson.annotations.SerializedName

//data class UserRegistration(
//    @SerializedName("name") // Match JSON key
//    val name: String,
//
//    @SerializedName("email") // Match JSON key
//    val email: String,
//
//    @SerializedName("password") // Match JSON key
//    val password: String
//)

// Data classes
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String
)

data class GroupRequest(
    val description: String,
    @SerializedName("group_start_date")
    val groupStartDate: String,
    @SerializedName("group_end_date")
    val groupEndDate: String,
    val location: String,
    val name: String
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
//data class LoginRequest(
//    val email: String,
//    val name: String,  // Note: This might be optional depending on your backend
//    val password: String
//)

// Login Response
// Login response
//data class LoginResponse(
//    val token: String
//)


// Auth response
data class AuthResponse(
    val token: String
)

data class AuthRequest(
    val email: String,
    val name: String,
    val password: String
)

// User registration request
data class UserRegistration(
    val name: String,
    val email: String,
    val password: String
)

// Login request
data class LoginRequest(
    val email: String,
    val password: String
)

//// Group request
//data class GroupRequest(
//    val name: String,
//    val owner: Int
//)