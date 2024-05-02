package com.bluell.roomdecoration.interiordesign.ui.home

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.common.interfaces.RoomTypeSelectionInterface
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentRoomTypeBottomSheetListDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoomTypeBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentRoomTypeBottomSheetListDialogBinding? = null
    private var arrayList: ArrayList<RoomTypeModel>? = null
    private val binding get() = _binding!!

    private var roomTypePos:Int = 0
    private lateinit var adapter: RoomsTypeAdapter

    private var roomTypeSelectionCallback: RoomTypeSelectionCallback? = null
    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRoomTypeBottomSheetListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObservers()
        initRoomTypes()
    }

    private fun initObservers() {
        roomTypePos = Constants.ROOM_TYPE
    }

    fun setRoomTypeSelectionCallback(callback: RoomTypeSelectionCallback) {
        roomTypeSelectionCallback = callback
    }

    private fun initRoomTypes() {
        arrayList = Constants.getRoomTypesList(requireActivity(),roomTypePos)
        adapter = RoomsTypeAdapter(arrayList!!,roomTypePos,object:RoomTypeSelectionInterface{
            override fun selectedRoomType(item: RoomTypeModel, position: Int) {
                roomTypeSelectionCallback?.onRoomTypeSelected(item,position)
                CoroutineScope(Dispatchers.Main).launch {
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

    interface RoomTypeSelectionCallback {
        fun onRoomTypeSelected(roomType: RoomTypeModel, pos: Int)
    }
}