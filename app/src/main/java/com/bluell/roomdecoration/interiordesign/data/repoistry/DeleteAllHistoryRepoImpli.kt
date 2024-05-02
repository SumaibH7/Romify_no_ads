package com.bluell.roomdecoration.interiordesign.data.repoistry

import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.remote.EndPointsInterface
import com.bluell.roomdecoration.interiordesign.domain.repo.DeleteAllHistory
import javax.inject.Inject


class DeleteAllHistoryRepoImpli@Inject constructor(
    private val endPointsInterface: EndPointsInterface,
    private val appDatabase: AppDatabase
) : DeleteAllHistory {
    override suspend fun DeleteAll(includeAll:Int,point:String) {
        appDatabase.genericResponseDao()?.deleteAllCreations(includeAll, point)
    }
}