package com.bluell.roomdecoration.interiordesign.domain.repo

interface DeleteAllHistory {
    suspend fun DeleteAll(includeAll:Int,point:String)
}