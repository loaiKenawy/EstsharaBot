package com.example.estsharabot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estsharabot.model.User
import com.example.estsharabot.repo.RemoteRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RegisterViewModel(private val repo: RemoteRepo) : ViewModel() {

    private var loadingFlag = -1

    suspend fun registerNewUser(newUser: User , email:String ,password:String) :Int {
        viewModelScope.launch {
           loadingFlag =  repo.registerNewUser(newUser,email,password)
        }
        while (loadingFlag == -1){
            delay(10)
        }
        return loadingFlag
    }
}