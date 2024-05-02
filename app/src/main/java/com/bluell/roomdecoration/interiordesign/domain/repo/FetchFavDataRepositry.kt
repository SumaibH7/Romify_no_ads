package com.bluell.roomdecoration.interiordesign.domain.repo

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import kotlinx.coroutines.flow.Flow

/**
 * ***********************************************************************************************
 * *                                                                                             *
 * *          _   _      _                                                                       *
 * *         | | | |    (_)                                                                      *
 * *         | |_| | ___ _ _ __   __ _  __ _  ___                                                *
 * *         |  _  |/ _ \ | '_ \ / _` |/ _` |/ _ \                                               *
 * *         | | | |  __/ | | | | (_| | (_| |  __/                                               *
 * *                                                                                             *
 * *         Created by Hamza ch on 10/20/2023.                                                      *
 * *         GAMICAN                                                                          *
 * *                                                                                             *
 * ***********************************************************************************************
 */
interface FetchFavDataRepositry {
    fun fetchAllFavCreations(point:String): Flow<Response<List<genericResponseModel>>>
}