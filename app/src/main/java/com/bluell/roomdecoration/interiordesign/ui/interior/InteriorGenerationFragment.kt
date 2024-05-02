package com.bluell.roomdecoration.interiordesign.ui.interior

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.Extras
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.common.interfaces.RoomTypeSelectionInterface
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOSingleton
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.BottomSheetSelectImageBinding
import com.bluell.roomdecoration.interiordesign.databinding.FragmentInteriorGenerationBinding
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.bluell.roomdecoration.interiordesign.ui.home.RoomStyleAdapterHome
import com.bluell.roomdecoration.interiordesign.ui.home.RoomStyleBottomSheet
import com.bluell.roomdecoration.interiordesign.ui.home.RoomTypeBottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.rewarded.RewardedAd
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class InteriorGenerationFragment : Fragment(), RoomTypeBottomSheet.RoomTypeSelectionCallback,
    RoomStyleBottomSheet.RoomStyleSelectionCallback {

    private var _binding: FragmentInteriorGenerationBinding? = null
    private val binding get() = _binding!!
    private var roomStyle: RoomTypeModel? = null
    private var roomStylePos: Int = 0
    private var roomTypePos: Int = 0
//    private var rewardedAd: RewardedAd? = null

    private lateinit var imageLoadingHelper: ImageLoadingHelper
    private var _navController: NavController? = null
    private val navController get() = _navController!!

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    @Inject
    lateinit var imageGenerationDialog: ImageGenerationDialog

    private var arrayListMyCreations: ArrayList<genericResponseModel?> = arrayListOf()

    private val viewModel: HistoryViewModel by viewModels()

    @Inject
    lateinit var appDatabase: AppDatabase

    private var roomType: RoomTypeModel? = null
    var adapter: RoomStyleAdapterHome? = null

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var imageStrength = 7

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if all permissions were granted
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                imageDetailsSheet(binding.uploadedImage)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.storage_permission_denied), Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val generateInteriorViewModel: InteriorGenerationViewModel by viewModels({ requireActivity() })
    var dtoObject: DTOObject = DTOSingleton.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInteriorGenerationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _navController = findNavController()

        imageLoadingHelper = ImageLoadingHelper()
        initObservers()
        initStyles()
        setEvents()
    }

    private fun setEvents() {
        binding.selectRoomType.setOnClickListener {
            val stylesBottomSheet = RoomTypeBottomSheet()
            stylesBottomSheet.setRoomTypeSelectionCallback(this)
            stylesBottomSheet.show(
                requireActivity().supportFragmentManager,
                stylesBottomSheet.tag
            )
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

        binding.viewAllStylesRooms.setOnClickListener {
            val stylesBottomSheet = RoomStyleBottomSheet()
            stylesBottomSheet.setRoomStyleSelectionCallback(this)
            stylesBottomSheet.show(
                requireActivity().supportFragmentManager,
                stylesBottomSheet.tag
            )
        }

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
                    requestMultiplePermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                } else {
                    // Permission is already granted, perform your desired action here
                    imageDetailsSheet(binding.uploadedImage)
                }
            } else {
                imageDetailsSheet(binding.uploadedImage)
            }
        }

        binding.toolbar.settings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        binding.cancelImage.setOnClickListener {
            binding.addPhotoHeadImage.visibility = View.VISIBLE
            binding.plusBtn.visibility = View.VISIBLE
            binding.imageBox.visibility = View.GONE
            Constants.initImageInterior = null
            Constants.clearInterior()
            binding.cancelImageBox.visibility = View.GONE
            binding.generatebtn.visibility = View.GONE
            Constants.initImage = null
            generateInteriorViewModel.clearImageToImage()
            generateInteriorViewModel.clearBase64Resp()
            binding.uploadedImage.setImageResource(0)
        }

        binding.generatebtn.setOnClickListener {
            if (Constants.initImageInterior != null) {
//                customProgressBar.show(
//                    requireContext(),
//                    requireContext().getString(R.string.loading_ad)
//                )
//                fullScreenContentCallBack()
//                loadRewarded()

                dtoObject.token = Constants.FIREBASE_TOKEN
                dtoObject.prompt =
                    "make this " + roomType?.room_title + " in " + roomStyle?.room_title + " style"
                dtoObject.init_image = Constants.initImageInterior
                dtoObject.steps = "51"
                dtoObject.guidance_scale = imageStrength.toString()
                dtoObject.endpoint = Constants.INTERIOR_END_POINT
                dtoObject.type = "a"
                generateInteriorViewModel.generateRoominterior(dtoObject = dtoObject)
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please select an image first!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    private fun initStyles() {
        binding.selectedRoomType.text = roomType!!.room_title
        adapter =
            RoomStyleAdapterHome(
                Constants.getRoomStyles(requireActivity(), roomStylePos),
                roomStylePos,
                object : RoomTypeSelectionInterface {
                    override fun selectedRoomType(item: RoomTypeModel, position: Int) {
                        Constants.ROOM_STYLE = position
                        roomStylePos = position
                        adapter?.updateSelection(
                            Constants.getRoomStyles(requireActivity(), roomStylePos)
                        )
                        roomStyle = item
                    }

                })

        binding.roomsStyleRv.adapter = adapter
        binding.roomsStyleRv.scrollToPosition(roomStylePos)
    }

    private fun imageDetailsSheet(imageView: ImageView) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetSelectImageBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        binding.openCamera.setOnClickListener {
            imageView.setImageResource(0)
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.cameraXFragment)
        }

        binding.pickGallery.setOnClickListener {
            bottomSheetDialog.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        bottomSheetDialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (!Constants.initImage.isNullOrEmpty()) {
            Constants.initImageInterior = Constants.initImage
            Constants.initImage = null
        }
        binding.toolbar.titleName.text = requireContext().getString(R.string.interior)
        if (!Constants.initImageInterior.isNullOrEmpty()) {
            binding.addPhotoHeadImage.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
            binding.imageBox.visibility = View.VISIBLE
            binding.cancelImageBox.visibility = View.VISIBLE
            binding.generatebtn.visibility = View.VISIBLE

            imageLoadingHelper.loadImage(
                binding.progressBar, binding.uploadedImage,
                Constants.initImageInterior!!
            )
        } else {
            binding.addPhotoHeadImage.visibility = View.VISIBLE
            binding.plusBtn.visibility = View.VISIBLE
            binding.imageBox.visibility = View.GONE
            binding.generatebtn.visibility = View.GONE
            binding.cancelImageBox.visibility = View.GONE
        }
    }

    fun initObservers() {
        roomStylePos = Constants.ROOM_STYLE
        roomStyle = Constants.getRoomStyles(requireActivity(), roomStylePos)[roomStylePos]
        roomTypePos = Constants.ROOM_TYPE
        roomType = Constants.getRoomTypesList(requireActivity(), roomTypePos)[roomTypePos]

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

        lifecycleScope.launch {
            generateInteriorViewModel.generateInteriorResp.collect { result ->
                when (result) {
                    is Response.Loading -> {
                        imageGenerationDialog.show(
                            requireContext()
                        )
                    }

                    is Response.Success -> {
                        if (isAdded) {
                            val imageToImageResponse = result.data
                            if (imageToImageResponse != null) {
                                imageToImageResponse.meta.init_image = dtoObject.init_image
                                imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                                Log.d(TAG, "initObserversINTERIOR: ${Constants.INTERIOR_END_POINT}")
                                imageToImageResponse.endpoint = Constants.INTERIOR_END_POINT
                                appDatabase.genericResponseDao()?.saveData(
                                    imageToImageResponse
                                )
                                Bundle().apply {
                                    putString("art", Gson().toJson(imageToImageResponse))
                                    putString("id", imageToImageResponse.id.toString())

                                    navController.navigate(
                                        R.id.fullScreenFragment,
                                        this
                                    )
                                }
                                mainActivityViewModel.subtractGems()
                                generateInteriorViewModel.clearImageToImage()
                                generateInteriorViewModel.clearBase64Resp()
                                Constants.clearInterior()
                                binding.addPhotoHeadImage.visibility = View.VISIBLE
                                binding.plusBtn.visibility = View.VISIBLE
                                binding.imageBox.visibility = View.GONE
                                binding.uploadedImage.setImageBitmap(null)
                            }
                        }
                        lifecycleScope.launch {
                            delay(500)
                            imageGenerationDialog.dismissDialog()
                        }
                    }

                    is Response.Processing -> {

                        if (isAdded) {
                            val imageToImageResponse = result.data
                            if (imageToImageResponse != null) {
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    "The Image is in processing It will be generated in a few seconds.",
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                                imageToImageResponse.meta.init_image = dtoObject.init_image
                                imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                                imageToImageResponse.endpoint = Constants.INTERIOR_END_POINT
                                appDatabase.genericResponseDao()?.saveData(
                                    imageToImageResponse
                                )
                                Bundle().apply {
                                    putString("art", Gson().toJson(imageToImageResponse))
                                    putString("id", imageToImageResponse.id.toString())

                                    navController.navigate(
                                        R.id.fullScreenFragment,
                                        this
                                    )
                                }
                                mainActivityViewModel.subtractGems()
                                generateInteriorViewModel.clearImageToImage()
                                generateInteriorViewModel.clearBase64Resp()
                                Constants.clearInterior()
                                binding.addPhotoHeadImage.visibility = View.VISIBLE
                                binding.plusBtn.visibility = View.VISIBLE
                                binding.imageBox.visibility = View.GONE
                                binding.uploadedImage.setImageBitmap(null)
                            }
                        }
                        imageGenerationDialog.dismissDialog()
                    }

                    is Response.Error -> {
                        imageGenerationDialog.getDialog()?.dismiss()
                        Log.d(TAG, "initObservers :${result.message}")
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        generateInteriorViewModel.clearImageToImage()
                    }
                }
            }
        }


        lifecycleScope.launch {
            generateInteriorViewModel.base64Response.collect { result ->
                when (result) {
                    is Response.Loading -> {}

                    is Response.Success -> {
                        if (result.data != null) {
                            val imgLink = result.data.image_url
                            Constants.initImageInterior = imgLink
                            if (isAdded) {
                                Log.d(TAG, "initObservers123: $imgLink")
                                binding.generatebtn.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                binding.cancelImageBox.visibility = View.VISIBLE
                            }
                        }
                    }

                    is Response.Error -> {
                        Log.d(TAG, "initObservers1: ${result.message}")
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()

                        if (isAdded) {
                            generateInteriorViewModel.clearBase64Resp()
                            binding.addPhotoHeadImage.visibility = View.VISIBLE
                            binding.plusBtn.visibility = View.VISIBLE
                            binding.imageBox.visibility = View.GONE
                        }
                    }

                    else -> {
                        if (isAdded) {
                            binding.progressBar.visibility = View.GONE
                        }
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
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }


    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                lifecycleScope.launch(Dispatchers.Main) {

                    uri.let {
                        if (isAdded) {
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

                                    lifecycleScope.launch {
                                        Log.d(TAG, "onResourceReady: ${uri.path}")
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

//    private fun isRewardEarned() {
//        // Implement your reward logic here (refer to ad network's documentation)
//        rewardedAd?.let { ad ->
//            ad.show(requireActivity()) { _ ->
//                // Handle the reward.
//                Constants.REWARD = true
//                if (Constants.initImageInterior != null) {
//                    dtoObject.token = Constants.FIREBASE_TOKEN
//                    dtoObject.prompt =
//                        "make this " + roomType?.room_title + " in " + roomStyle?.room_title + " style"
//                    dtoObject.init_image = Constants.initImageInterior
//                    dtoObject.steps = "51"
//                    dtoObject.guidance_scale = imageStrength.toString()
//                    dtoObject.endpoint = Constants.INTERIOR_END_POINT
//                    dtoObject.type = "a"
//                    generateInteriorViewModel.generateRoominterior(dtoObject = dtoObject)
//                } else {
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        "Please select an image first!",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
//                }
//                Log.d("AD_LOAD", "User earned the reward.")
//            }
//        } ?: run {
//            Log.d("AD_LOAD", "The rewarded ad wasn't ready yet.")
//        }
//    }

//    private fun fullScreenContentCallBack() {
//        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//            override fun onAdClicked() {
//                // Called when a click is recorded for an ad.
//                Log.d("AD_LOAD", "Ad was clicked.")
//            }
//
//            override fun onAdDismissedFullScreenContent() {
//                // Called when ad is dismissed.
//                // Set the ad reference to null so you don't show the ad a second time.
//                Log.d("AD_LOAD", "Ad dismissed fullscreen content.")
//                rewardedAd = null
//            }
//
//            override fun onAdImpression() {
//                // Called when an impression is recorded for an ad.
//                Log.d("AD_LOAD", "Ad recorded an impression.")
//            }
//
//            override fun onAdShowedFullScreenContent() {
//                // Called when ad is shown.
//                Log.d("AD_LOAD", "Ad showed fullscreen content.")
//            }
//        }
//    }

//    private fun loadRewarded() {
//        val adRequest = AdRequest.Builder().build()
//        RewardedAd.load(
//            requireActivity(),
//            Constants.REWARD_AD_ID,
//            adRequest,
//            object : RewardedAdLoadCallback() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    Log.d("AD_LOAD", adError.toString())
//                    customProgressBar.dismiss()
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        "An error occurred! Please try again later! Check your internet Connection",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
//                    rewardedAd = null
////                    Constants.REWARD = true
////                    if (Constants.initImageInterior != null) {
////                        dtoObject.token = Constants.FIREBASE_TOKEN
////                        dtoObject.prompt =
////                            "make this " + roomType?.room_title + " in " + roomStyle?.room_title + " style"
////                        dtoObject.init_image = Constants.initImageInterior
////                        dtoObject.steps = "51"
////                        dtoObject.guidance_scale = imageStrength.toString()
////                        dtoObject.endpoint = Constants.INTERIOR_END_POINT
////                        dtoObject.type = "a"
////                        generateInteriorViewModel.generateRoominterior(dtoObject = dtoObject)
////                    } else {
////                        Snackbar.make(
////                            requireActivity().findViewById(android.R.id.content),
////                            "Please select an image first!",
////                            Snackbar.LENGTH_LONG
////                        )
////                            .show()
////                    }
//                }
//
//                override fun onAdLoaded(ad: RewardedAd) {
//                    Log.d("AD_LOAD", "Ad was loaded.")
//                    rewardedAd = ad
//                    customProgressBar.dismiss()
//                    isRewardEarned()
//                }
//            })
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRoomStyleSelected(roomType: RoomTypeModel, pos: Int) {
        if (isAdded) {
            Constants.ROOM_STYLE = pos
            this.roomStylePos = pos
            this.roomStyle = roomType
            adapter?.updateSelection(
                Constants.getRoomStyles(requireActivity(), roomStylePos)
            )
            binding.roomsStyleRv.scrollToPosition(roomStylePos)
        }
    }

    override fun onRoomTypeSelected(roomType: RoomTypeModel, pos: Int) {
        if (isAdded) {
            Constants.ROOM_TYPE = pos
            this.roomTypePos = pos
            this.roomType = roomType
            binding.selectedRoomType.text = roomType.room_title
        }
    }
}