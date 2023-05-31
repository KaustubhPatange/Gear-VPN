package com.kpstv.vpn.extensions.interceptor

import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.data.api.PlanApi
import com.kpstv.vpn.data.api.VpnApi
import com.kpstv.vpn.ui.helpers.Device
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URL

class VpnInterceptor : Interceptor {
  private val url = URL(VpnApi.API)
  private val planUrl = URL(PlanApi.API)

  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    if (request.url.host == planUrl.host) {
      request = request.newBuilder()
        .addHeader(CLIENT_TYPE, "android")
        .addHeader(PACKAGE_NAME, BuildConfig.APPLICATION_ID)
        .addHeader(VERSION, BuildConfig.VERSION_CODE.toString())
        .addHeader(UNIQUE_ID, "ad-${Device.deviceId}")
        .build()
    } else if (request.url.host == url.host) {
      request = request.newBuilder()
        .addHeader(RAPID_API_KEY, BuildConfig.GEAR_KEY)
        .addHeader(RAPID_API_HOST, BuildConfig.GEAR_HOST)
        .build()
    }
    return chain.proceed(request)
  }

  companion object {
    private const val CLIENT_TYPE = "client-type"
    private const val PACKAGE_NAME = "application-id"
    private const val VERSION = "client-version"
    private const val UNIQUE_ID = "user-id"
    private const val RAPID_API_KEY = "X-RapidAPI-Key"
    private const val RAPID_API_HOST = "X-RapidAPI-Host"
  }
}