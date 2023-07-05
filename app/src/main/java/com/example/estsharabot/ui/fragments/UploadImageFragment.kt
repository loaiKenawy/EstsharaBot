package com.example.estsharabot.ui.fragments

import android.app.Activity.RESULT_OK
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
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.estsharabot.databinding.FragmentUploadImageBinding
import com.example.estsharabot.remote.Service
import com.example.estsharabot.repo.RemoteRepo
import com.example.estsharabot.utility.Constants
import com.example.estsharabot.utility.URIPathHelper
import com.example.estsharabot.viewmodel.MainViewModel
import com.example.estsharabot.viewmodel.MainViewModelFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@OptIn(DelicateCoroutinesApi::class)
class UploadImageFragment : Fragment() {

    private val TAG = "Upload Fragment"
    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var uri: Uri
    private val pickImage = 100

    private lateinit var viewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getAPI()
        _binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        val view = binding.root



        binding.btnUpload.setOnClickListener {
            Constants.BASE_URL = getAPI()

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
        return view
    }

    private fun getAPI(): String {

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

        val url = Firebase.remoteConfig.getString(Constants.IMAGE_UPLOAD_KEY)
        Log.d(TAG, url)
        return url
    }

    private suspend fun uploadFile(uri: Uri) {

        Constants.BASE_URL = getAPI()


        try {
            viewModel = ViewModelProvider(
                this,
                MainViewModelFactory(RemoteRepo(Service.getInstance()!!))
            )[MainViewModel::class.java]

            val path = URIPathHelper().getPath(requireActivity(), uri)
            Log.d("RepositoryPosting", "String from fragment" + path.toString())
            val imageFile = File(path!!)

            try {

                val imageRequestFile: RequestBody =
                    imageFile.asRequestBody("image/*".toMediaTypeOrNull())

                val requestBody: RequestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("Frame", "Frame", imageRequestFile)
                    .build()

                val result = viewModel.postImage(requestBody, viewLifecycleOwner)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (result.organ != "Failed") {
                        Toast.makeText(context, Constants.UPLOAD_DONE, Toast.LENGTH_SHORT).show()
                        binding.tvValueOrgan.text = result.organ
                        binding.tvValueType.text = result.disease
                        binding.pbRisk.progress = Integer.parseInt(result.percentage)
                        val percentageTemp = result.percentage + "%"
                        binding.tvRisk.text = percentageTemp
                    } else {
                        Toast.makeText(context, Constants.ERROR_TOAST, Toast.LENGTH_SHORT).show()
                    }

                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.d(TAG, " Uploading Failed -> ${ex.message}")
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "Error in path ${e.message}")
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Connection error", Toast.LENGTH_LONG).show()
            }

        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.progressBar.visibility = View.VISIBLE
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            uri = data?.data!!
            binding.btnUpload.visibility = View.INVISIBLE
            binding.ivImageContainer.load(uri) {
                crossfade(true)
                crossfade(100)
                transformations(RoundedCornersTransformation(50f))
            }
            try {

                GlobalScope.launch(Dispatchers.IO) {
                    uploadFile(uri)
                }
            } catch (ex: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error while selecting the image", Toast.LENGTH_LONG).show()
                Log.d(TAG, ex.toString())
            }

        } else {
            binding.progressBar.visibility = View.INVISIBLE
            Toast.makeText(context, "Task Failed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Constants.BASE_URL = getAPI()
        _binding = null
    }

}
