package com.kpstv.vpn.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.vpn.ui.helpers.VpnConfig

@Entity(tableName = "table_local_config")
data class LocalConfiguration(
  val profileName: String,
  val userName: String?,
  val password: String?,
  val config: String
) {
  @PrimaryKey(autoGenerate = true)
  var id: Int = 0
}

fun LocalConfiguration.asVpnConfiguration() : VpnConfig {
  return VpnConfig.createEmpty().copy(
    country = "Custom ($profileName)",
    username = userName,
    password = password,
    config = config,
  )
}