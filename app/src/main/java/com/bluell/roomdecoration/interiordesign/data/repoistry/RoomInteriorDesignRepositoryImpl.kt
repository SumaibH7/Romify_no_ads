package com.bluell.roomdecoration.interiordesign.data.repoistry

import android.util.Log
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOUpScale
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOvariations
import com.bluell.roomdecoration.interiordesign.data.models.response.base64Response
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.upScaleResponse
import com.bluell.roomdecoration.interiordesign.data.remote.EndPointsInterface
import com.bluell.roomdecoration.interiordesign.domain.repo.InteriorDesignRepoistry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RoomInteriorDesignRepositoryImpl @Inject constructor(
    private val endPointsInterface: EndPointsInterface,
) : InteriorDesignRepoistry {

    override fun uploadBase64Image(dtoBase64: DTOBase64): Flow<Response<base64Response>> = channelFlow {
        try {
            trySend(Response.Loading)
            val requestImage = RequestBody.create("image/*".toMediaTypeOrNull(), dtoBase64.image)
            val imagePart = MultipartBody.Part.createFormData("image", System.currentTimeMillis().toString()+dtoBase64.image.name, requestImage)
            val resp = endPointsInterface.uploadImage(imagePart)
            Log.e(TAG, "uploadBase64Image: $resp")
//            when (resp.status.lowercase()) {

//                "success" -> {
                    trySend(Response.Success(resp))
//                }

//                "error" -> {
//                    Log.e(TAG, "uploadBase64Image: "+resp )
//                    trySend(Response.Error(resp.messege.toString()))
//                }
//            }
        } catch (e: Exception) {
            if (e.message?.contains("Connection reset") == true){
                trySend(Response.Error("Unstable Internet Connection!"))
            }else{
                trySend(Response.Error("Unexpected Error Occurred ${e.message}"))
            }
            Log.e(TAG, "uploadBase64Image: "+e.message )
            e.printStackTrace()
        }
        awaitClose()
    }

    override fun GenerateVariations(dtoObject: DTOvariations): Flow<Response<genericResponseModel>> =
        channelFlow {

            try {
                trySend(Response.Loading)
                dtoObject.negative_prompt= Constants.NEGATIVE_PROMPT
                val resp = endPointsInterface.createVariations(dtoObject)
                Log.e(TAG, "GenerateTextToImage: $resp")

                when (resp.status.lowercase()) {
                    "failed"->{
                        trySend(Response.Error(resp.message.toString()))
                    }
                    "success" -> {
                        trySend(Response.Success(resp))
                    }

                    "processing" -> {
                        trySend(Response.Success(resp))
                    }

                    "error" -> {
                        trySend(Response.Error(resp.message.toString()))
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                if (e.message?.contains("Connection reset") == true){
                    trySend(Response.Error("Unstable Internet Connection!"))
                }else{
                    trySend(Response.Error("Unexpected Error Occurred ${e.message}"))
                }
            }

            awaitClose()
        }

    override fun GenerateTextToImage(dtoObject: DTOObjectGenerate): Flow<Response<genericResponseModel>> =
        channelFlow {

            try {
                trySend(Response.Loading)
                dtoObject.negative_prompt="${Constants.NEGATIVE_PROMPT} ${dtoObject.exact_Nprompt}"
                val resp = endPointsInterface.createTextToImage(dtoObject)
                Log.e(TAG, "GenerateTextToImage: $resp")

                when (resp.status.lowercase()) {
                    "failed"->{
                        trySend(Response.Error(resp.message.toString()))
                    }
                    "success" -> {
                        trySend(Response.Success(resp))
                    }

                    "processing" -> {
                        trySend(Response.Success(resp))
                    }

                    "error" -> {
                        trySend(Response.Error(resp.message.toString()))
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                if (e.message?.contains("Connection reset") == true){
                    trySend(Response.Error("Unstable Internet Connection!"))
                }else{
                    trySend(Response.Error("Unexpected Error Occurred ${e.message}"))
                }
            }

            awaitClose()
        }

    override fun GenerateInPaint(dtoObject: DTOObjectGenerate): Flow<Response<genericResponseModel>> =
        channelFlow {
            try {
                trySend(Response.Loading)
                dtoObject.negative_prompt="${Constants.NEGATIVE_PROMPT} ${dtoObject.exact_Nprompt}"
                val resp = endPointsInterface.InPaint(dtoObject = dtoObject)
                if (resp.id == null){
                    resp.id = System.currentTimeMillis().toInt()
                }
                when (resp.status.lowercase()) {
                    "failed"->{
                        trySend(Response.Error(resp.message.toString()))
                    }
                    "success" -> {
                        trySend(Response.Success(resp))
                    }

                    "processing" -> {
                        trySend(Response.Success(resp))
                    }

                    "error" -> {
                        trySend(Response.Error(resp.message.toString()))
                    }
                }
            } catch (e: java.lang.Exception) {
                if (e.message?.contains("Connection reset") == true){
                    trySend(Response.Error("Unstable Internet Connection!"))
                }else{
                    trySend(Response.Error("Unexpected Error Occurred ${e.message}"))
                }
            }
            awaitClose()
        }

    override fun GenerateRoomInterior(dtoObject: DTOObject): Flow<Response<genericResponseModel>> =
        channelFlow {
            try {
                trySend(Response.Loading)
                dtoObject.negative_prompt= Constants.NEGATIVE_PROMPT
                val resp = endPointsInterface.ImageToImage(dtoObject = dtoObject)
                Log.e("TAG", "GenerateRoomInterior: "+resp.status )
                Log.e("TAG", "GenerateRoomInterior: $resp")
                when (resp.status.lowercase()) {
                    "failed"->{
                        trySend(Response.Error(resp.message.toString()))
                    }
                    "success" -> {
                        trySend(Response.Success(resp))
                    }

                    "processing" -> {
                        resp.output = resp.future_links
                        trySend(Response.Processing(resp))
                    }

                    "error" -> {
                        trySend(Response.Error(resp.message.toString()))
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("Connection reset") == true){
                    trySend(Response.Error("Unstable Internet Connection!"))
                }else{
                    trySend(Response.Error("Unexpected Error Occurred ${e.message}"))
                }
            }
            awaitClose()
        }

    override fun UpScaleImage(dtoUpScale: DTOUpScale): Flow<Response<upScaleResponse>> =
        channelFlow {
            try {
                trySend(Response.Loading)
                val resp = endPointsInterface.UpScaleImage(dtoUpScale)
                when (resp.status.lowercase()) {
                    "failed"->{
                        trySend(Response.Error(resp.message.toString()))
                    }
                    "success" -> {
                        trySend(Response.Success(resp))
                    }

                    "processing" -> {
                        trySend(Response.Processing(resp))

                    }

                    "error" -> {
                        trySend(Response.Error(resp.message.toString()))
                    }
                }
            } catch (e: java.lang.Exception) {
                if (e.message?.contains("Connection reset") == true){
                    trySend(Response.Error("Unstable Internet Connection!"))
                }else{
                    trySend(Response.Error("Unexpected Error Occurred ${e.message}"))
                }
            }
            awaitClose()
        }

}