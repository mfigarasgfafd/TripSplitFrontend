package com.example.tripsplit

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("create_user")
    suspend fun registerUser(@Body user: UserRegistration): Response<ApiResponse>
}