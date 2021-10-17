package com.kpstv.vpn.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kpstv.vpn.data.db.localized.LocalDao
import com.kpstv.vpn.data.db.localized.VpnBookDao
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.models.LocalConfiguration
import com.kpstv.vpn.data.models.VpnConfiguration

@Database(
  entities = [VpnConfiguration::class, LocalConfiguration::class],
  version = 2,
  exportSchema = false
)
abstract class VpnDatabase : RoomDatabase() {
  abstract fun getVPNDao(): VpnDao
  abstract fun getLocalDao(): LocalDao
  abstract fun getVPNBookDao(): VpnBookDao
}

object VpnDatabaseMigrations {
  val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      // I don't know why RENAME COLUMN works otherwise this could've been so easy.
      database.execSQL("DROP TABLE table_vpnconfigs")
      database.execSQL("""
        CREATE TABLE table_vpnconfigs (
            country TEXT NOT NULL,
            sessions TEXT NOT NULL,
            ip TEXT NOT NULL,
            speed TEXT NOT NULL,
            upTime TEXT NOT NULL,
            score INTEGER NOT NULL,
            countryFlagUrl TEXT NOT NULL,
            password TEXT NOT NULL,
            configTCP TEXT,
            configUDP TEXT,
            expireTime INTEGER NOT NULL,
            premium INTEGER NOT NULL,
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL
        )
      """.trimIndent())

      database.execSQL("DROP TABLE table_local_config")
      database.execSQL("""
        CREATE TABLE table_local_config (
            profileName TEXT NOT NULL,
            userName TEXT,
            password TEXT,
            config TEXT NOT NULL,
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT
        )
      """.trimIndent())
    }
  }
}