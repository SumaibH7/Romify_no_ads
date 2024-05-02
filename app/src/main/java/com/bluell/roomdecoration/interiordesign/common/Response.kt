package com.bluell.roomdecoration.interiordesign.common

sealed class Response<out T> {
    object Loading : Response<Nothing>()


    data class Processing<out T>(val data: T?) : Response<T>()

    data class Success<out T>(val data: T?) : Response<T>()

    data class Error(val message: String) : Response<Nothing>()
}
