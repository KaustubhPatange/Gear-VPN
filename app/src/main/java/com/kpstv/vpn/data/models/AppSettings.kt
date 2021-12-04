package com.kpstv.vpn.data.models

import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.ConverterType
import com.squareup.moshi.JsonClass

@AutoGenerateConverter(using = ConverterType.MOSHI)
@JsonClass(generateAdapter = true)
data class AppSettings(
    val vpnbook: Vpnbook
) {
    @JsonClass(generateAdapter = true)
    data class Vpnbook(
        val password: String,
        val username: String
    )
}