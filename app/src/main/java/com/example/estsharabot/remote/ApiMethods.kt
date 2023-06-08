package com.example.estsharabot.remote

import com.example.estsharabot.model.BotResponse
import com.example.estsharabot.model.ImageReport
import com.example.estsharabot.model.Message
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface ApiMethods {


    @Streaming
    @POST("/post")
    suspend fun postImage(
        @Body requestBody: RequestBody
    ):ImageReport

    @Streaming
    @POST("/webhooks/rest/webhook")
    suspend fun postMessage(
         @Body message: Message
    ) : Array<BotResponse>
}