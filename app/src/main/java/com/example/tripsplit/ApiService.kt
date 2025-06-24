package com.example.tripsplit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/register")
    @Headers("Accept: text/plain")
    suspend fun register(@Body user: AuthRequest): Response<ResponseBody>

    @POST("auth/login")
    @Headers("Accept: text/plain")
    suspend fun login(@Body request: AuthRequest): Response<ResponseBody>

    // Group endpoints
    @POST("group/create_group")
    @Headers("Accept: text/plain")
    suspend fun createGroup(
        @Header("todo_apikey") apiKey: String,
        @Body request: CreateGroupRequest
    ): Response<ResponseBody>


    // User endpoints
    @GET("user/get_users")
    suspend fun getUsers(
        @Header("todo_apikey") apiKey: String
    ): Response<List<User>>


    @POST("group/get_user_groups")
    suspend fun getUserGroups(
        @Header("todo_apikey") apiKey: String
    ): Response<List<Group>>

    @POST("group/join_group")
    @Headers("Accept: text/plain")
    suspend fun joinGroup(
        @Header("todo_apikey") apiKey: String,
        @Body request: JoinGroupRequest
    ): Response<ResponseBody>

@POST("group/add_expense")
@Headers("Accept: text/plain")
suspend fun addExpense(
    @Header("todo_apikey") apiKey: String,
    @Body request: AddExpenseRequest
): Response<ResponseBody>

    @POST("group/calculate")
    suspend fun calculateGroup(
        @Header("todo_apikey") apiKey: String,
        @Body request: CalculateRequest
    ): Response<GroupCalculationResult>

}