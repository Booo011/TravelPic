package com.example.travelpic.data

data class Picture(val Date: String,
                   val Model: String,
                   val Latitude: String,
                   val Longitude: String,
                   var LikeCount: Int = 0,
                   val LocationTag:String = "",
                   val Memo:String = "",
                   val imageUrl: String = "")
{
    constructor() : this("-", "-", "-", "-")
}