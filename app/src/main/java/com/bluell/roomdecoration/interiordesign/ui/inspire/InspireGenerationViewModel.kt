package com.bluell.roomdecoration.interiordesign.ui.inspire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.usecase.Base64ImageUploadingUsecae
import com.bluell.roomdecoration.interiordesign.domain.usecase.TextToImgUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class InspireGenerationViewModel @Inject constructor(
    private val base64ImageUploadingUsecae: Base64ImageUploadingUsecae,
    private val textToImgUseCase: TextToImgUseCase
) : ViewModel() {

    private var _base64Response = MutableStateFlow<Response<base64Response>>(Response.Success(null))

    val base64Response: StateFlow<Response<base64Response>> = _base64Response

    private var _textToImgResponse =
        MutableStateFlow<Response<genericResponseModel>>(Response.Success(null))

    val textToImgResp: StateFlow<Response<genericResponseModel>> = _textToImgResponse

    fun uploadBase64(dtoBase64: DTOBase64) {
        viewModelScope.launch {
            base64ImageUploadingUsecae.invoke(dtoBase64).collect() {
                _base64Response.value = it
            }
        }
    }

    fun clearTextToImage() {
        _textToImgResponse.value = Response.Success(null)
    }

    fun generateTextToImg(dtoObject: DTOObjectGenerate) {
        viewModelScope.launch {
            textToImgUseCase.invoke(dtoObject).collect() {
                _textToImgResponse.value = it
            }
        }
    }
}