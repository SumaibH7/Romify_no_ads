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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bluell.roomdecoration.interiordesign.ImageLoadingHelper
import com.bluell.roomdecoration.interiordesign.MainActivityViewModel
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.ForegroundWorker
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOSingleton
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOVarSingelton
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.UpscaleResponseDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.customImagesModel
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentFullScreenBinding
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.bluell.roomdecoration.interiordesign.ui.home.GenerateInteriorViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenFragment : Fragment() {

    private var _binding: FragmentFullScreenBinding? = null
    private val binding get() = _binding!!

    private var genericModel: genericResponseModel? = null

    private var imagesList = mutableListOf<String>()

    var dtoObject: DTOObject = DTOSingleton.getInstance()

    private var dtoVar: DTOvariations = DTOVarSingelton.getInstance()

    private var _navController: NavController? = null

    private var imagesListUpscaled = mutableListOf<String>()
    private val navController get() = _navController!!

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var imageGenerationDialog: ImageGenerationDialog

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    private var selectedHd = true
    private var selectedUpscaled = false
    private val imageLoadingHelper = ImageLoadingHelper()

    private var customImgObj: customImagesModel? = null

    private var arrayListMyCreations: ArrayList<genericResponseModel?> = arrayListOf()

    private val viewModel: HistoryViewModel by viewModels()

    var preferenceDataStoreHelper: UserPreferencesDataStoreHelper? = null

    @Inject
    lateinit var appDatabase: AppDatabase

    private val generateInteriorViewModel: GenerateInteriorViewModel by viewModels({ requireActivity() })

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var isGenerated = true

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
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

        _binding = FragmentFullScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())
        binding.shimmerViewContainer.startShimmerAnimation()

        binding.shimmerViewContainer1.startShimmerAnimation()

        setEvents()
        _navController = findNavController()
        getPrevData()
    }

    fun setEvents() {
        binding.upscaledImage1box.setOnClickListener {
            if (imagesListUpscaled.isNotEmpty()) {
                Glide.with(activity?.applicationContext!!).load(imagesListUpscaled[0]).into(binding.actualImg)
                selectedHd = false
                selectedUpscaled = true
                binding.upscaledImage1box.strokeColor = resources.getColor(R.color.selected_color)
            }
        }

        binding.editImage.setOnClickListener {
            if (isGenerated){
                if (selectedHd){
                    Bundle().apply {
                        putString("type", Constants.EDIT_END_POINT)
                        putString("init_image", imagesList[0])
                        findNavController().navigate(R.id.maskImageFragment, this)
                    }
                }else if (selectedUpscaled){
                    Bundle().apply {
                        putString("type", Constants.EDIT_END_POINT)
                        putString("init_image", imagesListUpscaled[0])
                        findNavController().navigate(R.id.maskImageFragment, this)
                    }
                }

            }else{
                showSnackBar("Please wait, Art generation is in progress")
            }

        }

        binding.variations.setOnClickListener {
            if (isGenerated){
                dtoVar.token = Constants.FIREBASE_TOKEN
                if (selectedUpscaled){
                    dtoVar.init_image = imagesListUpscaled[0]
                }else{
                    dtoVar.init_image = imagesList[0]
                }

                dtoVar.negative_prompt = null
                dtoVar.prompt = null
                dtoVar.num_inference_steps = "21"
                dtoVar.strength = 0.5
                dtoVar.guidance_scale = 13.0
                dtoVar.enhance_prompt = "yes"

                Log.e("TAG", "setEvents: $dtoObject")
                generateInteriorViewModel.generateVariations(
                    dtoObject = dtoVar
                )
            }else{
                showSnackBar("Please wait, Art generation is in progress")
            }

        }

        binding.saveImg.setOnClickListener {
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
                        showSnackBar("Please wait while art generation is in progress")
                    }

                }
            }else{
                if (isGenerated) {
                    initWorkManager()
                } else {
                    showSnackBar("Please wait while art generation is in progress")
                }
            }

        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
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
            }else{
                showSnackBar("Please wait while art generation is in progress")
            }
        }

        binding.shareImg.setOnClickListener {
            if (isGenerated){
                downloadAndShareImageWithGlide(imagesList[0])
            }else{
                showSnackBar("Please wait while art generation is in progress")
            }
        }

        binding.openImage.setOnClickListener {
            if (isGenerated) {
                if (selectedHd) {
                    Bundle().apply {
                        putString("image", imagesList[0])
                        putString("art", Gson().toJson(genericModel))
                        putString("id", genericModel?.id.toString())

                        findNavController().navigate(
                            R.id.previewImageFragment,
                            this
                        )
                    }
                } else if (selectedUpscaled) {
                    Bundle().apply {
                        putString("id", genericModel?.id.toString())
                        putString("image", imagesListUpscaled[0])

                        findNavController().navigate(
                            R.id.previewImageFragment,
                            this
                        )
                    }
                }
            }else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please wait while art generation is in progress!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }

        binding.upscaleImage.setOnClickListener {
            if (isGenerated){
                customProgressBar.show(requireContext(),"Upscaling Image....")
                generateInteriorViewModel.upscaleImage(
                    DTOUpScale(
                        face_enhance = true,
                        key = "",
                        scale = 3,
                        url = imagesList[0],
                        token = Constants.FIREBASE_TOKEN,
                        type = "b",

                        )
                )
            }else{
                showSnackBar("Please wait while art generation is in progress")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSnackBar("Permission Granted")
                } else {
                    showSnackBar("Permission is required to save Image")
                }
                return
            }
        }
    }

    private fun downloadAndShareImageWithGlide(imageUrl: String) {
        // Define the directory and filename where the shared image will be stored
        val imageName = "${System.currentTimeMillis()}.png"
        val directory = File(requireContext().cacheDir, "shared_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, imageName)

        // Use Glide to download and save the image
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

                        // Create a share intent
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file))
                            type = "image/*"
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }

                        // Show the system share sheet

                        if (isAdded){
                            requireContext().startActivity(Intent.createChooser(shareIntent, "Share Image"))

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

    private fun getPrevData() {
        arguments?.let {
            val id = it.getString("id")
            genericModel = appDatabase.genericResponseDao()?.getCreationsByIdNotLive(id!!.toInt())
            if (genericModel != null) {
                initData()
                initObservers(genericModel?.id!!.toInt())
            } else {
                showSnackBar("This art has been deleted by the user!")
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
                showSnackBar("The generate art is in queue, image will be available in a few seconds.")
            } else {
                binding.actualImg.visibility = View.VISIBLE
                imageLoadingHelper.loadImage(
                    binding.progressBar,
                    binding.actualImg,
                    imagesList[0],
                    binding.buttons,
                    binding.openImage,
                    binding.addImagetoFavorites,
                    binding.shareImg,
                    binding.saveImg
                )
                binding.progressBar.visibility = View.VISIBLE
                isGenerated = true
            }
        }
    }


    private fun initObservers(id: Int) {
        appDatabase.genericResponseDao()?.getAllCreationsLive(1,"")?.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                it?.let { data ->
                    arrayListMyCreations = arrayListOf()
                    data.forEachIndexed { _, genericResponseModel ->
                        if (genericResponseModel.output!!.isNotEmpty()) {
                            arrayListMyCreations.add(genericResponseModel)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            var processing = false
            generateInteriorViewModel.UpscaleImage.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        processing = true
                    }
                    is Response.Success -> {
                        customProgressBar.dismiss()
                        if (result.data != null) {
                            val oldData = genericModel?.id?.let {
                                appDatabase.upscaleDao()?.getCreationsByIdNotLive(
                                    it,0)
                            }
                            if (selectedUpscaled){
                                imageLoadingHelper.loadImage(binding.progressBar,binding.actualImg,result.data.output[0])
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            val upscale = genericModel?.id?.let {
                                UpscaleResponseDatabase(System.currentTimeMillis().toInt(),
                                    it,0,result.data.output[0])
                            }

                            if (oldData == null){
                                upscale?.let { appDatabase.upscaleDao()?.saveData(it) }
                            }else{

                                oldData.output = result.data.output[0]
                                appDatabase.upscaleDao()?.UpdateData(oldData)
                            }
                            delay(1000)
                            setUpScaledImagesToView(upscale!!.indexs, upscale.output)
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
                        Log.d(TAG, "initObservers: ${result.message}")
                        generateInteriorViewModel.clearUpscale()
                    }

                    is Response.Processing -> {
                        if (processing){
                            customProgressBar.dismiss()
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Upscaling in progress! Please check back in a few seconds!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            if (result.data != null) {
                                val oldData = genericModel?.id?.let {
                                    appDatabase.upscaleDao()?.getCreationsByIdNotLive(
                                        it,0)
                                }

                                val upscale = genericModel?.id?.let {
                                    UpscaleResponseDatabase(System.currentTimeMillis().toInt(),
                                        it,0,result.data.output[0])
                                }

                                if (oldData == null){
                                    upscale?.let { appDatabase.upscaleDao()?.saveData(it) }
                                }else{

                                    oldData.output = result.data.output[0]
                                    appDatabase.upscaleDao()?.UpdateData(oldData)
                                }
                                delay(1000)
                                setUpScaledImagesToView(upscale!!.indexs, upscale.output)
                                generateInteriorViewModel.clearUpscale()
                            }
                        }
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            var processing = false
            generateInteriorViewModel.Imagevariations.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        processing = true
                        customProgressBar.show(requireContext(),"Generating Variations")
                    }

                    is Response.Success -> {
                        val imageToImageResponse = result.data

                        Log.d(TAG, "initObserversTest: ${result.data}")
                        if (imageToImageResponse != null) {
                            imageToImageResponse.meta.init_image = dtoObject.init_image
                            imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
//                            imageToImageResponse.endpoint = genericModel?.endpoint
                            imageToImageResponse.endpoint = Constants.INSPIRE_END_POINT
                            if (imageToImageResponse.id == null) {
                                imageToImageResponse.id = System.currentTimeMillis().toInt()
                            }
                            appDatabase.genericResponseDao()?.saveData(
                                imageToImageResponse
                            )
                            if (findNavController().currentDestination?.id == R.id.fullScreenFragment) {
                                customProgressBar.dismiss()
                                generateInteriorViewModel.clearImageToImage()
                                Bundle().apply {
                                    putString("art", Gson().toJson(imageToImageResponse))
                                    putString("id", imageToImageResponse.id.toString())

                                    findNavController().navigate(
                                        R.id.action_fragmentA_to_fragmentB,
                                        this
                                    )
                                }
                            }
                            generateInteriorViewModel.clearImageVariations()
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
                        if (result.data!=null){
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "The Image is in processing It will be generated in a few seconds.",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                        generateInteriorViewModel.clearImageVariations()
                    }

                    is Response.Error -> {
                        Constants.clear()
                        customProgressBar.dismiss()
                        result.message.takeIf { it.isNotEmpty() }?.let {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "An error occurred! Please try again later!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                        generateInteriorViewModel.clearImageVariations()
                        Log.e("TAG", "initObservers: ${result.message}")
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            appDatabase.upscaleDao()?.getAllUpscaled(id)?.observe(viewLifecycleOwner) {
                it?.let {
                    Log.e("TAG", "imagesFromDB: $it")
                    if (it.isNotEmpty()){
                        enableUpscaleBox()
                    }
                    for (i in it.indices){
                        imagesListUpscaled.add(it[i].output)
                        setUpScaledImagesToView(it[i].indexs,it[i].output)
                    }
                }
            }
        }


        lifecycleScope.launch {
            viewModel.allCreations.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {
                    }

                    is Response.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            arrayListMyCreations = arrayListOf()
                            result.data.forEachIndexed { _, genericResponseModel ->
                                if (genericResponseModel.output!!.isNotEmpty()){
                                    arrayListMyCreations.add(genericResponseModel)
                                }
                            }
                        }
                    }

                    is Response.Error -> {
                    }

                    else -> {
                    }
                }
            }
        }


        lifecycleScope.launch {
            generateInteriorViewModel.generateInteriorResp.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        imageGenerationDialog.show(requireContext())
                    }

                    is Response.Success -> {
//                        imageGenerationDialog.dismissDialog()
                        if (result.data != null) {
                            val generiModelResponse = result.data
                            if (generiModelResponse.output!!.isNotEmpty()) {
                                Log.e("TAG", "initObservers:success  ${result.data}")
                                val imageToImageResponse = result.data
                                if (imageToImageResponse.output!!.isNotEmpty()) {
                                    isGenerated = true
                                    imageToImageResponse.meta.height = genericModel?.meta?.height
                                    imageToImageResponse.meta.width = genericModel?.meta?.width
                                    imageToImageResponse.meta.init_image = customImgObj?.output
                                    imageToImageResponse.meta.mask_image = ""
                                    imageToImageResponse.meta.instance_prompt = ""
                                    imageToImageResponse.meta.samples = "4"
                                    imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                                    imageToImageResponse.meta.mId = genericModel?.meta?.mId
                                    imageToImageResponse.meta.exactNegativePrompt =
                                        genericModel?.meta?.exactNegativePrompt

                                    appDatabase.genericResponseDao()?.saveData(
                                        imageToImageResponse
                                    )


                                    val bundle = Bundle().apply {
                                        putString("art", Gson().toJson(imageToImageResponse))
                                        putString("id", imageToImageResponse.id.toString())
                                    }
                                    navController.popBackStack(R.id.fullScreenFragment, true)
                                    navController.navigate(
                                        R.id.fullScreenFragment,
                                        bundle
                                    )
                                    mainActivityViewModel.subtractGems()
                                    generateInteriorViewModel.clearImageToImage()
                                    Constants.clear()
                                }
                            }
                        }

                    }

                    is Response.Processing -> {
                        generateInteriorViewModel.clearImageToImage()
                        if (result.data!=null)showSnackBar("The Image is in processing It will be generated in a few seconds")
                    }

                    is Response.Error -> {
                        customProgressBar.dismiss()
                        generateInteriorViewModel.clearImageToImage()
                        if (result.message.isEmpty())showSnackBar("An error occurred! Please try again later!")
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            appDatabase.genericResponseDao()?.getCreationsById(id)?.observe(viewLifecycleOwner) {
                it?.let {
                    genericModel = it
                    Log.e("TAG", "imagesFromDB: $it")
                    initData()
                }
            }
        }
    }

    private fun enableUpscaleBox(){
        binding.upscaledTitleText.visibility = View.VISIBLE
        binding.upscaledImages.visibility = View.VISIBLE
    }

    private fun setUpScaledImagesToView(index:Int, output:String){
        when(index){
            0 -> {
                if (isAdded){
                    imageLoadingHelper.loadImage(binding.progressBar1,binding.upscaledImage1,output)
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
            putString("url", Gson().toJson(listOf(customImgObj?.output)))
        }
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(ForegroundWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .addTag(UUID.randomUUID().toString())
                .build()

        workManager.enqueue(oneTimeWorkRequest)
        showSnackBar("Image download is started")
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
                if (workInfo != null) {

                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            showSnackBar("Image has been downloaded")
                        }

                        WorkInfo.State.RUNNING -> {
                            Log.e("TAG", "initObservers:running buddy")
                        }

                        WorkInfo.State.FAILED -> {
                            showSnackBar("Failed to download!")
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

    private fun showSnackBar(title: String) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            title,
            Snackbar.LENGTH_LONG
        )
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}