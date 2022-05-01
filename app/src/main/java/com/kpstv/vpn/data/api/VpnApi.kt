package com.kpstv.vpn.data.api

import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.data.models.VpnConfiguration
import retrofit2.http.GET
import retrofit2.http.Query

interface VpnApi {

  @GET("api/gear/duoserver")
  suspend fun getDuoServers(@Query("secret") secret: String = BuildConfig.GEAR_DUO_SECRET): List<VpnConfiguration>

  companion object {
    const val API = "https://gear-vpn-api.vercel.app/"
  }
}