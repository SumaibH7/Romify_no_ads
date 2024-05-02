package com.bluell.roomdecoration.interiordesign.data.repoistry

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.remote.EndPointsInterface
import com.bluell.roomdecoration.interiordesign.domain.repo.FetchDataRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.lang.Exception
import javax.inject.Inject

class FetchDataRepositoryImpl @Inject constructor(
    private val endPointsInterface: EndPointsInterface,
    private val appDatabase: AppDatabase
) : FetchDataRepository {

    override fun fetchAllCreations(includeAll:Int,point:String): Flow<Response<List<genericResponseModel>>> = channelFlow {
        try {

            trySend(Response.Loading)

            val creations=appDatabase.genericResponseDao()?.getAllCreations(includeAll,point)
            if (creations?.isNotEmpty()!!){
                trySend(Response.Success(creations))
            }else{
                trySend(Response.Error("No Data found"))
            }

        }
        catch (e:Exception){
            trySend(Response.Error("Unexpected error ${e.message}"))
        }
        awaitClose()
    }
}