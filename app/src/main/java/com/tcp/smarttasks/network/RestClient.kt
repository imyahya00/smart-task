package com.tcp.smarttasks.network

import android.content.Context
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestClient {

    private const val BASE_URL = "http://demo1414406.mockable.io/"
    private var service: ApiService? = null

    fun getInstance(): ApiService {
        service = getService()
        return service!!
    }

    private fun getService(): ApiService {
        if (service != null) {
            service = null
        }

        val builder = OkHttpClient.Builder()
        builder.readTimeout(50, TimeUnit.SECONDS)
        builder.connectTimeout(50, TimeUnit.SECONDS)
        builder.writeTimeout(50, TimeUnit.SECONDS)

        builder.addInterceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }

        val retrofit = Retrofit.Builder()
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .baseUrl(BASE_URL).build()
        service = retrofit.create(ApiService::class.java)
        return service!!
    }
}
