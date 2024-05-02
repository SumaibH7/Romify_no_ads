package com.bluell.roomdecoration.interiordesign.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.usecase.DeleteFavHistoryUseCase
import com.bluell.roomdecoration.interiordesign.domain.usecase.GetFavCreationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavHistoryViewModel @Inject constructor(
    private val getFavCreationsUseCase: GetFavCreationsUseCase,
    private val deleteFavHistoryUseCase: DeleteFavHistoryUseCase
) : ViewModel() {
    private var _favCreations = MutableLiveData<Response<List<genericResponseModel>>>(
        Response.Success(
            emptyList()
        )
    )
    val favCreations:LiveData<Response<List<genericResponseModel>>> = _favCreations

    fun deleteAll() {
        viewModelScope.launch {
            deleteFavHistoryUseCase()
        }
    }
    fun getAllCreations(point:String){
        viewModelScope.launch {
            getFavCreationsUseCase.invoke(point).collect(){
                _favCreations.value=it
            }
        }
    }

    fun clearAllCreations(){
        _favCreations.value= Response.Success(emptyList())
    }
}