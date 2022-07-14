package com.gravity.loft.safarkasathi.services

import com.gravity.loft.safarkasathi.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TripService {

    /**
     *  @GET("/temp")
    suspend fun getFake(@Query("key") key: String): Response<List<Trip>>
     */

    @Headers(value = ["Accept: application/json"])
    @GET("/api/api/Trip/cmt_trip")
    suspend fun getCmtTrips(): Response<List<CmtTrip>>

    @Headers(value = ["Accept: application/json"])
    @GET("/api/api/trip/index_ongoing")
    suspend fun getTrips(): Response<List<Trip>>

    @POST("/api/api/Trip/unlock_trip")
    @JvmSuppressWildcards
    suspend fun unlockTrip(@Body map: Map<String, Any>): Response<String>

    @POST("/api/api/Trip/action_trip")
    @JvmSuppressWildcards
    suspend fun unlockTripAction(@Body map: Map<String, Any>): Response<String>

    @Multipart
    @POST("/api/api/drivers/check_driverlicense")
    @JvmSuppressWildcards
    suspend fun uploadDocumentsPark(@Part("aadharno") aadharno: RequestBody,
                                    @Part("lincenseno") lincenseno: RequestBody,
                                    @Part("image") image: MultipartBody.Part,
                                    @Part("image1") image1: MultipartBody.Part,): Response<String>

    @Multipart
    @POST("/api/api/drivers/check_driverlicense")
    @JvmSuppressWildcards
    suspend fun uploadDocuments(@PartMap params: Map<String, RequestBody>): Response<DriverDocument>

    @GET("/api/api/drivers/check_dlicense")
    @JvmSuppressWildcards
    suspend fun checkAadhar(): Response<DriverDocument>

    @POST("/api/api/Trip/cmt_task")
    @JvmSuppressWildcards
    suspend fun getCmtTripTask(@Body map: Map<String, Any>): Response<List<CmtTask>>

    @Multipart
    @POST("/api/api/Trip/upload_task_image")
    @JvmSuppressWildcards
    suspend fun uploadTaskDocs(@PartMap params: Map<String, RequestBody>): Response<UploadTask>

}