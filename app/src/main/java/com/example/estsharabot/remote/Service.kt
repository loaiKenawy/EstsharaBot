package com.example.estsharabot.remote

import android.util.Log
import com.example.estsharabot.utility.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class Service {

    companion object {

        var methods: ApiMethods? = null

        private val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        fun getInstance() : ApiMethods? {
            if (methods == null) {
                val baseURL = Constants.BASE_URL
                Log.d("Remote Service", baseURL)
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
                methods = retrofit.create(ApiMethods::class.java)
            }
            Log.d("API METHODS", "Instance created")
            return methods
        }
    }
}