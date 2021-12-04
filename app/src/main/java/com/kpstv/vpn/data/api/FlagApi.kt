package com.kpstv.vpn.data.api

import com.kpstv.vpn.data.models.Flag
import retrofit2.http.GET

interface FlagApi {

  @GET("flags.json")
  suspend fun fetch() : List<Flag>

  companion object {
    const val API = "https://raw.githubusercontent.com/KaustubhPatange/Gear-VPN/master/"
  }
}