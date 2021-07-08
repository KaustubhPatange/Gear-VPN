package com.kpstv.composetest.data.api

import com.kpstv.composetest.data.models.Location
import retrofit2.http.GET

interface IpApi {
  @GET("json")
  suspend fun fetch(): Location

  companion object {
    const val API = "http://ip-api.com/"
  }
}