package com.bluell.roomdecoration.interiordesign.ui.exterior

//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.OnUserEarnedRewardListener
//import com.google.android.gms.ads.rewarded.RewardedAd
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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
import com.bluell.roomdecoration.interiordesign.databinding.FragmentExteriorGenerationBinding
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.bluell.roomdecoration.interiordesign.ui.home.RoomStyleAdapterHome
import com.bluell.roomdecoration.interiordesign.ui.home.RoomStyleBottomSheet
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
class ExteriorGenerationFragment : Fragment(),
    RoomStyleBottomSheet.RoomStyleSelectionCallback {

    private var _binding: FragmentExteriorGenerationBinding? = null
    private val binding get() = _binding!!

    private var roomStyle: RoomTypeModel? = null
    private lateinit var imageLoadingHelper: ImageLoadingHelper

    private var _navController: NavController? = null

    private var roomStylePos: Int = 0
    private val navController get() = _navController!!

//    private var rewardedAd: RewardedAd? = null

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    @Inject
    lateinit var imageGenerationDialog: ImageGenerationDialog

    private var arrayListMyCreations: ArrayList<genericResponseModel?> = arrayListOf()

    private val viewModel: HistoryViewModel by viewModels()

    @Inject
    lateinit var appDatabase: AppDatabase

    var adapter: RoomStyleAdapterHome? = null

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var imageDetailing = "20"
    private var imageStrength = 7

    private val generateInteriorViewModel: ExteriorGenerationViewModel by viewModels({ requireActivity() })
    var dtoObject: DTOObject = DTOSingleton.getInstance()

    private var houseAngle = "front"

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
        _binding = FragmentExteriorGenerationBinding.inflate(inflater, container, false)
        Log.e("exterior", "onCreateView:123 ")
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


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setEvents() {

        binding.viewAllStylesRooms.setOnClickListener {
            val stylesBottomSheet = RoomStyleBottomSheet()
            stylesBottomSheet.setRoomStyleSelectionCallback(this)
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
                    // Permission is already granted, perform your desired action here

                    imageDetailsSheet()
                }
            } else {
                imageDetailsSheet()
            }
        }

        binding.toolbar.settings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        binding.cancelImage.setOnClickListener {
            binding.addPhotoHeadImage.visibility = View.VISIBLE
            binding.plusBtn.visibility = View.VISIBLE
            binding.imageBox.visibility = View.GONE
            Constants.initImageExterior = null
            Constants.clearExterior()
            binding.cancelImageBox.visibility = View.GONE
            binding.generatebtn.visibility = View.GONE
            Constants.initImage = null
            generateInteriorViewModel.clearImageToImage()
            generateInteriorViewModel.clearBase64Resp()
            binding.uploadedImage.setImageResource(0)
        }

        binding.frontAngle.setOnClickListener {
            houseAngle = "front"
            Constants.HOUSE_ANGLE = 0
            binding.frontAngle.setTextColor(resources.getColor(R.color.white))
            binding.backAngle.setTextColor(resources.getColor(R.color.image_color_filter))
            binding.sideAngle.setTextColor(resources.getColor(R.color.image_color_filter))
            binding.frontAngle.setBackgroundColor(resources.getColor(R.color.image_color_filter))
            binding.backAngle.setBackgroundColor(resources.getColor(R.color.same))
            binding.sideAngle.setBackgroundColor(resources.getColor(R.color.same))
        }

        binding.backAngle.setOnClickListener {
            houseAngle = "back"
            Constants.HOUSE_ANGLE = 1
            binding.backAngle.setTextColor(resources.getColor(R.color.white))
            binding.frontAngle.setTextColor(resources.getColor(R.color.image_color_filter))
            binding.sideAngle.setTextColor(resources.getColor(R.color.image_color_filter))
            binding.backAngle.setBackgroundColor(resources.getColor(R.color.image_color_filter))
            binding.frontAngle.setBackgroundColor(resources.getColor(R.color.same))
            binding.sideAngle.setBackgroundColor(resources.getColor(R.color.same))
        }

        binding.sideAngle.setOnClickListener {
            houseAngle = "side"
            Constants.HOUSE_ANGLE = 2
            binding.sideAngle.setTextColor(resources.getColor(R.color.white))
            binding.frontAngle.setTextColor(resources.getColor(R.color.image_color_filter))
            binding.backAngle.setTextColor(resources.getColor(R.color.image_color_filter))
            binding.sideAngle.setBackgroundColor(resources.getColor(R.color.image_color_filter))
            binding.frontAngle.setBackgroundColor(resources.getColor(R.color.same))
            binding.backAngle.setBackgroundColor(resources.getColor(R.color.same))
        }


        binding.generatebtn.setOnClickListener {
            if (Constants.initImageExterior != null) {
//                customProgressBar.show(
//                    requireContext(),
//                    requireContext().getString(R.string.loading_ad)
//                )
//                fullScreenContentCallBack()
//                loadRewarded()
                dtoObject.token = Constants.FIREBASE_TOKEN
                dtoObject.prompt =
                    "Generate " + roomStyle?.room_title + " exterior views of a house from distinct angles - " + houseAngle + "View, upon user selection, ensuring accurate representation and realism in the rendered images"
                dtoObject.init_image = Constants.initImageExterior
                dtoObject.steps = "50"
                dtoObject.guidance_scale = "20"
                dtoObject.endpoint = Constants.INTERIOR_END_POINT
                generateInteriorViewModel.generateRoominterior(dtoObject = dtoObject)
                Log.d("ADLOAD", "User earned the reward.")
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

    override fun onResume() {
        super.onResume()
        if (!Constants.initImage.isNullOrEmpty()) {
            Constants.initImageExterior = Constants.initImage
            Constants.initImage = null
        }
        binding.toolbar.titleName.text = requireContext().getString(R.string.exterior)
        if (!Constants.initImageExterior.isNullOrEmpty()) {
            binding.addPhotoHeadImage.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
            binding.imageBox.visibility = View.VISIBLE
            binding.cancelImageBox.visibility = View.VISIBLE
            binding.generatebtn.visibility = View.VISIBLE
            imageLoadingHelper.loadImage(
                binding.progressBar, binding.uploadedImage,
                Constants.initImageExterior!!
            )
        } else {
            binding.addPhotoHeadImage.visibility = View.VISIBLE
            binding.generatebtn.visibility = View.GONE
            binding.plusBtn.visibility = View.VISIBLE
            binding.cancelImageBox.visibility = View.GONE
            binding.imageBox.visibility = View.GONE
        }
    }

    fun initObservers() {

        roomStylePos = Constants.ROOM_STYLE
        roomStyle = Constants.getRoomStyles(requireActivity(), roomStylePos)[roomStylePos]

        if (isAdded) {
            when (Constants.HOUSE_ANGLE) {
                0 -> {
                    houseAngle = "front"
                    binding.frontAngle.setTextColor(resources.getColor(R.color.white))
                    binding.backAngle.setTextColor(resources.getColor(R.color.image_color_filter))
                    binding.sideAngle.setTextColor(resources.getColor(R.color.image_color_filter))
                    binding.frontAngle.setBackgroundColor(resources.getColor(R.color.image_color_filter))
                    binding.backAngle.setBackgroundColor(resources.getColor(R.color.same))
                    binding.sideAngle.setBackgroundColor(resources.getColor(R.color.same))
                }

                1 -> {
                    houseAngle = "back"
                    binding.frontAngle.setTextColor(resources.getColor(R.color.image_color_filter))
                    binding.backAngle.setTextColor(resources.getColor(R.color.white))
                    binding.sideAngle.setTextColor(resources.getColor(R.color.image_color_filter))
                    binding.frontAngle.setBackgroundColor(resources.getColor(R.color.same))
                    binding.backAngle.setBackgroundColor(resources.getColor(R.color.image_color_filter))
                    binding.sideAngle.setBackgroundColor(resources.getColor(R.color.same))
                }

                else -> {
                    houseAngle = "side"
                    binding.frontAngle.setTextColor(resources.getColor(R.color.image_color_filter))
                    binding.backAngle.setTextColor(resources.getColor(R.color.image_color_filter))
                    binding.sideAngle.setTextColor(resources.getColor(R.color.white))
                    binding.frontAngle.setBackgroundColor(resources.getColor(R.color.same))
                    binding.backAngle.setBackgroundColor(resources.getColor(R.color.same))
                    binding.sideAngle.setBackgroundColor(resources.getColor(R.color.image_color_filter))
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

        lifecycleScope.launch {
            generateInteriorViewModel.generateInteriorResp.collect() { result ->
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
                                imageToImageResponse.meta.type = Constants.EXTERIOR_END_POINT
                                imageToImageResponse.endpoint = Constants.EXTERIOR_END_POINT
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
                                Constants.clearExterior()
                                binding.addPhotoHeadImage.visibility = View.VISIBLE
                                binding.plusBtn.visibility = View.VISIBLE
                                binding.imageBox.visibility = View.GONE
                                binding.uploadedImage.setImageBitmap(null)
                            }
                        }
                        imageGenerationDialog.dismissDialog()
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
                                imageToImageResponse.meta.type = Constants.EXTERIOR_END_POINT
                                imageToImageResponse.endpoint = Constants.EXTERIOR_END_POINT
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
                                Constants.clearExterior()
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
                        result.message.takeIf { it.isNotEmpty() }?.let {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "An error occurred! Please try again later!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                        Log.e("TAG", "initObservers: ${result.message}")
                    }
                }
            }
        }


        lifecycleScope.launch {
            generateInteriorViewModel.base64Response.collect() { result ->
                when (result) {
                    is Response.Loading -> {}

                    is Response.Success -> {
                        if (result.data != null) {
                            Log.e("TAG", "Image link has been generated ")
                            val imgLink = result.data.image_url
                            Constants.initImageExterior = imgLink
                            Log.e("TAG", "initObservers: " + Constants.initImageExterior)
                            delay(500)
                            if (isAdded) {
                                binding.generatebtn.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                binding.cancelImageBox.visibility = View.VISIBLE
                            }
                        }

                    }

                    is Response.Error -> {
                        result.message.takeIf { it.isNotEmpty() }?.let {
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
                        Log.e("TAG", "initObservers1: ${result.message}")
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
                            result.data.forEachIndexed { index, genericResponseModel ->
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
    }

    private fun initStyles() {
        if (isAdded) {
            adapter =
                RoomStyleAdapterHome(
                    Constants.getRoomStyles(requireActivity(), roomStylePos),
                    roomStylePos,
                    object : RoomTypeSelectionInterface {
                        override fun selectedRoomType(item: RoomTypeModel, position: Int) {
                            Constants.ROOM_STYLE = position
                            roomStylePos = position
                            adapter?.updateSelection(
                                Constants.getRoomStyles(
                                    requireActivity(),
                                    roomStylePos
                                )
                            )
                            roomStyle = item
                        }
                    })
            binding.roomsStyleRv.adapter = adapter
            binding.roomsStyleRv.scrollToPosition(roomStylePos)
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
                            binding.progressBar.visibility = View.VISIBLE
                            binding.addPhotoHeadImage.visibility = View.GONE
                            binding.plusBtn.visibility = View.GONE
                            binding.imageBox.visibility = View.VISIBLE
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

                                    if (isAdded) {
                                        binding.uploadedImage.setImageBitmap(resource)
                                    }
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

//    private fun fullScreenContentCallBack() {
//        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//            override fun onAdClicked() {
//                // Called when a click is recorded for an ad.
//                Log.d("ADLOAD", "Ad was clicked.")
//            }
//
//            override fun onAdDismissedFullScreenContent() {
//                // Called when ad is dismissed.
//                // Set the ad reference to null so you don't show the ad a second time.
//                Log.d("ADLOAD", "Ad dismissed fullscreen content.")
//                rewardedAd = null
//            }
//
//            override fun onAdImpression() {
//                // Called when an impression is recorded for an ad.
//                Log.d("ADLOAD", "Ad recorded an impression.")
//            }
//
//            override fun onAdShowedFullScreenContent() {
//                // Called when ad is shown.
//                Log.d("ADLOAD", "Ad showed fullscreen content.")
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
//                    Log.d("ADLOAD", adError.toString())
//                    rewardedAd = null
//                    customProgressBar.dismiss()
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        "An error occurred! Please try again later! Check your internet Connection",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
////                    if (Constants.initImageExterior != null) {
////                        dtoObject.token = Constants.FIREBASE_TOKEN
////                        dtoObject.prompt =
////                            "Generate " + roomStyle?.room_title + " exterior views of a house from distinct angles - " + houseAngle + "View, upon user selection, ensuring accurate representation and realism in the rendered images"
////                        dtoObject.init_image = Constants.initImageExterior
////                        dtoObject.steps = "50"
////                        dtoObject.guidance_scale = "20"
////                        dtoObject.endpoint = Constants.INTERIOR_END_POINT
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
//                    Log.d("ADLOAD", "Ad was loaded.")
//                    rewardedAd = ad
//                    customProgressBar.dismiss()
//                    isRewardEarned()
//                }
//            })
//    }

//    private fun isRewardEarned() {
//        // Implement your reward logic here (refer to ad network's documentation)
//        rewardedAd?.let { ad ->
//            ad.show(requireActivity(), OnUserEarnedRewardListener { _ ->
//                // Handle the reward.
//                Constants.REWARD = true
//                if (Constants.initImageExterior != null) {
//                    dtoObject.token = Constants.FIREBASE_TOKEN
//                    dtoObject.prompt =
//                        "Generate " + roomStyle?.room_title + " exterior views of a house from distinct angles - " + houseAngle + "View, upon user selection, ensuring accurate representation and realism in the rendered images"
//                    dtoObject.init_image = Constants.initImageExterior
//                    dtoObject.steps = "50"
//                    dtoObject.guidance_scale = "20"
//                    dtoObject.endpoint = Constants.INTERIOR_END_POINT
//                    generateInteriorViewModel.generateRoominterior(dtoObject = dtoObject)
//                } else {
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        "Please select an image first!",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
//                }
//                Log.d("ADLOAD", "User earned the reward.")
//            })
//        } ?: run {
//            Log.d("ADLOAD", "The rewarded ad wasn't ready yet.")
//        }
//    }

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
}