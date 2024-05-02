package com.bluell.roomdecoration.interiordesign.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.usecase.Base64ImageUploadingUsecae
import com.example.artbotic.domain.usecase.InPaintUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRoomInteriorViewModel @Inject constructor(
    private val base64ImageUploadingUsecae: Base64ImageUploadingUsecae,
    private val inPaintUseCase: InPaintUseCase,
) : ViewModel() {
    private var _base64Response = MutableStateFlow<Response<base64Response>>(Response.Success(null))

    val base64Response: StateFlow<Response<base64Response>> = _base64Response

    private var _inPaintImage =
        MutableStateFlow<Response<genericResponseModel>>(Response.Success(null))
    val inPaintImage: StateFlow<Response<genericResponseModel>> = _inPaintImage

    fun uploadBase64(dtoBase64: DTOBase64) {
        viewModelScope.launch {
            base64ImageUploadingUsecae.invoke(dtoBase64).collect() {
                _base64Response.value = it
            }
        }
    }

    fun clearInPaint() {
        _inPaintImage.value = Response.Success(null)
    }

    fun generateInpaint(dtoObject: DTOObjectGenerate) {
        viewModelScope.launch {
            inPaintUseCase.invoke(dtoObject).collect() {
                _inPaintImage.value = it
            }
        }
    }

    fun clearBase64Resp() {
        _base64Response.value = Response.Success(null)
    }
}