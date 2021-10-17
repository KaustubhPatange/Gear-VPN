package com.kpstv.vpn.data.db.localized

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.logging.Logger

@Dao
interface VpnBookDao {

  // safe update password only when needed.
  @Transaction
  suspend fun safeUpdate(username: String, password: String) {
    val config = getFirstOrNull(username) ?: return
    if (config.password != password && config.password.isNotEmpty()) {
      Logger.d("Updating password for vpnbook.com configurations")
      updatePassword(username, password)
    }
  }

  @Query("update table_vpnconfigs set password = :password where username = :username")
  suspend fun updatePassword(username: String, password: String)

  @Query("select * from table_vpnconfigs where username = :username limit 1")
  suspend fun getFirstOrNull(username: String) : VpnConfiguration?
}