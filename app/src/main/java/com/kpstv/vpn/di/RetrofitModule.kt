package com.kpstv.vpn.di

import com.kpstv.vpn.data.api.IpApi
import com.kpstv.vpn.extensions.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

  @[Singleton Provides]
  fun provideIpApi(
    networkUtils: NetworkUtils
  ): IpApi {
    return networkUtils.getRetrofitBuilder()
      .baseUrl(IpApi.API)
      .build()
      .create(IpApi::class.java)
  }

}