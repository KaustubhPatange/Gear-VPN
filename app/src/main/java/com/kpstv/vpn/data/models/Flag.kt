package com.kpstv.vpn.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = Flag.TABLE_NAME)
data class Flag(
    @PrimaryKey
    val country: String,
    @Json(name = "flag")
    val flagUrl: String
) {
    companion object {
        const val TABLE_NAME = "table_flag"
    }
}