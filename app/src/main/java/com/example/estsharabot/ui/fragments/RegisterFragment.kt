@file:OptIn(DelicateCoroutinesApi::class)

package com.example.estsharabot.ui.fragments

import android.media.audiofx.AudioEffect.Descriptor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.estsharabot.R
import com.example.estsharabot.databinding.FragmentLoginBinding
import com.example.estsharabot.databinding.FragmentRegisterBinding
import com.example.estsharabot.model.User
import com.example.estsharabot.remote.Service
import com.example.estsharabot.repo.RemoteRepo
import com.example.estsharabot.viewmodel.MainViewModel
import com.example.estsharabot.viewmodel.MainViewModelFactory
import com.example.estsharabot.viewmodel.RegisterViewModel
import com.example.estsharabot.viewmodel.RegisterViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {
    private val TAG = "RegisterFragment"
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.progressBar.visibility = View.GONE

        binding.btnRegister.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE

            val name = binding.inName.text?.toString()
            val email = binding.inEmail.text?.toString()
            val password = binding.inPassword.text?.toString()
            val phone = binding.inPhone.text?.toString()
            val age = binding.inAge.text?.toString()

            val gander = if (binding.inMale.isChecked) {
                1
            } else {
                0
            }
            Log.d(TAG, "Email $email \n Password $password")
            if (email != null && password != null) {
                val newUser =
                    User(name!!,  phone!!, Integer.parseInt(age!!), gander)
                viewModel = ViewModelProvider(
                    this,
                    RegisterViewModelFactory(RemoteRepo())
                )[RegisterViewModel::class.java]
                GlobalScope.launch(Dispatchers.IO) {
                    val res = viewModel.registerNewUser(newUser,email,password)
                    Log.d(TAG, "Result : $res")
                }
                binding.progressBar.visibility = View.GONE
                Toast.makeText(activity,"Successfully Registered",Toast.LENGTH_SHORT).show()
                view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } else {
                Log.d(TAG, "Null values")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(activity,"Registration Failed",Toast.LENGTH_SHORT).show()
            }

        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}