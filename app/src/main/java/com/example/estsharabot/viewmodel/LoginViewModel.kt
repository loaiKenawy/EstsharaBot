package com.example.estsharabot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.estsharabot.utility.DataStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreHelper = DataStoreHelper(application)


    val email = dataStoreHelper.getUserEmail().asLiveData(Dispatchers.IO)
    val password = dataStoreHelper.getUserPassword().asLiveData(Dispatchers.IO)

    fun saveUserData(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreHelper.setUserData(email, password)
        }
    }

}