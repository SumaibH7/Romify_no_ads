package com.bluell.roomdecoration.interiordesign.common.interfaces

import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel


interface RoomTypeSelectionInterface {
    fun selectedRoomType(item: RoomTypeModel, position:Int)
}