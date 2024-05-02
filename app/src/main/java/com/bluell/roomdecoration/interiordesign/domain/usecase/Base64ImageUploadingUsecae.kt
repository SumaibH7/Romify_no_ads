package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.domain.repo.InteriorDesignRepoistry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Base64ImageUploadingUsecae @Inject constructor(private val textToImgRepository: InteriorDesignRepoistry){
    operator fun invoke(dtoBase64: DTOBase64): Flow<Response<base64Response>> {
        return textToImgRepository.uploadBase64Image(dtoBase64)
    }
}