package com.bluell.roomdecoration.interiordesign.ui.history.fusion

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.RecyclerviewItemDecore
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.DialogChooseGalleryOrCameraBinding
import com.bluell.roomdecoration.interiordesign.databinding.FragmentEditHistoryBinding
import com.bluell.roomdecoration.interiordesign.domain.repo.MyInteriorClicks
import com.bluell.roomdecoration.interiordesign.ui.history.GenerateHistoryAdaptor
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class EditHistoryFragment : Fragment() {

    private var _binding:FragmentEditHistoryBinding ?= null
    private val binding get() = _binding!!

    private var currentPopupWindow: PopupWindow? = null

    private var arrayListMyCreations: MutableList<genericResponseModel>? = mutableListOf()
    private var myCreationAdapter: GenerateHistoryAdaptor? = null

    private val viewModel: HistoryViewModel by viewModels()

    private var _navController: NavController? = null
    private val navController get() = _navController!!

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditHistoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _navController = findNavController()

        binding.rvHistory.addItemDecoration(
            RecyclerviewItemDecore(
                2,
                20,
                false
            )
        )

        initObservers()

    }

    private fun initObservers() {
        appDatabase.genericResponseDao()?.getAllCreationsLive(0,Constants.EDIT_END_POINT)?.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.emptySupport.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                it?.let { data ->
                    arrayListMyCreations = mutableListOf()
                    data.forEachIndexed { _, genericResponseModel ->
                        if (genericResponseModel.output!!.isNotEmpty()) {
                            arrayListMyCreations?.add(genericResponseModel)
                        }
                    }
                    initMyCreations()
                    myCreationAdapter?.notifyDataSetChanged()
                }
            } else {
                binding.rvHistory.visibility = View.GONE
                binding.emptySupport.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.allCreations.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {
                        binding.emptySupport.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                    }

                    is Response.Success -> {
                        binding.emptySupport.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                        if (!result.data.isNullOrEmpty()) {
                            arrayListMyCreations = mutableListOf()
                            result.data.forEachIndexed { _, genericResponseModel ->
                                if (genericResponseModel.output!!.isNotEmpty()){
                                    arrayListMyCreations?.add(genericResponseModel)
                                }
                            }

                            initMyCreations()
                            myCreationAdapter?.notifyDataSetChanged()
                        }
                    }

                    is Response.Error -> {
                        binding.rvHistory.visibility = View.GONE
                        binding.emptySupport.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.emptySupport.visibility=View.GONE}
                }
            }
        }
    }

    private fun initMyCreations() {

        myCreationAdapter = arrayListMyCreations?.let { GenerateHistoryAdaptor(it,object:
            MyInteriorClicks {
            override fun onMenuItemClick(item: genericResponseModel?, position: Int, anchor: View) {
                showPopup(anchor, item!!,position)
            }

            override fun onItemClick(item: genericResponseModel?, position: Int) {
                    Bundle().apply {
                        this.putString("art", Gson().toJson(item))
                        this.putString("id", item!!.id.toString())
                        navController.navigate(R.id.fullScreenInspireFragment, this)
                    }


            }
        }) }
        binding.rvHistory.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.rvHistory.adapter = myCreationAdapter

    }

    private fun showPopup(anchorView: View, item: genericResponseModel, position:Int) {
        currentPopupWindow?.dismiss()

        val popupViewBinding = DialogChooseGalleryOrCameraBinding.inflate(layoutInflater)
        val popupView = popupViewBinding.root

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        currentPopupWindow = popupWindow

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorBottomY = location[1] + anchorView.height

        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        val spaceBelow = screenHeight - anchorBottomY

        val offsetY = if (spaceBelow >= popupHeight) {
            0
        } else {
            -popupHeight - anchorView.height
        }

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(anchorView, 0, offsetY)

        popupViewBinding.cameraTxt.text = getString(R.string.delete)
        popupViewBinding.galleryTxt.text = getString(R.string.favorites)
        popupViewBinding.cameraIcon.setImageResource(R.drawable.trash_light)

        if (item.isFavorite == true){
            popupViewBinding.galleryIcon.setImageResource(R.drawable.favorite_filled)
            popupViewBinding.galleryIcon.setColorFilter(Color.parseColor("#E04F5F"))

        }else{
            popupViewBinding.galleryIcon.setImageResource(R.drawable.favorite_light)
            popupViewBinding.galleryIcon.setColorFilter(Color.parseColor("#999999"))

        }
        popupViewBinding.openCamera.setOnClickListener {
            popupWindow.dismiss()
            appDatabase.genericResponseDao()?.deleteCreation(item)
            arrayListMyCreations?.removeAt(position)
            myCreationAdapter?.notifyDataSetChanged()
        }

        popupViewBinding.pickGallery.setOnClickListener {
            popupWindow.dismiss()
            if (item.isFavorite == true) {
                item.isFavorite = false
                popupViewBinding.galleryIcon.setImageResource(R.drawable.favorite_light)
                popupViewBinding.galleryIcon.setColorFilter(Color.parseColor("#999999"))
                appDatabase.genericResponseDao()?.UpdateData(item)
                arrayListMyCreations?.clear()
            } else {
                item.isFavorite = true
                arrayListMyCreations?.clear()
                popupViewBinding.galleryIcon.setImageResource(R.drawable.favorite_filled)
                popupViewBinding.galleryIcon.setColorFilter(Color.parseColor("#E04F5F"))
                appDatabase.genericResponseDao()?.UpdateData(item)
            }
        }

        popupWindow.setOnDismissListener {
            currentPopupWindow = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}