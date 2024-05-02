package com.bluell.roomdecoration.interiordesign.domain.repo

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.upScaleResponse
import kotlinx.coroutines.flow.Flow

interface InteriorDesignRepoistry {
    fun GenerateTextToImage(dtoObject: DTOObjectGenerate): Flow<Response<genericResponseModel>>
    fun GenerateInPaint(dtoObject: DTOObjectGenerate):Flow<Response<genericResponseModel>>
    fun GenerateRoomInterior(dtoObject: DTOObject): Flow<Response<genericResponseModel>>

    fun UpScaleImage(dtoUpScale: DTOUpScale):Flow<Response<upScaleResponse>>


    fun uploadBase64Image(dtoBase64: DTOBase64):Flow<Response<base64Response>>

    fun GenerateVariations(dtoObject: DTOvariations): Flow<Response<genericResponseModel>>

}