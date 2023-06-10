package com.kpstv.vpn.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VpnResponse(
    val data: List<VpnConfiguration>,
    val total: Int,
    val nextSegment: String = "",
    val prevSegment: String = ""
)