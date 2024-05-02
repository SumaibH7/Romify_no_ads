package com.bluell.roomdecoration.interiordesign.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.upScaleResponse
import com.bluell.roomdecoration.interiordesign.domain.usecase.Base64ImageUploadingUsecae
import com.bluell.roomdecoration.interiordesign.domain.usecase.GenerateRoomInteriorUsecase
import com.bluell.roomdecoration.interiordesign.domain.usecase.GenerateVariationsUsecase
import com.bluell.roomdecoration.interiordesign.domain.usecase.UpscaleImageUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerateInteriorViewModel @Inject constructor(
    private val generateRoomInteriorUsecase: GenerateRoomInteriorUsecase,
    private val base64ImageUploadingUsecae: Base64ImageUploadingUsecae,
    private val upscaleImageUsecase: UpscaleImageUsecase,
    private val generateVariationsUsecase: GenerateVariationsUsecase
) : ViewModel() {

    private var _generateInteriorResp =
        MutableStateFlow<Response<genericResponseModel>>(Response.Success(null))

    val generateInteriorResp: StateFlow<Response<genericResponseModel>> = _generateInteriorResp

    private var _base64Response = MutableStateFlow<Response<base64Response>>(Response.Success(null))

    val base64Response: StateFlow<Response<base64Response>> = _base64Response

    private var _upscaleImage = MutableStateFlow<Response<upScaleResponse>>(Response.Success(null))
    val UpscaleImage: StateFlow<Response<upScaleResponse>> = _upscaleImage

    private var _imageVariationsResponse =
        MutableStateFlow<Response<genericResponseModel>>(Response.Success(null))

    val Imagevariations: StateFlow<Response<genericResponseModel>> = _imageVariationsResponse

    fun generateRoominterior(dtoObject: DTOObject) {
        viewModelScope.launch {
            generateRoomInteriorUsecase.invoke(dtoObject).collect() {
                _generateInteriorResp.value = it
            }
        }
    }

    fun uploadBase64(dtoBase64: DTOBase64) {
        viewModelScope.launch {
            base64ImageUploadingUsecae.invoke(dtoBase64).collect() {
                _base64Response.value = it
            }
        }
    }

    fun clearImageVariations() {
        _imageVariationsResponse.value = Response.Success(null)
    }

    fun generateVariations(dtoObject: DTOvariations) {
        viewModelScope.launch {
            generateVariationsUsecase.invoke(dtoObject).collect() {
                _imageVariationsResponse.value = it
            }
        }
    }

    fun upscaleImage(dtoObject: DTOUpScale) {
        viewModelScope.launch {
            upscaleImageUsecase.invoke(dtoObject).collect() {
                _upscaleImage.value = it
            }
        }
    }

    fun clearUpscale() {
        _upscaleImage.value = Response.Success(null)
    }

    fun clearImageToImage() {
        _generateInteriorResp.value = Response.Success(null)
    }

    fun clearBase64Resp() {
        _base64Response.value = Response.Success(null)
    }
}