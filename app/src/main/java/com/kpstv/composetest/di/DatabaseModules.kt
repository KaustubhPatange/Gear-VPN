package com.kpstv.composetest.di

import android.content.Context
import androidx.room.Room
import com.kpstv.composetest.data.db.database.VpnDatabase
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
      .fallbackToDestructiveMigration()
      .fallbackToDestructiveMigrationOnDowngrade()
      .build()
  }

  @[Singleton Provides]
  fun provideVpnDao(database: VpnDatabase) = database.getVPNDao()

  @[Singleton Provides]
  fun provideLocalDao(database: VpnDatabase) = database.getLocalDao()
}