package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.repo.FetchFavDataRepositry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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

class GetFavCreationsUseCase @Inject constructor(private val fetchDataRepository: FetchFavDataRepositry) {
    operator fun invoke(point:String): Flow<Response<List<genericResponseModel>>> {
        return fetchDataRepository.fetchAllFavCreations(point)
    }
}