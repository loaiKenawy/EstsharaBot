package com.example.estsharabot.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.estsharabot.R
import com.example.estsharabot.databinding.FragmentLoginBinding
import com.example.estsharabot.utility.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {

    private val TAG = "LoginFragment"

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        val view = binding.root

        binding.btnLogin.setOnClickListener {
            val email = binding.inEmail.text?.toString()
            val password = binding.inPassword.text?.toString()
            Log.d(TAG, "Email $email \n Password $password")
            if (!(email.isNullOrEmpty()) && !(password.isNullOrEmpty())) {
                login(email, password)


            } else {
                Log.d(TAG, "Null values")
            }
        }

        binding.btnRegister.setOnClickListener {
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        return view
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
                            Constants.userId = user.uid
                            view?.findNavController()?.navigate(R.id.action_loginFragment_to_homeFragment)

                        }
                    } else {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}