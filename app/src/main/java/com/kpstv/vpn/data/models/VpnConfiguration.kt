package com.kpstv.vpn.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.vpn.extensions.utils.DateUtils
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "table_vpnconfigs")
@Parcelize
@JsonClass(generateAdapter = true)
data class VpnConfiguration(
  val country: String,
  val countryFlagUrl: String,
  val ip: String,
  /**
   * as "x sessions" for VPN Gate
   */
  val sessions: String,
  /**
   * as "x days/hours/mins" for VPN Gate
   */
  val upTime: String,
  val speed: String,
  val configTCP: String?,
  val configUDP: String?,
  val score: Long,
  val expireTime: Long,
  val username: String,
  val password: String,
  val premium: Boolean = false
) : Parcelable {
  @PrimaryKey(autoGenerate = true)
  var id: Int = 0

  fun isExpired(): Boolean = isExpired(expireTime)

  companion object {
    fun createEmpty(): VpnConfiguration = VpnConfiguration(
      country = "Unknown",
      ip = "Unknown",
      countryFlagUrl = "",
      sessions = "",
      upTime = "",
      speed = "",
      configTCP = null,
      configUDP = null,
      username = "vpn",
      password = "vpn",
      score = 0,
      expireTime = -1L
    )

    fun isExpired(expireTime: Long): Boolean {
      if (expireTime == -1L) return false
      val offsetDate = DateUtils.format(Calendar.getInstance().time).toLong()
      return offsetDate >= expireTime
    }
  }
}