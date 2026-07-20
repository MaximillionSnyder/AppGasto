package com.example.appgasto.data.updater

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    val body: String?,
    val assets: List<GitHubAsset>?
)

data class GitHubAsset(
    val name: String,
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String
)
