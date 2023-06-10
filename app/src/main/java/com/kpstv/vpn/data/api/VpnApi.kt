package com.kpstv.vpn.data.api

import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.data.models.VpnResponse
import okhttp3.CacheControl
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface VpnApi {
    @GET("all")
    suspend fun getVpnConfigs(
        @Header("Cache-Control") cacheControl: CacheControl = CACHE_NORMAL,
        @Query("limit") limit: Int
    ): VpnResponse

    companion object {
        const val API: String = BuildConfig.GEAR_API2

        val CACHE_NORMAL = CacheControl.Builder().build()
        val FORCE_NETWORK = CacheControl.FORCE_NETWORK
    }
}