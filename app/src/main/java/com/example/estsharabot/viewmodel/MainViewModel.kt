package com.example.estsharabot.viewmodel

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estsharabot.model.BotResponse
import com.example.estsharabot.model.ImageReport
import com.example.estsharabot.model.Message
import com.example.estsharabot.repo.RemoteRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class MainViewModel(private val apiRepo: RemoteRepo) : ViewModel() {

    private var text = "..."
    private val TAG = "Upload Fragment"
    private val myResponse: MutableLiveData<ImageReport> = MutableLiveData()
    private var report = ImageReport("Failed","Failed","Failed")





    suspend fun postImage(file: RequestBody , lifecycleOwner: LifecycleOwner): ImageReport {
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
                    for (i in 0 until it.percentage.length){
                        if(it.percentage[i]!= '%'){
                            temp += it.percentage[i]
                        }
                    }
                   report.percentage = temp
                    text = "MRI Uploaded Successfully"
                })
            }
        while (text == "..."){
            delay(5)
        }
        return report
    }

    suspend fun postMessage(message: Message, lifecycleOwner: LifecycleOwner): BotResponse {

        val botResponse : MutableLiveData<BotResponse> = MutableLiveData()
        val mResponse = BotResponse("Failed", "Failed")

        var text = "..."

        viewModelScope.launch {
            val response = apiRepo.postMessage(message)
            botResponse.value = response
            botResponse.observe(lifecycleOwner, Observer {
                if (it.recipent != null){
                    mResponse.recipent = it.recipent
                }else{
                    mResponse.recipent = "Done"
                }
                mResponse.text = it.text
            })
            text = "Done"
        }

        while (text == "..."){
            delay(100)
        }
        text = "..."
        return mResponse
    }


}