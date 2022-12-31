package com.kpstv.vpn.di.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.kpstv.vpn.data.api.FlagApi
import com.kpstv.vpn.data.api.IpApi
import com.kpstv.vpn.data.api.PlanApi
import com.kpstv.vpn.data.api.VpnApi
import com.kpstv.vpn.data.db.database.FlagDatabase
import com.kpstv.vpn.data.db.database.VpnDatabase
import com.kpstv.vpn.data.db.database.VpnDatabaseMigrations
import com.kpstv.vpn.data.db.localized.FlagDao
import com.kpstv.vpn.data.db.localized.LocalDao
import com.kpstv.vpn.data.db.localized.VpnBookDao
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.extensions.utils.NetworkUtils
import dagger.Module
import dagger.Provides

@Module
class AppModule {

  @AppContext
  @Provides
  fun provideApplicationContext(application: Application): Context = application.applicationContext

  /* VPN Database */

  @Provides
  fun provideVPNDatabase(@AppContext context: Context): VpnDatabase {
    return Room.databaseBuilder(
      context,
      VpnDatabase::class.java,
      VpnDatabase.DB_NAME
    )
      .addMigrations(VpnDatabaseMigrations.MIGRATION_1_2, VpnDatabaseMigrations.MIGRATION_2_3)
      .fallbackToDestructiveMigration()
      .fallbackToDestructiveMigrationOnDowngrade()
      .build()
  }

  @Provides
  fun provideVpnDao(database: VpnDatabase): VpnDao = database.getVPNDao()

  @Provides
  fun provideVpnBookDao(database: VpnDatabase): VpnBookDao = database.getVPNBookDao()

  @Provides
  fun provideLocalDao(database: VpnDatabase): LocalDao = database.getLocalDao()

  /* Flag Database */

  @Provides
  fun provideFlagDatabase(@AppContext context: Context): FlagDatabase {
    return Room.databaseBuilder(
      context,
      FlagDatabase::class.java,
      FlagDatabase.DB_NAME
    )
      .fallbackToDestructiveMigration()
      .fallbackToDestructiveMigrationOnDowngrade()
      .build()
  }

  @Provides
  fun provideFlagDao(database: FlagDatabase): FlagDao = database.getFlagDao()

  /* Networking */

  @Provides
  fun provideNetworkUtils(@AppContext context: Context): NetworkUtils = NetworkUtils(context)

  @Provides
  fun provideIpApi(networkUtils: NetworkUtils): IpApi {
    return networkUtils.getRetrofitBuilder()
      .baseUrl(IpApi.API)
      .build()
      .create(IpApi::class.java)
  }

  @Provides
  fun provideFlagApi(networkUtils: NetworkUtils): FlagApi {
    return networkUtils.getRetrofitBuilder()
      .baseUrl(FlagApi.API)
      .build()
      .create(FlagApi::class.java)
  }

  @Provides
  fun provideVpnApi(networkUtils: NetworkUtils): VpnApi {
    return networkUtils.getRetrofitBuilder()
      .baseUrl(VpnApi.API)
      .build()
      .create(VpnApi::class.java)
  }

  @Provides
  fun providePlanApi(networkUtils: NetworkUtils): PlanApi {
    return networkUtils.getRetrofitBuilder()
      .baseUrl(PlanApi.API)
      .build()
      .create(PlanApi::class.java)
  }
}