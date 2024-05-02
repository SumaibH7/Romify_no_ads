package com.bluell.roomdecoration.interiordesign.domain.repo

import android.view.View
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel


interface MyInteriorClicks {

    fun onMenuItemClick(item: genericResponseModel?, position: Int, anchor: View)

    fun onItemClick(item: genericResponseModel?, position: Int)

}