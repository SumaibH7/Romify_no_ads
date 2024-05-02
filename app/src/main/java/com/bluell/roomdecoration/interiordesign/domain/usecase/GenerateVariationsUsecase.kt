package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.repo.InteriorDesignRepoistry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GenerateVariationsUsecase @Inject constructor(private val textToImgRepository: InteriorDesignRepoistry){
    operator fun invoke(dtoObject: DTOvariations): Flow<Response<genericResponseModel>> {
        return textToImgRepository.GenerateVariations(dtoObject)
    }
}