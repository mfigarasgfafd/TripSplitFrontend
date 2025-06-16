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
    suspend fun createGroup(
        @Body group: GroupRequest,
        @Header("todo_apikey") apiKey: String
    ): Response<Void>

    @GET("group/get_groups")
    suspend fun getGroups(
        @Header("todo_apikey") apiKey: String
    ): Response<List<Group>>

    // User endpoints
    @GET("user/get_users")
    suspend fun getUsers(
        @Header("todo_apikey") apiKey: String
    ): Response<List<User>>



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
}