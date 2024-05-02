package com.bluell.roomdecoration.interiordesign.ui.home

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.common.interfaces.RoomTypeSelectionInterface
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentRoomStyleBottomSheetListDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RoomStyleBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentRoomStyleBottomSheetListDialogBinding? = null
    private var arrayList: ArrayList<RoomTypeModel>? = null
    private val binding get() = _binding!!
    private var roomStylePos:Int = 0


    private lateinit var adapter: RoomStyleAdapter
    private var roomTypeSelectionCallback: RoomStyleSelectionCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRoomStyleBottomSheetListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObservers()
        initRoomTypes()
    }

    private fun initObservers() {
        roomStylePos = Constants.ROOM_STYLE
    }

    fun setRoomStyleSelectionCallback(callback: RoomStyleSelectionCallback) {
        roomTypeSelectionCallback = callback
    }

    private fun initRoomTypes() {
        arrayList = Constants.getRoomStyles(requireActivity(),roomStylePos)
        adapter = RoomStyleAdapter(arrayList!!,roomStylePos,object:RoomTypeSelectionInterface{
            override fun selectedRoomType(item: RoomTypeModel, position: Int) {
                Constants.ROOM_STYLE = position
                roomStylePos = position
                adapter.updateSelection(
                    Constants.getRoomStyles(
                        requireActivity(),
                        roomStylePos
                    )
                )
                CoroutineScope(Dispatchers.Main).launch {
                    roomTypeSelectionCallback?.onRoomStyleSelected(item,position)
                    delay(500)
                    dismiss()
                }
            }
        })

        binding.roomsTypeRv.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL,false)
        binding.roomsTypeRv.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface RoomStyleSelectionCallback {
        fun onRoomStyleSelected(roomType: RoomTypeModel, pos: Int)
    }
}