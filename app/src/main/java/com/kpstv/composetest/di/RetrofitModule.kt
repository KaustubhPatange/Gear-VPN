package com.kpstv.composetest.di

import com.kpstv.composetest.data.api.IpApi
import com.kpstv.composetest.extensions.utils.NetworkUtils
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