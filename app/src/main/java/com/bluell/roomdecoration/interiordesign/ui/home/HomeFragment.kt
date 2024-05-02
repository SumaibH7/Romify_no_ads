package com.bluell.roomdecoration.interiordesign.ui.home

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bluell.roomdecoration.interiordesign.MainActivityViewModel
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.FIREBASE_TOKEN
import com.bluell.roomdecoration.interiordesign.common.Constants.ROOM_STYLE
import com.bluell.roomdecoration.interiordesign.common.Constants.ROOM_TYPE
import com.bluell.roomdecoration.interiordesign.common.Constants.getRoomStyles
import com.bluell.roomdecoration.interiordesign.common.Constants.getRoomTypesList
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.Extras
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.common.interfaces.RoomTypeSelectionInterface
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOSingleton
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.newUser
import com.bluell.roomdecoration.interiordesign.databinding.DialogChooseGalleryOrCameraBinding
import com.bluell.roomdecoration.interiordesign.databinding.FragmentHomeBinding
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), RoomTypeBottomSheet.RoomTypeSelectionCallback,
    RoomStyleBottomSheet.RoomStyleSelectionCallback {
    private var _binding:FragmentHomeBinding ?= null
    private val binding get() = _binding!!


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

    private var roomType: RoomTypeModel?= null
    private var roomTypePos:Int = 0
    private var roomStylePos:Int = 0
    private var roomStyle: RoomTypeModel?= null
    var adapter: RoomsTypeAdapter?= null
    private var styleAdapter: RoomStyleAdapter?=  null

    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper
    private var userObj: newUser?=null

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private val generateInteriorViewModel: GenerateInteriorViewModel by viewModels({ requireActivity() })
    var dtoObject: DTOObject = DTOSingleton.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())

        _navController = findNavController()
        initObservers()
        initStyles()
        setEvents()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun initStyles() {
        binding.roomsTypeRv.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        adapter =
            RoomsTypeAdapter(getRoomTypesList(requireActivity(),roomTypePos), roomTypePos,object:
                RoomTypeSelectionInterface {
                override fun selectedRoomType(item: RoomTypeModel, position: Int) {
                   ROOM_TYPE = position
                    roomType = item
                }
            })
        binding.roomsTypeRv.adapter = adapter
        binding.roomsStyleRv.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        styleAdapter =
            RoomStyleAdapter(getRoomStyles(requireActivity(),roomStylePos), roomStylePos,object:RoomTypeSelectionInterface{
                override fun selectedRoomType(item: RoomTypeModel, position: Int) {
                    ROOM_STYLE = position
                    roomStyle = item
                }
            })
        binding.roomsStyleRv.adapter = styleAdapter
    }

    fun setEvents(){
        binding.addPhoto.setOnClickListener {
            showPopup(binding.addPhoto)
        }

        binding.gemAnim.setOnClickListener {
            navController.navigate(R.id.rewardsFragment)
        }

        binding.viewAllRooms.setOnClickListener {
            val stylesBottomSheet = RoomTypeBottomSheet()
            stylesBottomSheet.setRoomTypeSelectionCallback(this)
            stylesBottomSheet.show(
                requireActivity().supportFragmentManager,
                stylesBottomSheet.tag
            )
        }

        binding.viewAllStylesRooms.setOnClickListener {
            val stylesBottomSheet = RoomStyleBottomSheet()
            stylesBottomSheet.setRoomStyleSelectionCallback(this)
            stylesBottomSheet.show(
                requireActivity().supportFragmentManager,
                stylesBottomSheet.tag
            )
        }

        binding.generateInterior.setOnClickListener {
            if (Constants.initImage != null){
                dtoObject.token =  FIREBASE_TOKEN
                dtoObject.prompt = "make this "+roomType?.room_title+" in "+roomStyle?.room_title
                dtoObject.init_image = Constants.initImage
                dtoObject.steps = "50"
                dtoObject.guidance_scale = "7"
                dtoObject.endpoint = Constants.INTERIOR_END_POINT
                generateInteriorViewModel.generateRoominterior(dtoObject = dtoObject)
            }else{
                Toast.makeText(requireContext(),"Please add image first",Toast.LENGTH_SHORT).show()
            }



        }
    }


    fun initObservers(){

        roomStylePos = ROOM_STYLE
        roomStyle = getRoomStyles(requireActivity(),roomStylePos)[roomStylePos]
        roomTypePos = ROOM_TYPE
        roomType = getRoomTypesList(requireActivity(),roomTypePos)[roomTypePos]

        mainActivityViewModel.user.observe(viewLifecycleOwner){
            binding.gemsTV.text=it.totalGems.toString()
            userObj = it
        }

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
            generateInteriorViewModel.generateInteriorResp.collect() { result ->
                when (result) {
                    is Response.Loading -> {
//                        customProgressBar.show(requireContext())
                        Log.e("TAG", "initobservers: loading" )
                        imageGenerationDialog.show(requireContext())
                    }

                    is Response.Success -> {
//                        imageGenerationDialog.dismissDialog()
                        Log.e("TAG", "initObservers:success  ${result.data}")
                        val imageToImageResponse = result.data
                        if (imageToImageResponse != null && imageToImageResponse.output!!.isNotEmpty()) {

                            imageToImageResponse.meta.init_image = dtoObject.init_image
                            imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
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
                            Constants.clear()

                        }
                    }

                    is Response.Processing -> {
                        Toast.makeText(
                            requireContext(),
                            "The Image is in processing It will be generated in a few seconds",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is Response.Error -> {
                        generateInteriorViewModel.clearImageToImage()
                        Constants.clear()
//                        customProgressBar.getDialog()?.dismiss()
//                        imageGenerationDialog.getDialog()?.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Error :${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("TAG", "initObservers: ${result.message}")
                    }
                }
            }
        }


        lifecycleScope.launch {
            generateInteriorViewModel.base64Response.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        customProgressBar.show(requireContext(),requireContext().getString(R.string.uploading_image))
                        Log.e("TAG", "promptType in loading upload")
                    }

                    is Response.Success -> {
                        if (result.data != null) {

                            val imgLink = result.data.image_url
                            Constants.initImage = imgLink
                            Log.e("TAG", "initObservers: "+ Constants.initImage )
                            delay(500)
                            customProgressBar.getDialog()?.dismiss()
                            generateInteriorViewModel.clearBase64Resp()
                        }

                    }

                    is Response.Error -> {
                        customProgressBar.getDialog()?.dismiss()
                        generateInteriorViewModel.clearBase64Resp()
                        Toast.makeText(requireContext(), "${result.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {
                        customProgressBar.getDialog()?.dismiss()
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
                                if (genericResponseModel.output!!.isNotEmpty()){
                                    arrayListMyCreations.add(genericResponseModel)
                                }
                            }
                        }
                    }
                    is Response.Error -> {}
                    else -> {
                        customProgressBar.getDialog()?.dismiss() }
                }
            }
        }
    }

    private fun clearSingletonInstances() {
        DTOSingleton.resetInstance()
        Constants.clear()
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            lifecycleScope.launch(Dispatchers.Main) {

                uri.let {
                    customProgressBar.show(requireContext(),requireContext().getString(R.string.uploading_image))
                    val bitmap = withContext(Dispatchers.IO) {
                        Constants.loadBitmapFromUri(uri, requireContext())
                    }

                    Glide.with(activity?.applicationContext!!)
                        .asBitmap()
                        .load(bitmap)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                binding.addPhotoHeadImage.setImageBitmap(resource)
                                lifecycleScope.launch {
                                    generateInteriorViewModel.uploadBase64(
                                        DTOBase64(
                                            Extras.getFileFromUri(requireContext(), uri)!!
                                        )
                                    )
                                }
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {} })
                }

            }
        }
    }

    private fun showPopup(anchorView: View) {
        val popupViewBinding = DialogChooseGalleryOrCameraBinding.inflate(layoutInflater)
        val popupView = popupViewBinding.root

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popupViewBinding.openCamera.setOnClickListener {
            popupWindow.dismiss()
            findNavController().navigate(R.id.cameraXFragment)
        }

        popupViewBinding.pickGallery.setOnClickListener {
            popupWindow.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Required for touch outside to work
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(anchorView)
    }

    override fun onRoomTypeSelected(roomType: RoomTypeModel, pos: Int) {
        ROOM_TYPE = pos
        this.roomTypePos = pos
        this.roomType = roomType
        initStyles()
    }

    override fun onRoomStyleSelected(roomType: RoomTypeModel, pos: Int) {
        ROOM_STYLE = pos
        this.roomStylePos = pos
        this.roomStyle = roomType
        initStyles()

    }
}