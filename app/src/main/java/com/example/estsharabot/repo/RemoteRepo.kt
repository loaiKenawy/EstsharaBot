package com.example.estsharabot.repo

import android.util.Log
import com.example.estsharabot.model.BotResponse
import com.example.estsharabot.model.ImageReport
import com.example.estsharabot.model.Message
import com.example.estsharabot.model.User
import com.example.estsharabot.remote.ApiMethods
import com.example.estsharabot.utility.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.RequestBody


class RemoteRepo(private val service: ApiMethods? = null) {

    private val TAG = "RepositoryPosting"

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    suspend fun postFrame(file: RequestBody): ImageReport {
        Log.d(TAG, "Repo Posting")
        return try {
            val response = service!!.postImage(file)
            response
        } catch (e: Exception) {
            Log.e(TAG, "Repo Post failed -> ${e.message}")
            val report = ImageReport("Failed", "Failed", "Failed")
            report
        }
    }

    suspend fun postMessage(message: Message): BotResponse {

        return try {
            val res = service!!.postMessage(message)
            Log.d(TAG, "sent: ${message.message}")
            Log.d(TAG, "receive: ${res[0].text}")
            res[0]
        } catch (e: Exception) {
            Log.i(TAG, "URL : ${Constants.BASE_URL}")
            Log.e(TAG, "Can't Post message : ${e.message}")
            BotResponse("Failed", "Failed")
        }
    }

    fun registerNewUser(newUser: User, email: String, password: String): Int {
        var successFlag = -1
        try {
            auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    database = Firebase.database.reference
                    database.child("users").child(user!!.uid).setValue(newUser)
                    Log.d(TAG, "New User Registered Successfully")
                    successFlag = Constants.SUCCESS_VALUE

                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    successFlag = Constants.FAILED_VALUE
                }

            }

        } catch (e: Exception) {
            Log.e(TAG, "RTDB : Can't register new user : ${e.message}")
            successFlag = Constants.FAILED_VALUE
        }
        return successFlag
    }
}