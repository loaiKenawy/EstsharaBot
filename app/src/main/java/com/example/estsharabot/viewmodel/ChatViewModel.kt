package com.example.estsharabot.viewmodel


import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.example.estsharabot.model.BotResponse
import com.example.estsharabot.model.ImageReport
import com.example.estsharabot.model.Message
import com.example.estsharabot.repo.RemoteRepo
import com.example.estsharabot.utility.Constants
import com.example.estsharabot.utility.URIPathHelper
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.File

@Suppress("SENSELESS_COMPARISON")
class ChatViewModel(private val apiRepo: RemoteRepo) : ViewModel() {

    private val TAG = "Chat ViewModel"

    private lateinit var botResponse: BotResponse

    private var loadingFlag = Constants.loading
    private val storage = Firebase.storage("gs://estsharabot.appspot.com")
    private var storageRef = storage.reference

    private val myResponse: MutableLiveData<ImageReport> = MutableLiveData()

    private var report = ImageReport("Failed", "Failed", "Failed")

    fun getAPI(activity: FragmentActivity, key: String) {

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        activity?.let {
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        Log.d(TAG, "Config params updated: $updated")

                    } else {
                        try {
                            Toast.makeText(
                                activity,
                                "Please check your internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Toast Problem : ${e.message}")
                        }

                    }
                }
        }

        val url = Firebase.remoteConfig.getString(key)
        Log.d(TAG, url)
        Constants.BASE_URL = url
    }

    suspend fun postImage(
        file: RequestBody,
        uri: Uri,
        context: Context,
        fragment: Fragment,
        lifecycleOwner: LifecycleOwner
    ): ImageReport {

        viewModelScope.launch {
            Log.d(TAG, "ViewModel")
            val response = apiRepo.postFrame(file)
            myResponse.value = response
            myResponse.observe(lifecycleOwner, Observer {
                Log.d(TAG, it.organ)
                Log.d(TAG, it.disease)
                Log.d(TAG, it.percentage)
                report.organ = it.organ
                report.disease = it.disease
                var temp = ""
                for (i in 0 until it.percentage.length) {
                    if (it.percentage[i] != '%') {
                        temp += it.percentage[i]
                    }
                }
                report.percentage = temp
                loadingFlag = "a7a"
            })
        }
        while (loadingFlag == "...") {
            delay(5)
        }
        if (report.organ != "Failed") {
            try {

                val path = URIPathHelper().getPath(fragment.requireActivity(), uri)
                val imageFile = Uri.fromFile(File(path!!))
                val riversRef = storageRef.child("${Constants.userId}/${imageFile.lastPathSegment}")
                val uploadTask = riversRef.putFile(imageFile)

                uploadTask.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Firebase Failed to upload Image ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }.addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(
                        context,
                        "Success ${taskSnapshot.totalByteCount}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    Log.i(TAG, riversRef.downloadUrl.toString())
                    storageRef.child("images/${imageFile.lastPathSegment}").downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "Firebase : URL $it")
                        report = ImageReport(
                            report.organ,
                            report.disease,
                            report.percentage,
                            it.toString()
                        )
                    }.addOnFailureListener {
                        Log.e(TAG, "Firebase Exception : Failed to get URL")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Firebase Exception : ${e.message}")
                return ImageReport("Failed", "Failed", "Failed")
            }
        }
        return report
    }


    suspend fun postMessage(message: Message): BotResponse {

        loadingFlag = Constants.loading

        try {
            viewModelScope.launch {
                val response = apiRepo.postMessage(message)
                if (response.recipient_id != null) {
                    Log.d(TAG, "it is not null")
                    try {
                        Log.d(TAG, "Response : " + response.text)
                        botResponse =  response
                        loadingFlag = Constants.loaded

                    } catch (e: Exception) {
                        botResponse = BotResponse("Failed" , "Failed")
                        loadingFlag = Constants.loaded
                        Log.d(TAG, "Exception : ${e.message}")
                    }
                }

            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception : ${ex.message}")
        }
        while (loadingFlag == Constants.loading) {
            delay(1)
        }
        return botResponse
    }
}