package com.kpstv.vpn.di

import android.content.Context
import androidx.room.Room
import com.kpstv.vpn.data.db.database.VpnDatabase
import com.kpstv.vpn.data.db.database.VpnDatabaseMigrations
import com.kpstv.vpn.data.db.localized.LocalDao
import com.kpstv.vpn.data.db.localized.VpnDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VpnModule {

  @[Singleton Provides]
  fun provideDatabase(
    @ApplicationContext context: Context
  ): VpnDatabase {
    return Room.databaseBuilder(
      context,
      VpnDatabase::class.java,
      "vpn.db"
    )
      .addMigrations(VpnDatabaseMigrations.MIGRATION_1_2)
      .fallbackToDestructiveMigration()
      .fallbackToDestructiveMigrationOnDowngrade()
      .build()
  }

  @[Singleton Provides]
  fun provideVpnDao(database: VpnDatabase): VpnDao = database.getVPNDao()

  @[Singleton Provides]
  fun provideLocalDao(database: VpnDatabase): LocalDao = database.getLocalDao()
}