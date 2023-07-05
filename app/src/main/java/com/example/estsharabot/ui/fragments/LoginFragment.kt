package com.example.estsharabot.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.estsharabot.R
import com.example.estsharabot.databinding.FragmentLoginBinding
import com.example.estsharabot.utility.Constants
import com.example.estsharabot.utility.DataStoreHelper
import com.example.estsharabot.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@DelicateCoroutinesApi
class LoginFragment : Fragment() {

    private val TAG = "LoginFragment"

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private lateinit var dataStore: DataStoreHelper
    private lateinit var viewModel: LoginViewModel

    private lateinit var savedEmail: String
    private lateinit var savedPassword: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        val view = binding.root
        controlProgressBar(true)
        dataStore = DataStoreHelper(requireContext())

        viewModel = LoginViewModel(requireActivity().application)

        viewModel.email.observe(requireActivity()) { email ->
            savedEmail = if (email == "N/A" || email.isNullOrEmpty()) {
                Log.d(TAG, "N/A")
                email
            } else {
                Log.d(TAG, email)
                email
            }
        }
        viewModel.password.observe(requireActivity()) { password ->
            savedPassword = if (password == "N/A" || password.isNullOrEmpty()) {
                Log.d(TAG, "N/A")
                password
            } else {
                Log.d(TAG, password)
                password
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            checkedSavedUser()
        }

        //checkedSavedUser()
        return view
    }


    private suspend fun checkedSavedUser() {
        if (this::savedEmail.isInitialized && this::savedPassword.isInitialized) {
            if (savedEmail != "N/A" && savedPassword != "N/A") {
                Log.d(TAG, "email and password aren't Null")
                login(savedEmail, savedPassword)
            } else {
                controlProgressBar(false)
                Log.d(TAG, "email and password are Null")
                binding.btnLogin.setOnClickListener {
                    controlProgressBar(true)
                    val email = binding.inEmail.text?.toString()
                    val password = binding.inPassword.text?.toString()
                    Log.d(TAG, "Email $email \n Password $password")
                    if (!(email.isNullOrEmpty()) && !(password.isNullOrEmpty())) {
                        login(email, password)
                        if (binding.cbRememberMe.isChecked) {
                            GlobalScope.launch(Dispatchers.IO) {
                                viewModel.saveUserData(email, password)
                            }
                        }
                    } else {
                        Log.d(TAG, "Null values")
                        controlProgressBar(false)
                    }
                }

                binding.btnRegister.setOnClickListener {
                    view?.findNavController()
                        ?.navigate(R.id.action_loginFragment_to_registerFragment)
                }
            }
        } else {
            delay(50)
            Log.d(TAG, "isNotInitialized")
            checkedSavedUser()
        }
    }

    private fun login(email: String, password: String) {
        activity?.let {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        Log.d(TAG, "user  ${user?.uid}")
                        if (user != null) {
                            controlProgressBar(false)
                            Constants.userId = user.uid
                            view?.findNavController()
                                ?.navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                    } else {
                        controlProgressBar(false)
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            activity, "Email or Password may be incorrect",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun controlProgressBar(switch:Boolean){
        if (switch){
            binding.progressBar.visibility = View.VISIBLE
            binding.etEmail.visibility = View.GONE
            binding.etPassword.visibility = View.GONE
            binding.cbRememberMe.visibility = View.GONE
            binding.btnLogin.visibility = View.GONE
            binding.btnRegister.visibility = View.GONE
        }else{
            binding.progressBar.visibility = View.GONE
            binding.etEmail.visibility = View.VISIBLE
            binding.etPassword.visibility = View.VISIBLE
            binding.cbRememberMe.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE

        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}