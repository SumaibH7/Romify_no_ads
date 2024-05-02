package com.bluell.roomdecoration.interiordesign.data.remote

import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.upScaleResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface EndPointsInterface {

    @POST("V1/text_to_image.php")
    suspend fun createTextToImage(
        @Body dtoObject: DTOObjectGenerate
    ): genericResponseModel

    @POST("V1/img2img.php")
    suspend fun createVariations(
        @Body dtoObject: DTOvariations
    ): genericResponseModel

    @POST("V1/inpaint.php")
    suspend fun InPaint(
        @Body dtoObject: DTOObjectGenerate
    ): genericResponseModel

    @POST("V1/upscale.php")
    suspend fun UpScaleImage(
        @Body dtoUpScale: DTOUpScale
    ): upScaleResponse

    @POST("room_interior.php")
    suspend fun ImageToImage(
        @Body dtoObject: DTOObject
    ): genericResponseModel

    @Multipart
    @POST("test.php")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): base64Response

}