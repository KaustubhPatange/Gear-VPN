package com.kpstv.vpn.data.models

import com.kpstv.vpn.extensions.custom.AbstractMoshiConverter
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppSettings(
    val vpnbook: Vpnbook
) {
    @JsonClass(generateAdapter = true)
    data class Vpnbook(
        val password: String,
        val username: String
    )

    object Converter : AbstractMoshiConverter<AppSettings>(AppSettings::class)
}