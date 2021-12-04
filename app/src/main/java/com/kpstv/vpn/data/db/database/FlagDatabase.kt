package com.kpstv.vpn.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kpstv.vpn.data.db.localized.FlagDao
import com.kpstv.vpn.data.models.Flag

@Database(
  entities = [Flag::class],
  version = 1,
  exportSchema = false
)
abstract class FlagDatabase : RoomDatabase() {
  abstract fun getFlagDao() : FlagDao

  companion object { const val DB_NAME = "flag.db" }
}