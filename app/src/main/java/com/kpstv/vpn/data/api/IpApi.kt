package com.kpstv.vpn.data.api

import com.kpstv.vpn.data.models.Location
import retrofit2.http.GET

interface IpApi {
  @GET("json")
  suspend fun fetch(): Location

  companion object {
    const val API = "http://ip-api.com/"
  }
}