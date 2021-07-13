package com.kpstv.vpn.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kpstv.vpn.data.db.localized.LocalDao
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.models.LocalConfiguration
import com.kpstv.vpn.data.models.VpnConfiguration

@Database(
  entities = [VpnConfiguration::class, LocalConfiguration::class],
  version = 1
)
abstract class VpnDatabase : RoomDatabase() {
  abstract fun getVPNDao(): VpnDao
  abstract fun getLocalDao(): LocalDao
}