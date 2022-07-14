package com.gravity.loft.safarkasathi.commons

import com.google.gson.GsonBuilder
import com.gravity.loft.safarkasathi.MainApp
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.exceptions.AuthTokenException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {

    private val gson = GsonBuilder().setLenient().create()

    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl(MainApp.getString(R.string.API_BASE_URL))
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())

    fun <T> buildService(service: Class<T>): T{
        return retrofitBuilder.client(OkHttpClient()).build().create(service)
    }

    fun <T> buildAuthService(service: Class<T>): T {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addInterceptor {
            val token = Settings.getBearerToken()
            if(token != null){
                val request = it.request().newBuilder().addHeader("Authorization", token).build()
                it.proceed(request)
            }else{
                throw AuthTokenException()
            }
        }
        return retrofitBuilder.client(clientBuilder.build()).build().create(service)
    }

}