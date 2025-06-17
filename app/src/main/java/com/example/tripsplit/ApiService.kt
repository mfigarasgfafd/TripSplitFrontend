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


    @GET("group/get_groups")
    suspend fun getGroups(
        @Header("todo_apikey") apiKey: String
    ): Response<List<Group>>

    // User endpoints
    @GET("user/get_users")
    suspend fun getUsers(
        @Header("todo_apikey") apiKey: String
    ): Response<List<User>>

    @GET("group/get_group/{id}")
    suspend fun getGroupDetails(
        @Header("todo_apikey") apiKey: String,
        @Path("id") id: Int
    ): Response<Group>

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

//    // New endpoints for groups
//    @GET("user/get_users")
//    suspend fun getUsers(): Response<List<User>>
//
//    @POST("group/create_group")
//    suspend fun createGroup(@Body group: GroupRequest): Response<Void>
//
//    // Add group endpoints
//    @GET("group/get_groups")
//    suspend fun getAllGroups(): Response<List<Group>>
//
//    @GET("group/get_group/{id}")
//    suspend fun getGroup(@Path("id") id: Int): Response<Group>
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