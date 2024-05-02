package com.bluell.roomdecoration.interiordesign.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.domain.usecase.Base64ImageUploadingUsecae
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InPaintViewModel @Inject constructor(
    private val base64UploadImageUseCase: Base64ImageUploadingUsecae
) : ViewModel() {

    private var _base64Response = MutableStateFlow<Response<base64Response>>(Response.Success(null))

    val base64Response: StateFlow<Response<base64Response>> = _base64Response

    fun clearBase64Resp() {
        _base64Response.value = Response.Success(null)
    }

    fun uploadBase64(dtoBase64: DTOBase64) {
        viewModelScope.launch {
            base64UploadImageUseCase.invoke(dtoBase64).collect() {
                _base64Response.value = it
            }
        }
    }


}