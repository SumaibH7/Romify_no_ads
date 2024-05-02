package com.bluell.roomdecoration.interiordesign.domain.repo

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import kotlinx.coroutines.flow.Flow


interface FetchDataRepository {

    fun fetchAllCreations(includeAll:Int,point:String): Flow<Response<List<genericResponseModel>>>
}