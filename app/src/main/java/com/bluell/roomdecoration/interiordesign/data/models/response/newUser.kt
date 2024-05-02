package com.bluell.roomdecoration.interiordesign.data.models.response

/**
 * ***********************************************************************************************
 * *                                                                                             *
 * *          _   _      _                                                                       *
 * *         | | | |    (_)                                                                      *
 * *         | |_| | ___ _ _ __   __ _  __ _  ___                                                *
 * *         |  _  |/ _ \ | '_ \ / _` |/ _` |/ _ \                                               *
 * *         | | | |  __/ | | | | (_| | (_| |  __/                                               *
 * *                                                                                             *
 * *         Created by Hamza ch on 10/6/2023.                                                      *
 * *         GAMICAN                                                                          *
 * *                                                                                             *
 * ***********************************************************************************************
 */
data class newUser (
    var deviceID:String?="",
    var gemsFromSub:Int? = 0,
    var otherGems :Int? = 0,
    var purchaseToken:String?="",
    var type:String? = "",
    var totalGems:Int? = 10,
    var usedGems:Int? = 0,
    var updateDate:String?="",
    var isDailyClaimed:Boolean? = false,
    var expiry:String? = ""
)