package com.kpstv.vpn.data.helpers

import androidx.annotation.WorkerThread
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.resume
import kotlin.math.min

class OpenApiParser(private val networkUtils: NetworkUtils) {

  companion object {
    private val ipRegex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex()
  }

  // Suspend in a way to work like a callback. This should make bridge between `emit`ion & direct snapshot seamless.
  suspend fun parse(onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {}, onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit) {
    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    val vpnResponse = networkUtils.simpleGetRequest("https://www.vpngate.net/en")
    if (vpnResponse.isSuccessful) {

      val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
      val expiredTime = DateUtils.format(offsetDateTime).toLong()

      val body = vpnResponse.body?.string()
      vpnResponse.close() // close Stream

      val doc = Jsoup.parse(body)

      val table = doc.getElementById("vpngate_inner_contents_td").children().findLast { it.id() == "vg_hosts_table_id" }?.child(0)
        ?: run {
          onComplete.invoke(formatConfigurations(vpnConfigurations))
          return
        }
      vpnConfigurations.clear()

      for (i in 1 until table.childrenSize()) {

        val tr = table.child(i)

        if (tr.getElementsByClass("vg_table_header").size == 0) {
          val imageUrl = tr.child(0).child(0).attr("src").replace("../", "https://www.vpngate.net/")
          val country = tr.child(0).text()

          if (country == "Reserved") continue

          // no more than 3 countries....
          if (vpnConfigurations.count { it.country == formatCountry(country) } == 3) continue

          val ip = ipRegex.find(tr.child(1).html())?.value ?: ""

          val sessions = tr.child(2).child(0).child(0).text()
          val uptime = tr.child(2).child(2).text()

          val speed = tr.child(3).child(0).child(0).text()

          if (speed == "0.00 Mbps") continue

          val ovpnConfigElement = tr.child(6)
          if (ovpnConfigElement.childrenSize() == 0) continue

          val score = tr.child(tr.childrenSize() - 1).child(0).child(0).text()
            .replace(",","").toLong()

          val configUrl = "https://www.vpngate.net/en/" + ovpnConfigElement.child(0).attr("href")

          val configResponse = networkUtils.simpleGetRequest(configUrl)
          if (configResponse.isSuccessful) {
            val configBody = configResponse.body?.string()
            configResponse.close() // Always close stream

            val hrefElements = Jsoup.parse(configBody).getElementsByAttribute("href")
            val ovpnConfig =
              hrefElements.find { it.attr("href").contains(".ovpn") }?.attr("href") ?: continue

            val configDataResponse = networkUtils.simpleGetRequest("https://www.vpngate.net/$ovpnConfig")
            if (configDataResponse.isSuccessful) {
              val data = configDataResponse.body?.string() ?: continue
              configDataResponse.close() // Always close stream
              val vpnConfig = VpnConfiguration(
                formatCountry(country), imageUrl, ip, sessions, uptime, speed.replace("Mbps", "").trim(),
                data, score,
                expiredTime,
                "vpn", "vpn"
              )
              vpnConfigurations.add(vpnConfig)
              onNewConfigurationAdded.invoke(formatConfigurations(vpnConfigurations))
            } else continue
          } else {
            continue
          }
        }
      }
    }
    onComplete.invoke(formatConfigurations(vpnConfigurations))
  }

  // Implementation of direct snapshot for getting all configurations.
  @WorkerThread
  suspend fun parse(): List<VpnConfiguration> = suspendCancellableCoroutine { continuation ->
    val job = SupervisorJob()
    CoroutineScope(Dispatchers.IO + job).launch scope@{
      parse(
        onNewConfigurationAdded = {
          if (continuation.isCancelled) {
            job.cancel()
          }
        },
        onComplete = continuation::resume
      )
    }
  }

  private fun formatConfigurations(list: List<VpnConfiguration>): List<VpnConfiguration> {
    return list.sortedByDescending { it.speed.toFloat() }.distinctBy { it.country }
      .take(3).map { it.copy(premium = true) }
      .union(list).distinctBy { it.ip }
  }

  private fun formatCountry(country: String): String {
    var ct = country
    list.forEach { ct = ct.replace(it, "") }
    return ct.trim()
  }

  private val list = listOf("Federation", "Republic of")
}