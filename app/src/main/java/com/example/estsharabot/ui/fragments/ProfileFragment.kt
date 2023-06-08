package com.example.estsharabot.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.estsharabot.databinding.FragmentProfileBinding
import com.example.estsharabot.model.User
import com.example.estsharabot.utility.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private var user: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        database = Firebase.database.reference

        getProfile()

        return view
    }

    private fun getProfile() {
        database.child("users").child(Constants.userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)!!
                    displayProfile()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase profile", "Error getting data ${error.message}")
                }
            })
    }

    private fun displayProfile() {
        val cUser = Firebase.auth.currentUser
        binding.tvEmail.text = cUser?.email
        binding.tvUsername.text = user?.fullName
        binding.tvAge.text = user?.age.toString()
        binding.tvPhone.text = user?.phone
        binding.tvGander.text = if (user?.gander == 1) {
            "Male"
        } else {
            "Female"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}