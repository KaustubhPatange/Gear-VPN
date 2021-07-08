package com.kpstv.composetest.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kpstv.composetest.data.db.localized.VpnDao
import com.kpstv.composetest.data.models.VpnConfiguration

@Database(
  entities = [VpnConfiguration::class],
  version = 1
)
abstract class VpnDatabase : RoomDatabase() {
  abstract fun getVPNDao(): VpnDao
}