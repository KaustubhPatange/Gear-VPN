package com.kpstv.composetest.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_local_config")
data class LocalConfiguration(
  val profileName: String,
  val userName: String,
  val password: String,
  val config: String
) {
  @PrimaryKey(autoGenerate = true)
  var id: Int = 0
}

fun LocalConfiguration.asVpnConfiguration() : VpnConfiguration {
  return VpnConfiguration.createEmpty().copy(
    country = "Custom ($profileName)",
    username = userName,
    password = password,
    config = config
  )
}