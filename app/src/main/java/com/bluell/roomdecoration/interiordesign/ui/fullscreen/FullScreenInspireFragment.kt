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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.bluell.roomdecoration.interiordesign.ImageLoadingHelper
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.ForegroundWorker
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOVarSingelton
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.UpscaleResponseDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.customImagesModel
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentFullScreenInspireBinding
import com.bluell.roomdecoration.interiordesign.ui.home.GenerateInteriorViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class FullScreenInspireFragment : Fragment() {

    private var _binding: FragmentFullScreenInspireBinding? = null
    private val binding get() = _binding!!

    private var genericModel: genericResponseModel? = null

    var customImgObj: customImagesModel? = null

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    @Inject
    lateinit var appDatabase: AppDatabase

    private var imagesList = mutableListOf<String>()
    private var imagesListUpScaled = mutableListOf<String>()

    private var selectedHd = true
    private var selectedUpScaled = false

    private var isGenerated = true

    private var currentIndex = 0

    private var currentIndexUpScaled = 0

    private var generateVariations = false
    private val imageLoadingHelper = ImageLoadingHelper()

    @Inject
    lateinit var workManager: WorkManager

    private val generateInteriorViewModel: GenerateInteriorViewModel by viewModels({ requireActivity() })

    var dtoObject: DTOvariations = DTOVarSingelton.getInstance()

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all permissions were granted
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            if (isGenerated) {
                initWorkManager()
            } else {
                showSnackBar("Please wait while art generation is in progress")
            }
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
        _binding = FragmentFullScreenInspireBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.shimmerViewContainer.startShimmerAnimation()
        binding.shimmerViewContainer1.startShimmerAnimation()
        binding.shimmerViewContainer2.startShimmerAnimation()
        binding.shimmerViewContainer3.startShimmerAnimation()
        binding.shimmerViewContainer4.startShimmerAnimation()

        setEvents()
        getPrevData()

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
                binding.addImagetoFavorites.setImageResource(R.drawable.favorite_filled)
                binding.addImagetoFavorites.setColorFilter(Color.parseColor("#E04F5F"))
            } else {
                binding.addImagetoFavorites.setImageResource(R.drawable.favorite_light)
                binding.addImagetoFavorites.setColorFilter(Color.parseColor("#999999"))
            }

        }
        imagesList = mutableListOf()

        genericModel?.output?.forEachIndexed { _, s ->
            imagesList.add(s)
        }
        if (imagesList.size > 0) {
            if (imagesList[0].isEmpty()) {
                isGenerated = false
                showSnackBar("The generate art is in queue, images will be available in a few seconds")
            } else {
                isGenerated = true
            }
            if (imagesList[0].isNotEmpty()) {
                binding.selectedImage.visibility = View.VISIBLE
                if (genericModel?.endpoint == Constants.EDIT_END_POINT) {
                    imageLoadingHelper.loadImage(
                        binding.progressBar5,
                        binding.generatedImage1,
                        imagesList[0]
                    )
                    imageLoadingHelper.loadImage(
                        binding.progressBar6,
                        binding.generatedImage2,
                        imagesList[1]
                    )
                    imageLoadingHelper.loadImage(
                        binding.progressBar7,
                        binding.generatedImage3,
                        imagesList[2]
                    )
                    imageLoadingHelper.loadImage(
                        binding.progressBar,
                        binding.selectedImage,
                        imagesList[0],
                        binding.buttons,
                        binding.openImage,
                        binding.addImagetoFavorites,
                        binding.shareImage,
                        binding.downloadImage
                    )
                    binding.progressBar.visibility = View.VISIBLE
                    if (imagesList.size > 3) {
                        imageLoadingHelper.loadImage(
                            binding.progressBar8,
                            binding.generatedImage4,
                            imagesList[3]
                        )
                    } else {
                        binding.generatedImage4box.visibility = View.GONE
                        binding.upscaledImage4box.visibility = View.GONE
                    }
                } else {
                    imageLoadingHelper.loadImage(
                        binding.progressBar5,
                        binding.generatedImage1,
                        imagesList[0]
                    )
                    imageLoadingHelper.loadImage(
                        binding.progressBar6,
                        binding.generatedImage2,
                        imagesList[1]
                    )
                    imageLoadingHelper.loadImage(
                        binding.progressBar7,
                        binding.generatedImage3,
                        imagesList[2]
                    )
                    imageLoadingHelper.loadImage(
                        binding.progressBar,
                        binding.selectedImage,
                        imagesList[0],
                        binding.buttons,
                        binding.openImage,
                        binding.addImagetoFavorites,
                        binding.shareImage,
                        binding.downloadImage
                    )
                    binding.progressBar.visibility = View.VISIBLE
                    if (imagesList.size > 3) {
                        imageLoadingHelper.loadImage(
                            binding.progressBar8,
                            binding.generatedImage4,
                            imagesList[3]
                        )
                    } else {
                        binding.generatedImage4box.visibility = View.GONE
                        binding.upscaledImage4box.visibility = View.GONE
                    }
                }

            }
        }
    }

    private fun initObservers(id: Int) {

        lifecycleScope.launch {
            var processing = false
            generateInteriorViewModel.UpscaleImage.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        processing = true
                    }

                    is Response.Success -> {

                        if (result.data != null) {
                            customProgressBar.dismiss()

                            val imageUrl = result.data.output[0]
                            Log.d(TAG, "Image URL: $imageUrl")
                            setUpScaledImagesToView(currentIndex, imageUrl)
                            if (selectedUpScaled && currentIndexUpScaled == currentIndex) {
                                imageLoadingHelper.loadImage(
                                    binding.progressBar,
                                    binding.selectedImage,
                                    imageUrl
                                )
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            val oldData = genericModel?.id?.let {
                                appDatabase.upscaleDao()?.getCreationsByIdNotLive(
                                    it, currentIndex
                                )
                            }

                            if (oldData == null) {
                                val upscale = genericModel?.id?.let {
                                    UpscaleResponseDatabase(
                                        System.currentTimeMillis().toInt(),
                                        it, currentIndex, result.data.output[0]
                                    )
                                }
                                upscale?.let { appDatabase.upscaleDao()?.saveData(it) }
                            } else {
                                appDatabase.upscaleDao()
                                    ?.updateOutputById(oldData.id, result.data.output[0], id)
                            }
                            generateInteriorViewModel.clearUpscale()
                        }
                    }

                    is Response.Error -> {
                        customProgressBar.dismiss()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        generateInteriorViewModel.clearUpscale()
                    }

                    is Response.Processing -> {
                        if (processing) {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Upscaling in progress! Please check back in a few seconds!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            if (result.data != null) {
                                customProgressBar.dismiss()

                                val imageUrl = result.data.output[0]
                                Log.d(TAG, "Image URL: $imageUrl")
                                delay(1000)
                                setUpScaledImagesToView(currentIndex, imageUrl)

                                val oldData = genericModel?.id?.let {
                                    appDatabase.upscaleDao()?.getCreationsByIdNotLive(
                                        it, currentIndex
                                    )
                                }

                                if (oldData == null) {
                                    val upscale = genericModel?.id?.let {
                                        UpscaleResponseDatabase(
                                            System.currentTimeMillis().toInt(),
                                            it, currentIndex, result.data.output[0]
                                        )
                                    }
                                    upscale?.let { appDatabase.upscaleDao()?.saveData(it) }
                                } else {
                                    appDatabase.upscaleDao()
                                        ?.updateOutputById(oldData.id, result.data.output[0], id)
                                }
                                generateInteriorViewModel.clearUpscale()
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            appDatabase.genericResponseDao()?.getCreationsById(id)?.observe(viewLifecycleOwner) {
                it?.let {
                    genericModel = it
                    initData()
                }
            }
        }

        lifecycleScope.launch {
            imagesListUpScaled.clear()
            appDatabase.upscaleDao()?.getAllUpscaled(id)
                ?.observe(viewLifecycleOwner) { upscaleResponseDatabases ->
                    upscaleResponseDatabases?.let { list ->
                        if (list.isNotEmpty()) {
                            enableUpscaleBox()
                        }
                        val maxIndex = list.maxOfOrNull { it.indexs } ?: -1
                        val listSize =
                            maxIndex + 1 // Finalize the list size from the greatest index available
                        imagesListUpScaled = MutableList(listSize) { _ -> null.toString() }

                        // Loop through the items retrieved from the database and populate the list at the specified indexes
                        for (item in list) {
                            val indexToAdd = item.indexs
                            val outputToAdd = item.output
                            lifecycleScope.launch(Dispatchers.Main) {
                                delay(2000)
                                setUpScaledImagesToView(item.indexs, item.output)
                            }
                            imagesListUpScaled[indexToAdd] = outputToAdd
                        }
                    }
                }
        }

        lifecycleScope.launch {
            generateInteriorViewModel.Imagevariations.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        customProgressBar.show(requireContext(), "Generating Variations")
                    }

                    is Response.Success -> {
                        if (generateVariations) {
                            val imageToImageResponse = result.data

                            if (imageToImageResponse != null) {

                                imageToImageResponse.meta.init_image = dtoObject.init_image
                                imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
//                                imageToImageResponse.endpoint = genericModel?.endpoint
                                imageToImageResponse.endpoint = Constants.INSPIRE_END_POINT
                                if (imageToImageResponse.id == null) {
                                    imageToImageResponse.id = System.currentTimeMillis().toInt()
                                }
                                appDatabase.genericResponseDao()?.saveData(
                                    imageToImageResponse
                                )
                                Log.d(
                                    TAG,
                                    "initObserversABC: " + imageToImageResponse.id.toString()
                                )
                                customProgressBar.dismiss()
                                Bundle().apply {
                                    putString("art", Gson().toJson(imageToImageResponse))
                                    putString("id", imageToImageResponse.id.toString())

                                    findNavController().navigate(
                                        R.id.action_your_fragment_to_itself,
                                        this
                                    )
                                }
                                generateInteriorViewModel.clearImageVariations()
                                generateVariations = false
                            }
                        }
                    }

                    is Response.Processing -> {
                        customProgressBar.dismiss()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "The Image is in processing. It will be generated in a few seconds.",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        val imageToImageResponse = result.data

                        if (imageToImageResponse != null) {

                            imageToImageResponse.meta.init_image = dtoObject.init_image
                            imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                            imageToImageResponse.endpoint = Constants.INSPIRE_END_POINT
                            if (imageToImageResponse.id == null) {
                                imageToImageResponse.id = System.currentTimeMillis().toInt()
                            }
                            appDatabase.genericResponseDao()?.saveData(
                                imageToImageResponse
                            )
                            Log.d(TAG, "initObserversABC: " + imageToImageResponse.id.toString())
                            customProgressBar.dismiss()
                            Bundle().apply {
                                putString("art", Gson().toJson(imageToImageResponse))
                                putString("id", imageToImageResponse.id.toString())

                                findNavController().navigate(
                                    R.id.action_your_fragment_to_itself,
                                    this
                                )
                            }
                            generateInteriorViewModel.clearImageVariations()
                            generateVariations = false
                        }
                    }

                    is Response.Error -> {
                        customProgressBar.dismiss()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        generateInteriorViewModel.clearImageVariations()
                        Log.e("TAG", "initObservers: ${result.message}")
                    }
                }
            }
        }
    }

    private fun enableUpscaleBox() {
        binding.hdTitleText.visibility = View.VISIBLE
        binding.upscaledTitleText.visibility = View.VISIBLE
        binding.upscaledImages.visibility = View.VISIBLE
    }

    private fun setUpScaledImagesToView(index: Int, output: String) {

        when (index) {
            0 -> {
                binding.upscaledImage1box.visibility = View.VISIBLE
                imageLoadingHelper.loadImage(binding.progressBar1, binding.upscaledImage1, output)
            }

            1 -> {
                binding.upscaledImage2box.visibility = View.VISIBLE
                imageLoadingHelper.loadImage(binding.progressBar2, binding.upscaledImage2, output)
            }

            2 -> {
                binding.upscaledImage3box.visibility = View.VISIBLE
                imageLoadingHelper.loadImage(binding.progressBar3, binding.upscaledImage3, output)
            }

            3 -> {
                binding.upscaledImage4box.visibility = View.VISIBLE
                imageLoadingHelper.loadImage(binding.progressBar4, binding.upscaledImage4, output)
            }
        }
    }

    private fun showSnackBar(title: String) {
        val snackBar = Snackbar
            .make(binding.root, title, Snackbar.LENGTH_LONG)
        snackBar.show()
    }

    fun setEvents() {
        binding.generatedImage1box.setOnClickListener {
            if (isGenerated) {
                Glide.with(activity?.applicationContext!!).load(imagesList[0])
                    .into(binding.selectedImage)
            }
            selectedHd = true
            selectedUpScaled = false
            currentIndex = 0
            binding.generatedImage1box.strokeColor = resources.getColor(R.color.selected_color)
            binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
            binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
            binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
        }

        binding.generatedImage2box.setOnClickListener {
            if (isGenerated) {
                Glide.with(activity?.applicationContext!!).load(imagesList[1])
                    .into(binding.selectedImage)
            }
            selectedHd = true
            selectedUpScaled = false
            currentIndex = 1
            binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
            binding.generatedImage2box.strokeColor = resources.getColor(R.color.selected_color)
            binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
            binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
        }

        binding.generatedImage3box.setOnClickListener {
            if (isGenerated) {
                Glide.with(activity?.applicationContext!!).load(imagesList[2])
                    .into(binding.selectedImage)
            }
            selectedHd = true
            selectedUpScaled = false

            currentIndex = 2
            binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
            binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
            binding.generatedImage3box.strokeColor = resources.getColor(R.color.selected_color)
            binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
            binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
        }

        binding.generatedImage4box.setOnClickListener {
            if (isGenerated && imagesList.size > 3) {
                Glide.with(activity?.applicationContext!!).load(imagesList[3])
                    .into(binding.selectedImage)
            }
            if (imagesList.size > 3) {
                selectedHd = true
                selectedUpScaled = false
                currentIndex = 3
                binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
                binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
                binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
                binding.generatedImage4box.strokeColor = resources.getColor(R.color.selected_color)
                binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
                binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
                binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
                binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
            }
        }

        binding.upscaledImage1box.setOnClickListener {

            if (imagesListUpScaled.isNotEmpty()) {
                if (imagesListUpScaled[0] != "null") {
                    Glide.with(activity?.applicationContext!!).load(imagesListUpScaled[0])
                        .into(binding.selectedImage)
                    selectedHd = false
                    selectedUpScaled = true
                    currentIndexUpScaled = 0
                    binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage1box.strokeColor =
                        resources.getColor(R.color.selected_color)
                    binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
                }
            }
        }

        binding.upscaledImage2box.setOnClickListener {
            if (imagesListUpScaled.isNotEmpty() && imagesListUpScaled.size > 1) {
                if (imagesListUpScaled[1] != "null") {
                    Glide.with(activity?.applicationContext!!).load(imagesListUpScaled[1])
                        .into(binding.selectedImage)
                    selectedHd = false
                    selectedUpScaled = true
                    currentIndexUpScaled = 1
                    binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage2box.strokeColor =
                        resources.getColor(R.color.selected_color)
                    binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
                }
            }


        }

        binding.upscaledImage3box.setOnClickListener {
            if (imagesListUpScaled.isNotEmpty() && imagesListUpScaled.size > 2) {
                if (imagesListUpScaled[2] != "null") {
                    Glide.with(activity?.applicationContext!!).load(imagesListUpScaled[2])
                        .into(binding.selectedImage)
                    selectedHd = false
                    selectedUpScaled = true
                    currentIndexUpScaled = 2
                    binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage3box.strokeColor =
                        resources.getColor(R.color.selected_color)
                    binding.upscaledImage4box.strokeColor = resources.getColor(R.color.transparent)
                }
            }


        }

        binding.upscaledImage4box.setOnClickListener {
            if (imagesListUpScaled.isNotEmpty() && imagesListUpScaled.size > 3) {
                if (imagesListUpScaled[3] != "null") {
                    Glide.with(activity?.applicationContext!!).load(imagesListUpScaled[3])
                        .into(binding.selectedImage)
                    selectedHd = false
                    selectedUpScaled = true
                    currentIndexUpScaled = 3
                    binding.generatedImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.generatedImage4box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage1box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage2box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage3box.strokeColor = resources.getColor(R.color.transparent)
                    binding.upscaledImage4box.strokeColor =
                        resources.getColor(R.color.selected_color)
                }
            }
        }



        binding.downloadImage.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                } else {
                    if (isGenerated) {
                        initWorkManager()
                    } else {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Please wait while art generation is in progress!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }

                }
            } else {
                if (isGenerated) {
                    initWorkManager()
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Please wait while art generation is in progress!",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            }
        }




        binding.addImagetoFavorites.setOnClickListener {
            if (isGenerated) {
                val oldData =
                    appDatabase.genericResponseDao()?.getCreationsByIdNotLive(genericModel?.id!!)
                if (oldData != null) {
                    if (oldData.isFavorite == true) {
                        oldData.isFavorite = false
                        appDatabase.genericResponseDao()?.UpdateData(oldData)

                        binding.addImagetoFavorites.setImageResource(R.drawable.favorite_light)
                        binding.addImagetoFavorites.setColorFilter(Color.parseColor("#999999"))
                    } else {
                        oldData.isFavorite = true
                        appDatabase.genericResponseDao()?.UpdateData(oldData)

                        binding.addImagetoFavorites.setImageResource(R.drawable.favorite_filled)
                        binding.addImagetoFavorites.setColorFilter(Color.parseColor("#E04F5F"))
                    }

                }
            }
        }

        binding.variations.setOnClickListener {
            if (isGenerated) {
                dtoObject.token = Constants.FIREBASE_TOKEN
                if (selectedUpScaled) {
                    dtoObject.init_image = imagesListUpScaled[currentIndexUpScaled]
                } else {
                    dtoObject.init_image = imagesList[currentIndex]
                }

                dtoObject.enhance_prompt = "yes"

                generateVariations = true
                generateInteriorViewModel.generateVariations(
                    dtoObject = dtoObject
                )

            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please wait while art generation is in progress!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

        }

        binding.shareImage.setOnClickListener {
            if (isGenerated) {
                if (selectedHd) {

                    downloadAndShareImageWithGlide(imagesList[currentIndex])
                } else if (selectedUpScaled) {
                    downloadAndShareImageWithGlide(imagesListUpScaled[currentIndexUpScaled])
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please wait while art generation is in progress!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.upscaleImage.setOnClickListener {
            if (isGenerated) {
                if (selectedHd) {
                    customProgressBar.show(requireContext(), "Upscaling Image....")
                    generateInteriorViewModel.upscaleImage(
                        DTOUpScale(
                            face_enhance = true,
                            key = "",
                            scale = 3,
                            url = imagesList[currentIndex],
                            token = Constants.FIREBASE_TOKEN,
                            type = "b",
                        )
                    )
                } else if (selectedUpScaled) {
                    customProgressBar.show(requireContext(), "Upscaling Image....")
                    generateInteriorViewModel.upscaleImage(
                        DTOUpScale(
                            face_enhance = true,
                            key = "",
                            scale = 3,
                            url = imagesListUpScaled[currentIndexUpScaled],
                            token = Constants.FIREBASE_TOKEN,
                            type = "b",
                        )
                    )
                }

            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please wait while art generation is in progress!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

        }

        binding.editImage.setOnClickListener {
            if (isGenerated) {
                if (selectedHd) {
                    Bundle().apply {
                        putString("type", Constants.EDIT_END_POINT)
                        putString("init_image", imagesList[currentIndex])
                        findNavController().navigate(R.id.maskImageFragment, this)
                    }
                } else if (selectedUpScaled) {
                    Bundle().apply {
                        putString("type", Constants.EDIT_END_POINT)
                        putString("init_image", imagesListUpScaled[currentIndexUpScaled])
                        findNavController().navigate(R.id.maskImageFragment, this)
                    }
                }

            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please wait while art generation is in progress!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

        }


        binding.openImage.setOnClickListener {
            if (isGenerated) {
                if (selectedHd) {
                    Bundle().apply {
                        putString("image", imagesList[currentIndex])
                        putString("art", Gson().toJson(genericModel))
                        putString("id", genericModel?.id.toString())

                        findNavController().navigate(
                            R.id.previewImageFragment,
                            this
                        )
                    }
                } else if (selectedUpScaled) {
                    Bundle().apply {
                        putString("id", genericModel?.id.toString())
                        putString("image", imagesListUpScaled[currentIndexUpScaled])

                        findNavController().navigate(
                            R.id.previewImageFragment,
                            this
                        )
                    }
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please wait while art generation is in progress!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
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

    private fun initWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(false)
            .setRequiresBatteryNotLow(false)
            .build()
        val data = Data.Builder()

        data.apply {
            putString(Constants.FILE_TYPE, "image")
            putString(
                "url", Gson().toJson(
                    if (selectedHd) listOf(
                        genericModel?.output?.get(currentIndex) ?: ""
                    ) else listOf(
                        imagesListUpScaled[currentIndexUpScaled] ?: ""
                    )
                )
            )
            putString(
                "url", Gson().toJson(
                    if (selectedHd) listOf(
                        genericModel?.output?.get(currentIndex) ?: ""
                    ) else listOf(
                        imagesListUpScaled[currentIndexUpScaled] ?: ""
                    )
                )
            )
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

                    val progress = workInfo.progress

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

    override fun onDestroyView() {
        super.onDestroyView()

    }
}