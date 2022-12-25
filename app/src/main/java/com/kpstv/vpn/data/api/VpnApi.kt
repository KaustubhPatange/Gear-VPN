package com.kpstv.vpn.data.api

import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.data.models.VpnResponse
import okhttp3.CacheControl
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface VpnApi {
    @GET("v1.0/vpn-configs/all")
    suspend fun getVpnConfigs(
        @Header("Cache-Control") cacheControl: CacheControl = CACHE_NORMAL,
        @Query("lastKey") lastKey: String = "",
        @Query("limit") limit: Int
    ): VpnResponse

    companion object {
        const val API: String = BuildConfig.GEAR_API

        val CACHE_NORMAL = CacheControl.Builder().build()
        val FORCE_NETWORK = CacheControl.FORCE_NETWORK
    }
}