package com.example.tripsplit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("user/create_user")
    suspend fun createUser(@Body user: UserRegistration): Response<ResponseBody>

    // New endpoints for groups
    @GET("user/get_users")
    suspend fun getUsers(): Response<List<User>>

    @POST("group/create_group")
    suspend fun createGroup(@Body group: GroupRequest): Response<Void>

    // Add group endpoints
    @GET("group/get_groups")
    suspend fun getAllGroups(): Response<List<Group>>

    @GET("group/get_group/{id}")
    suspend fun getGroup(@Path("id") id: Int): Response<Group>
}