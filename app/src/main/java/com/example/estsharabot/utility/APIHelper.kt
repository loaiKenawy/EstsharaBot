package com.example.estsharabot.utility

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class APIHelper {
    companion object {

        fun getAPI(TAG: String, activity: FragmentActivity, key: String): String {
            var url = ""
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            activity.let {
                remoteConfig.fetchAndActivate()
                    .addOnCompleteListener(it) { task ->
                        if (task.isSuccessful) {
                            val updated = task.result
                            Log.d(TAG, "Config params updated: $updated")
                            url = Firebase.remoteConfig.getString(key)
                            Log.d(TAG, url)
                        } else {
                            Toast.makeText(
                                activity,
                                "Please check your internet connection",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
            return url
        }

    }

}