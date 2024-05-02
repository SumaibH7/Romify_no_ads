package com.bluell.roomdecoration.interiordesign.ui.fullscreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.ForegroundWorker
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOVarSingelton
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.customImagesModel
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentPreviewImageBinding
import com.bluell.roomdecoration.interiordesign.ui.home.GenerateInteriorViewModel
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PreviewImageFragment : Fragment() {
    private var _binding: FragmentPreviewImageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var workManager: WorkManager

    private var customImgObj: customImagesModel? = null

    private val generateInteriorViewModel: GenerateInteriorViewModel by viewModels({ requireActivity() })

    var dtoObject: DTOvariations = DTOVarSingelton.getInstance()

    private var genericModel: genericResponseModel? = null
    var image = ""

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // Check if all permissions were granted
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            initWorkManager()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.storage_permission_denied), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.shimmerViewContainer.startShimmerAnimation()
        arguments?.let {
            image = it.getString("image")!!
            Glide.with(activity?.applicationContext!!).load(image).into(binding.selectedImage)
            binding.selectedImage.scaleType= ImageView.ScaleType.CENTER_CROP
        }

        getPrevData()

        setEvents()

    }

    fun setEvents() {
        binding.downloadImage.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                } else {
                    initWorkManager()
                }
            } else {
                initWorkManager()

            }
        }


        binding.minimizeImage.setOnClickListener {
            findNavController().popBackStack()
        }



        binding.favoritesBtn.setOnClickListener {
            val oldData =
                appDatabase.genericResponseDao()?.getCreationsByIdNotLive(genericModel?.id!!)
            if (oldData != null) {
                if (oldData.isFavorite == true) {
                    oldData.isFavorite = false
                    appDatabase.genericResponseDao()?.UpdateData(oldData)

                    binding.favorites.setImageResource(R.drawable.favorite_light)
                    binding.favorites.setColorFilter(Color.parseColor("#999999"))
                } else {
                    oldData.isFavorite = true
                    appDatabase.genericResponseDao()?.UpdateData(oldData)

                    binding.favorites.setImageResource(R.drawable.favorite_filled)
                    binding.favorites.setColorFilter(Color.parseColor("#E04F5F"))
                }

            }

        }

        binding.variations.setOnClickListener {
            dtoObject.token = Constants.FIREBASE_TOKEN
            dtoObject.init_image = image
            dtoObject.enhance_prompt = "yes"
            generateInteriorViewModel.generateVariations(
                dtoObject = dtoObject
            )
        }

        binding.shareImg.setOnClickListener {
            downloadAndShareImageWithGlide(image)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.editImage.setOnClickListener {
            Bundle().apply {
                putString("type", Constants.EDIT_END_POINT)
                putString("init_image", image)
                findNavController().navigate(R.id.maskImageFragment, this)
            }


        }
    }

    private fun getPrevData() {
        arguments?.let {
            val id = it.getString("id")

            genericModel = appDatabase.genericResponseDao()?.getCreationsByIdNotLive(id!!.toInt())
            if (genericModel != null) {
                initData()
                initObservers(genericModel?.id!!.toInt())
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "This art has been deleted by user!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

        }

    }


    private fun initData() {
        if (genericModel?.output != null && genericModel!!.output!!.isNotEmpty()) {
            customImgObj = customImagesModel(
                genericModel!!.output!![0],
                genericModel!!.id!!,
                genericModel!!.meta,
                genericModel!!.status,
                genericModel?.eta,
                genericModel?.message,
                false
            )
        }
        if (genericModel != null) {
            if (genericModel?.isFavorite == true) {
                binding.favorites.setImageResource(R.drawable.favorite_filled)
                binding.favorites.setColorFilter(Color.parseColor("#E04F5F"))
            } else {
                binding.favorites.setImageResource(R.drawable.favorite_light)
                binding.favorites.setColorFilter(Color.parseColor("#999999"))
            }
            binding.prompt.text = genericModel!!.meta.prompt
        }
    }

    private fun initObservers(id: Int) {

        lifecycleScope.launch {
            appDatabase.genericResponseDao()?.getCreationsById(id)?.observe(viewLifecycleOwner) {
                it?.let {
                    genericModel = it
                    Log.e("TAG", "imagesFromDB: $it")
                    initData()
                }
            }
        }

        lifecycleScope.launch {
            var processing = false
            generateInteriorViewModel.Imagevariations.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        processing = true
                        customProgressBar.show(requireContext(), "Generating Variations")
                        Log.e("TAG", "initobservers: loading")
                    }

                    is Response.Success -> {

                        val imageToImageResponse = result.data

                        if (imageToImageResponse != null && imageToImageResponse.output!!.isNotEmpty()) {

                            imageToImageResponse.meta.init_image = dtoObject.init_image
                            imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
//                            imageToImageResponse.endpoint = genericModel?.endpoint
                            imageToImageResponse.endpoint = Constants.INSPIRE_END_POINT
                            appDatabase.genericResponseDao()?.saveData(
                                imageToImageResponse
                            )
                            generateInteriorViewModel.clearImageVariations()
                            Constants.clear()
                            customProgressBar.dismiss()
                            Bundle().apply {
                                putString("art", Gson().toJson(imageToImageResponse))
                                putString("id", imageToImageResponse.id.toString())

                                findNavController().navigate(
                                    R.id.action_fragmentB_to_fragmentA,
                                    this
                                )
                            }
                        }else{
                            if (processing){
                                customProgressBar.dismiss()
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    "Project processing in progress! Please check back later!",
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }

                    is Response.Processing -> {
                        customProgressBar.dismiss()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "The Image is in processing It will be generated in a few seconds.",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }

                    is Response.Error -> {
                        generateInteriorViewModel.clearImageToImage()
                        Constants.clear()
                        customProgressBar.dismiss()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        Log.e("TAG", "initObservers: ${result.message}")
                    }
                }
            }
        }
    }

    private fun initWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(false)
            .setRequiresBatteryNotLow(false)
            .build()
        val data = Data.Builder()

        data.apply {
            putString(Constants.FILE_TYPE, "image")
            putString("url", Gson().toJson(listOf(image ?: "")))
        }
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(ForegroundWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .addTag(UUID.randomUUID().toString())
                .build()

        workManager.enqueue(oneTimeWorkRequest)
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Image download started!",
            Snackbar.LENGTH_LONG
        )
            .show()
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
                if (workInfo != null) {

                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Image download completed!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }

                        WorkInfo.State.RUNNING -> {
                            Log.e("TAG", "initObservers:running buddy")
                        }

                        WorkInfo.State.FAILED -> {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "An error occurred! Please try again later!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            val outputData = workInfo.outputData
                            Log.e(
                                "TAG",
                                "error in fragment ${outputData.getString("error")}",
                            )
                        }

                        else -> {}
                    }
                }
            }
    }

    private fun downloadAndShareImageWithGlide(imageUrl: String) {

        val imageName = "${System.currentTimeMillis()}.png"
        val directory = File(requireContext().cacheDir, "shared_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, imageName)


        Glide.with(activity?.applicationContext!!)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    try {
                        // Save the bitmap to a file
                        val outputStream = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()

                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_STREAM,
                                FileProvider.getUriForFile(
                                    requireContext(),
                                    "${requireContext().packageName}.fileprovider",
                                    file
                                )
                            )
                            type = "image/*"
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        if (isAdded) {
                            requireContext().startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share Image"
                                )
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not implemented
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}