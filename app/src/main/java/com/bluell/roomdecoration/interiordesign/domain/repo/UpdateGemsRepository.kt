package com.bluell.roomdecoration.interiordesign.domain.repo

import com.bluell.roomdecoration.interiordesign.common.Response
import kotlinx.coroutines.flow.Flow


interface UpdateGemsRepository {

    fun updateGemsUseCase(map: Map<String,Any>): Flow<Response<Int>>
}