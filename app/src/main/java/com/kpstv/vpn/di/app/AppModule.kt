package com.kpstv.vpn.di.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.kpstv.vpn.data.api.IpApi
import com.kpstv.vpn.data.db.database.VpnDatabase
import com.kpstv.vpn.data.db.database.VpnDatabaseMigrations
import com.kpstv.vpn.data.db.localized.LocalDao
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.db.repository.VpnRepository
import com.kpstv.vpn.extensions.utils.NetworkUtils
import dagger.Module
import dagger.Provides

@Module
class AppModule {

  @AppContext
  @Provides
  fun provideApplicationContext(application: Application): Context = application.applicationContext

  /* Database */

  @Provides
  fun provideDatabase(@AppContext context: Context): VpnDatabase {
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

  @Provides
  fun provideVpnDao(database: VpnDatabase): VpnDao = database.getVPNDao()

  @Provides
  fun provideLocalDao(database: VpnDatabase): LocalDao = database.getLocalDao()

  @Provides
  fun provideVpnRepository(vpnDao: VpnDao, networkUtils: NetworkUtils): VpnRepository =
    VpnRepository(vpnDao, networkUtils)

  /* Networking */

  @Provides
  fun provideNetworkUtils(): NetworkUtils = NetworkUtils()

  @Provides
  fun provideIpApi(networkUtils: NetworkUtils): IpApi {
    return networkUtils.getRetrofitBuilder()
      .baseUrl(IpApi.API)
      .build()
      .create(IpApi::class.java)
  }
}