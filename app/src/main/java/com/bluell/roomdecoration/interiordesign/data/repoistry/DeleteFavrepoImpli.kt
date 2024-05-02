package com.bluell.roomdecoration.interiordesign.data.repoistry

import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.remote.EndPointsInterface
import com.bluell.roomdecoration.interiordesign.domain.repo.DeletefavoriteHistory
import javax.inject.Inject


class DeleteFavrepoImpli@Inject constructor(
    private val endPointsInterface: EndPointsInterface,
    private val appDatabase: AppDatabase
) : DeletefavoriteHistory {
    override suspend fun Deletefav() {
        appDatabase.genericResponseDao()?.updateFavoritesToFalse()
    }
}