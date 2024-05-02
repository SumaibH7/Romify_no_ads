package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.domain.repo.UpdateGemsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateGemsUseCase @Inject constructor(private val updateGemsRepository: UpdateGemsRepository){
    operator fun invoke(map: Map<String,Any>):Flow<Response<Int>>{
        return updateGemsRepository.updateGemsUseCase(map)
    }
}