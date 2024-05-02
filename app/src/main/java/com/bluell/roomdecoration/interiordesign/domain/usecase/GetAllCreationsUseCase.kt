package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.repo.FetchDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCreationsUseCase @Inject constructor(private val fetchDataRepository: FetchDataRepository) {
    operator fun invoke(includeAll:Int,point:String):Flow<Response<List<genericResponseModel>>>{
        return fetchDataRepository.fetchAllCreations(includeAll,point)
    }
}