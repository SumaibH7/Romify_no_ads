package com.bluell.roomdecoration.interiordesign.domain.usecase

import com.bluell.roomdecoration.interiordesign.domain.repo.DeletefavoriteHistory
import javax.inject.Inject


class DeleteFavHistoryUseCase@Inject constructor(private val fetchDataRepository: DeletefavoriteHistory) {
    suspend operator fun invoke() {
        fetchDataRepository.Deletefav()
    }
}