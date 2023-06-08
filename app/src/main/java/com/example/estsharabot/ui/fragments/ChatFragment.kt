@file:OptIn(DelicateCoroutinesApi::class)

package com.example.estsharabot.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.estsharabot.R
import com.example.estsharabot.adapters.LiveChatAdapter
import com.example.estsharabot.databinding.FragmentChatBinding
import com.example.estsharabot.model.Message
import com.example.estsharabot.remote.Service
import com.example.estsharabot.repo.RemoteRepo
import com.example.estsharabot.utility.URIPathHelper
import com.example.estsharabot.viewmodel.MainViewModel
import com.example.estsharabot.viewmodel.MainViewModelFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import java.io.File


class ChatFragment : Fragment() {

    private val TAG = "CHAT_FRAGMENT"

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var uri: Uri
    private val pickImage = 100


    private val storage = Firebase.storage("gs://estsharabot.appspot.com")
    var storageRef = storage.reference


    private var mList = ArrayList<Message>()

    private lateinit var messagesAdapter: LiveChatAdapter
    private lateinit var messageRecyclerView: RecyclerView

    private lateinit var viewModel: MainViewModel

    private var errorFlag = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root


        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)


        // GlobalScope.launch(Dispatchers.Main) {
        //     sendMessage(Message("Hi", true))
        //     binding.etMessage.isFocusable = false
        // }

        // binding.btnSend.setOnClickListener {
        //     if (!errorFlag) {
        //         if (binding.etMessage.text.isNotEmpty()) {
        //             send(Message(binding.etMessage.text.toString(), true))
        //         }
        //     }
        // }

        return view
    }


    private fun send(message: Message) {
        GlobalScope.launch(Dispatchers.Main) {
            sendMessage(message)
        }

        mList.add(message)
        binding.etMessage.text.clear()
        binding.etMessage.hint = "Type a message"

        messagesAdapter.notifyItemInserted(mList.size - 1)
        messageRecyclerView.smoothScrollToPosition(mList.size - 1)
    }


    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        messagesAdapter = context?.let { LiveChatAdapter(it, mList) }!!
        messageRecyclerView = binding.rvMessages
        messageRecyclerView.adapter = messagesAdapter
        messageRecyclerView.layoutManager = layoutManager
        messageRecyclerView.smoothScrollToPosition(mList.size - 1)
    }


    private suspend fun sendMessage(message: Message) {
        try {
            viewModel = ViewModelProvider(
                this, MainViewModelFactory(RemoteRepo(Service.getInstance()!!))
            )[MainViewModel::class.java]
            // Start Post process
            try {
                val text = viewModel.postMessage(message, viewLifecycleOwner).text

                Log.d(TAG, "response : $text")
                if (text == "Failed") {
                    mList.add(Message(getString(R.string.ChatBotError), false))
                    errorFlag = true

                } else {
                    mList.add(Message(text, false))
                    binding.etMessage.isFocusable = true
                }
                initRecyclerView()
                //Add message to view
                messagesAdapter.notifyItemInserted(mList.size - 1)
                messageRecyclerView.smoothScrollToPosition(mList.size - 1)

            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e(TAG, "Uploading Failed")
                Log.d(TAG, ex.toString())
                withContext(Dispatchers.Main) {
                    mList.add(Message(getString(R.string.ChatBotError), false))
                    messagesAdapter.notifyItemInserted(mList.size - 1)
                    messageRecyclerView.smoothScrollToPosition(mList.size - 1)

                    initRecyclerView()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get instance: ${e.message}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Connection error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkUploadImage(message: String) {
        if (message == "Can you please upload the x-ray image here") {

        }

    }

    private fun uploadImage(uri: Uri) {
        try {
            val path = URIPathHelper().getPath(requireActivity(), uri)

            val file = Uri.fromFile(File(path!!))
            val riversRef = storageRef.child("images/${file.lastPathSegment}")
            val uploadTask = riversRef.putFile(file)

            uploadTask.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Firebase Failed to upload Image ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(context, "Success ${taskSnapshot.totalByteCount}", Toast.LENGTH_LONG)
                    .show()
                Log.i(TAG, riversRef.downloadUrl.toString())
                storageRef.child("images/${file.lastPathSegment}").downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "Firebase : URL $it")
                }.addOnFailureListener {
                    Log.e(TAG, "Firebase Exception : Failed to get URL")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Firebase Exception : ${e.message}")
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            uri = data?.data!!
            uploadImage(uri)
        } else {
            Toast.makeText(context, "Task Failed", Toast.LENGTH_LONG).show()
        }
    }


}


/*
    private val permissionLauncherMultiple = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    )
    { result ->
        var areAllGranted = true
        for (isGranted in result.values) {
            Log.d(TAG, "permissionLauncherMultiple: isGranted: $isGranted")
            areAllGranted = areAllGranted && isGranted
        }
        if (areAllGranted) {
            try {
                getContent.launch("image/*")
            } catch (e: Exception) {
                Log.e("PhotoPicker", "Error ${e.message}")
            }
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_LONG).show()
        }
    }


private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                uploadImage(uri)
            } else {
                Toast.makeText(context, "Error while selecting the image", Toast.LENGTH_LONG).show()
            }
        }

* */