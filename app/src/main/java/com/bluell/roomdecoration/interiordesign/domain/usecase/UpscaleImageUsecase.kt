package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.response.upScaleResponse
import com.bluell.roomdecoration.interiordesign.domain.repo.InteriorDesignRepoistry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpscaleImageUsecase@Inject constructor(private val roomInteriorrepo: InteriorDesignRepoistry) {
    operator fun invoke(dtoObject: DTOUpScale): Flow<Response<upScaleResponse>> {
        return roomInteriorrepo.UpScaleImage(dtoObject)
    }

}