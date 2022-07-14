package com.gravity.loft.safarkasathi.services

import com.gravity.loft.safarkasathi.models.Profile
import com.gravity.loft.safarkasathi.models.Token
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface LoginService {

    @POST("/api/api/drivers/register_mobile")
    suspend fun registerMobile(@Body map: Map<String, String>): Response<String>

    @POST("/api/api/drivers/validate_otp")
    suspend fun validateOtp(@Body map: Map<String, String>): Response<Token>

    /**
     * This api called when we try to update user name and recordTrip
     * This api called after validating otp
     * On success call we save access token to the SharedPreferences
     */
    @PUT("/api/api/drivers/update_details")
    suspend fun updateDetails(@Body map: Map<String, String>): Response<String>

    @GET("/api/api/drivers/profile")
    suspend fun getProfile(): Response<Profile>
}