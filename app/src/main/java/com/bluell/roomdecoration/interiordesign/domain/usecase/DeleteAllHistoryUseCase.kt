package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.domain.repo.DeleteAllHistory
import javax.inject.Inject

class DeleteAllHistoryUseCase@Inject constructor(private val fetchDataRepository: DeleteAllHistory) {
    suspend operator fun invoke(includeAll:Int,point:String) {
        fetchDataRepository.DeleteAll(includeAll,point)
    }
}