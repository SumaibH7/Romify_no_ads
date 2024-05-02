package com.bluell.roomdecoration.interiordesign.data.repoistry

import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.domain.repo.UpdateGemsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.lang.Exception
import javax.inject.Inject

class UpdateGemsRespositryImpl @Inject constructor(private val firestore: FirebaseFirestore):
    UpdateGemsRepository {

    override fun updateGemsUseCase(map: Map<String, Any>): Flow<Response<Int>> = channelFlow {
        try{
            trySend(Response.Loading)
            firestore.collection("user").document(map["deviceID"].toString()).set(map).addOnSuccessListener {
                trySend(Response.Success(1))
            }
        }catch (e:Exception){
            trySend(Response.Error(e.printStackTrace().toString()))
        }
        awaitClose()
    }
}