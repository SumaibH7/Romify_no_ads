package com.bluell.roomdecoration.interiordesign.ui.history.inspire

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.domain.usecase.DeleteAllHistoryUseCase
import com.bluell.roomdecoration.interiordesign.domain.usecase.GetAllCreationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspireHistoryViewmodel @Inject constructor(
    private val getAllCreationsUseCase: GetAllCreationsUseCase,
    private val deleteAllHistoryUseCase: DeleteAllHistoryUseCase
) : ViewModel() {
    private var _allCreations = MutableLiveData<Response<List<genericResponseModel>>>(
        Response.Success(
            emptyList()
        )
    )
    val allCreations: LiveData<Response<List<genericResponseModel>>> = _allCreations


    fun getAllCreations(includeAll:Int,point:String){
        viewModelScope.launch {
            getAllCreationsUseCase.invoke(includeAll,point).collect(){
                _allCreations.value=it
            }
        }
    }

//    fun deleteAll() {
//        viewModelScope.launch {
//            deleteAllHistoryUseCase()
//        }
//    }

    fun clearAllCreations(){
        _allCreations.value= Response.Success(emptyList())
    }
}