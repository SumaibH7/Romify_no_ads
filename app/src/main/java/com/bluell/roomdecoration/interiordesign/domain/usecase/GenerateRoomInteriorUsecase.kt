package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.repo.InteriorDesignRepoistry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GenerateRoomInteriorUsecase@Inject constructor(private val roomInteriorrepo: InteriorDesignRepoistry) {
    operator fun invoke(dtoObject: DTOObject): Flow<Response<genericResponseModel>> {
        return roomInteriorrepo.GenerateRoomInterior(dtoObject)
    }

}