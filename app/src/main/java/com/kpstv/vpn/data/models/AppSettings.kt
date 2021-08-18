package com.kpstv.vpn.data.models

import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.ConverterType

@AutoGenerateConverter(using = ConverterType.MOSHI)
data class AppSettings(
    val vpnbook: Vpnbook
) {
    data class Vpnbook(
        val password: String,
        val username: String
    )
}