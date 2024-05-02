package com.bluell.roomdecoration.interiordesign.ui.edit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluell.roomdecoration.interiordesign.ImageLoadingHelper
import com.bluell.roomdecoration.interiordesign.MainActivityViewModel
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.Constants.initImage
import com.bluell.roomdecoration.interiordesign.common.Constants.initImageEdit
import com.bluell.roomdecoration.interiordesign.common.Constants.maskedImage
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.Extras
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOGenSingleton
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.BottomSheetSelectImageBinding
import com.bluell.roomdecoration.interiordesign.databinding.FragmentEditDesignBinding
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentEditDesign : Fragment() {
    private var _binding: FragmentEditDesignBinding? = null
    private val binding get() = _binding!!

    var dtoObject: DTOObjectGenerate = DTOGenSingleton.getInstance()

    private lateinit var imageLoadingHelper: ImageLoadingHelper
    private val generateInteriorViewModel: EditRoomInteriorViewModel by viewModels({ requireActivity() })
    private val viewModel: HistoryViewModel by viewModels()

    @Inject
    lateinit var imageGenerationDialog: ImageGenerationDialog

    private var arrayListMyCreations: ArrayList<genericResponseModel?> = arrayListOf()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var _navController: NavController? = null
    private val navController get() = _navController!!

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    private var selectedImageUri: Uri? = null

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if all permissions were granted
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                imageDetailsSheet()
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
        _binding = FragmentEditDesignBinding.inflate(inflater, container, false)

        binding.root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val heightDiff = binding.root.height - (bottom - top)
            if (heightDiff > 100) {
                binding.generatebtn.visibility = View.GONE
            } else {
                if (initImageEdit!=null){
                    binding.generatebtn.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _navController = findNavController()
        imageLoadingHelper = ImageLoadingHelper()
        setEvents()
        checkPreviousData()
        initObservers()

    }


    private fun checkPreviousData() {
        binding.toolbar.titleName.text = requireContext().getString(R.string.edit)
        if (maskedImage?.isNotEmpty() == true) {
            binding.addPhotoHeadImage.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
            binding.imageBox.visibility = View.VISIBLE
            imageLoadingHelper.loadImage(
                binding.progressBar, binding.uploadedImage,
                maskedImage!!
            )
        } else if (Constants.initImageEdit?.isNotEmpty() == true) {
            binding.addPhotoHeadImage.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
            binding.imageBox.visibility = View.VISIBLE
            imageLoadingHelper.loadImage(
                binding.progressBar, binding.uploadedImage,
                Constants.initImageEdit!!
            )
        }
    }


    fun setEvents() {
        binding.addPhotoHeadImage.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    Log.e("TAG", "functionality: inside click permission")
                    requestMultiplePermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                } else {
                    imageDetailsSheet()
                }
            } else {
                imageDetailsSheet()
            }

        }

        binding.clearEdt.setOnClickListener {
            binding.promptEdt.setText("")
        }

