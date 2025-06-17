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
    val name: String,
    val owner: Int
)

data class Group(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("owner_id") val ownerId: Int? = null,
    @SerializedName("members_ids") val membersIds: List<Int>? = null,
    @SerializedName("expenses") val expenses: List<Int>? = null,
    @SerializedName("group_start_date") val groupStartDate: String? = null,
    @SerializedName("group_end_date") val groupEndDate: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("location") val location: String? = null
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
data class CreateGroupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("group_start_date") val groupStartDate: String? = null,
    @SerializedName("group_end_date") val groupEndDate: String? = null
)

data class JoinGroupRequest(
    @SerializedName("group_id") val groupId: Int
)

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

// Expense data class
data class Expense(
    @SerializedName("id") val id: Int,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("payer_id") val payerId: Int,
    @SerializedName("participants_ids") val participantsIds: List<Int>,
    @SerializedName("date") val date: String
)

// Transaction data class
data class Transaction(
    @SerializedName("id") val id: Int,
    @SerializedName("payer_id") val payerId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("date") val date: String
)

// Calculate response data class
data class GroupCalculationResult(
    @SerializedName("group") val group: Group,
    @SerializedName("total_spent") val totalSpent: Double,
    @SerializedName("expenses") val expenses: List<Expense>,
    @SerializedName("transactions") val transactions: List<Transaction>
)

// Add expense request
data class AddExpenseRequest(
    @SerializedName("expense") val expense: Expense,
    @SerializedName("group_id") val groupId: Int
)

// Calculate request
data class CalculateRequest(
    @SerializedName("group_id") val groupId: Int
)