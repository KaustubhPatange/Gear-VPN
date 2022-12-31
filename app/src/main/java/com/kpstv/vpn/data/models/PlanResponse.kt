package com.kpstv.vpn.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanResponse(
  val data: List<Plan>
)

@JsonClass(generateAdapter = true)
data class Plan(
  val sku: String,
  val name: String,
  val billingCycleMonth: Int
)