//        binding.toolbar.goPro.setOnClickListener {
//            Snackbar.make(
//                requireActivity().findViewById(android.R.id.content),
//                "Coming Soon!",
//                Snackbar.LENGTH_LONG
//            )
//                .show()
////            findNavController().navigate(R.id.premiumFragment)
//        }

        binding.toolbar.settings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        binding.generatebtn.setOnClickListener {
            if (!initImageEdit.isNullOrEmpty()) {
                if (maskedImage.isNullOrEmpty()){
                    maskedImage = initImageEdit
                }
                //                customProgressBar.show(
//                    requireContext(),
//                    requireContext().getString(R.string.loading_ad)
//                )
//                fullScreenContentCallBack()
//                loadRewarded()
                if (binding.promptEdt.text.toString().isEmpty() || maskedImage?.isEmpty() == true) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Prompt is empty",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
                if (initImageEdit == null) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Please select an image first!",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
                dtoObject.prompt =
                    "${dtoObject.instance_prompt} _ ${binding.promptEdt.text.toString()}"
                dtoObject.upscale = "no"
                dtoObject.init_image = initImageEdit
                dtoObject.mask_image = maskedImage
                dtoObject.token = Constants.FIREBASE_TOKEN
                dtoObject.endpoint = "v3/inpaint"
                dtoObject.samples = "4"
                dtoObject.strength = null
                generateInteriorViewModel.generateInpaint(
                    dtoObject = dtoObject
                )
            }else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please select an image first!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }

        binding.cancelImage.setOnClickListener {
            binding.addPhotoHeadImage.visibility = View.VISIBLE
            binding.plusBtn.visibility = View.VISIBLE
            binding.imageBox.visibility = View.GONE
            binding.cancelImageBox.visibility = View.GONE
            binding.generatebtn.visibility = View.GONE
            Constants.clearEdit()
            generateInteriorViewModel.clearBase64Resp()
            binding.uploadedImage.setImageResource(0)
            maskedImage = null
        }
    }

    private fun imageDetailsSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetSelectImageBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        binding.openCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.cameraXFragment)
        }

        binding.pickGallery.setOnClickListener {
            bottomSheetDialog.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        bottomSheetDialog.show()
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    uri.let {
                        if (isActive) {
                            binding.addPhotoHeadImage.visibility = View.GONE
                            binding.plusBtn.visibility = View.GONE
                            binding.imageBox.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        Glide.with(activity?.applicationContext!!)
                            .asBitmap()
                            .load(uri)
                            .apply(RequestOptions().override(1024, 1024))
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {

                                    binding.uploadedImage.setImageBitmap(resource)
                                    binding.cancelImageBox.visibility = View.GONE
                                    lifecycleScope.launch {
                                        generateInteriorViewModel.uploadBase64(
                                            DTOBase64(
                                                Extras.getFileFromUri(
                                                    requireContext(),
                                                    uri
                                                )!!
                                            )
                                        )
                                    }

                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    }
                }
            } else {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    binding.addPhotoHeadImage.visibility = View.VISIBLE
                    binding.plusBtn.visibility = View.VISIBLE
                    binding.imageBox.visibility = View.GONE
                }
            }
        }

    fun initObservers() {
        lifecycleScope.launch {
            generateInteriorViewModel.base64Response.collect() { result ->
                when (result) {
                    is Response.Loading -> {}

                    is Response.Success -> {
                        if (result.data != null) {
                            val imgLink = result.data.image_url
                            initImageEdit = imgLink
                            if (isAdded){
                                binding.generatebtn.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                binding.cancelImageBox.visibility = View.VISIBLE
                            }

                            Bundle().apply {
                                putString("type", "create")
                                putString("uri", selectedImageUri.toString())
                                findNavController().navigate(R.id.maskImageFragment, this)
                            }
                            generateInteriorViewModel.clearBase64Resp()
                        }

                    }

                    is Response.Error -> {
                        if (isAdded) {
                            binding.addPhotoHeadImage.visibility = View.VISIBLE
                            binding.plusBtn.visibility = View.VISIBLE
                            binding.imageBox.visibility = View.GONE
                        }
                        generateInteriorViewModel.clearBase64Resp()
                        Log.d(TAG, "initObservers: ${result.message}")
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }

                    else -> {
                        if (isAdded){
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            generateInteriorViewModel.inPaintImage.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        imageGenerationDialog.show(
                            requireContext())
                    }

                    is Response.Success -> {

                            val imageToImageResponse = result.data
                            if (imageToImageResponse?.id == null) {
                                imageToImageResponse?.id = System.currentTimeMillis().toInt()
                            }
                            if (imageToImageResponse != null) {
                                imageToImageResponse.meta.init_image = dtoObject.init_image
                                imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                                imageToImageResponse.endpoint = Constants.EDIT_END_POINT
                                appDatabase.genericResponseDao()?.saveData(
                                    imageToImageResponse
                                )
                                if (imageGenerationDialog.isDialogShowing()){
                                    Bundle().apply {
                                        putString("art", Gson().toJson(imageToImageResponse))
                                        putString("id", imageToImageResponse.id.toString())

                                        navController.navigate(
                                            R.id.fullScreenInspireFragment,
                                            this
                                        )
                                    }
                                }
                                mainActivityViewModel.subtractGems()
                                generateInteriorViewModel.clearInPaint()
                                Constants.clearEdit()
                                if (isAdded) {
                                    binding.addPhotoHeadImage.visibility = View.VISIBLE
                                    binding.plusBtn.visibility = View.VISIBLE
                                    binding.imageBox.visibility = View.GONE
                                    binding.uploadedImage.setImageBitmap(null)
                                }
                            }
                                imageGenerationDialog.getDialog()?.dismiss()
                    }

                    is Response.Processing -> {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "The Image is in processing It will be generated in a few seconds.",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        if (isAdded) {
                            val imageToImageResponse = result.data
                            if (imageToImageResponse != null) {
                                imageToImageResponse.meta.init_image = dtoObject.init_image
                                imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                                imageToImageResponse.endpoint = Constants.EDIT_END_POINT
                                appDatabase.genericResponseDao()?.saveData(
                                    imageToImageResponse
                                )
                                Bundle().apply {
                                    putString("art", Gson().toJson(imageToImageResponse))
                                    putString("id", imageToImageResponse.id.toString())

                                    navController.navigate(
                                        R.id.fullScreenInspireFragment,
                                        this
                                    )
                                }
                                mainActivityViewModel.subtractGems()
                                generateInteriorViewModel.clearInPaint()
                                Constants.clearEdit()
                                binding.addPhotoHeadImage.visibility = View.VISIBLE
                                binding.plusBtn.visibility = View.VISIBLE
                                binding.imageBox.visibility = View.GONE
                                binding.uploadedImage.setImageBitmap(null)
                            }
                        }
                        if (imageGenerationDialog.isDialogShowing()){
                            lifecycleScope.launch {
                                delay(100)
                                imageGenerationDialog.getDialog()?.dismiss()
                            }
                        }

                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Image Generation in Progress you'll be soon notified!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }

                    is Response.Error -> {
                        imageGenerationDialog.getDialog()?.dismiss()
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

        lifecycleScope.launch {
            viewModel.allCreations.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {}

                    is Response.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            arrayListMyCreations = arrayListOf()
                            result.data.forEachIndexed { _, genericResponseModel ->
                                if (genericResponseModel.output!!.isNotEmpty()) {
                                    arrayListMyCreations.add(genericResponseModel)
                                }
                            }
                        }
                    }

                    is Response.Error -> {}
                    is Response.Processing -> {}
                }
            }
        }


        appDatabase.genericResponseDao()?.getAllCreationsLive(1, "")?.observe(viewLifecycleOwner) {
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
    }

    override fun onResume() {
        super.onResume()

        if (!initImage.isNullOrEmpty()) {
            Constants.initImageEdit = initImage
            initImage = null
            if (maskedImage.isNullOrEmpty()) {
                Bundle().apply {
                    putString("type", "create")
                    putString("uri", selectedImageUri.toString())
                    findNavController().navigate(R.id.maskImageFragment, this)
                }
                generateInteriorViewModel.clearBase64Resp()
            }
        }
        if (!Constants.initImageEdit.isNullOrEmpty()) {
            binding.addPhotoHeadImage.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
            binding.imageBox.visibility = View.VISIBLE
            binding.cancelImageBox.visibility = View.VISIBLE
            binding.generatebtn.visibility = View.VISIBLE
            imageLoadingHelper.loadImage(
                binding.progressBar, binding.uploadedImage,
                Constants.initImageEdit!!
            )
        } else {
            binding.addPhotoHeadImage.visibility = View.VISIBLE
            binding.plusBtn.visibility = View.VISIBLE
            binding.cancelImageBox.visibility = View.GONE
            binding.generatebtn.visibility = View.GONE
            binding.imageBox.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}