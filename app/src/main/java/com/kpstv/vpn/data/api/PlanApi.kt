package com.kpstv.vpn.data.api

import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.data.models.PlanResponse
import retrofit2.http.GET

interface PlanApi {
  @GET("v1.0/plans")
  suspend fun fetchPlans(): PlanResponse

  companion object {
    val API = BuildConfig.GEAR_API
  }
}