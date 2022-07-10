package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class data(
    @SerializedName("id")
    val id: Int,
    @SerializedName("word")
    val word: String
)
data class data2(
    @SerializedName("DownloadUri")
    val DownloadUri: String,
    @SerializedName("DownloadPassword")
    val DownloadPassword: String,
    @SerializedName("AppVersion")
    val AppVersion: String
)
data class  Words(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data:List<data>,
    val data2:List<data2>
)
