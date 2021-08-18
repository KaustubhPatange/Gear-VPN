package com.kpstv.vpn.data.helpers

import androidx.annotation.WorkerThread
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils.Companion.getBodyAndClose
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.resume

class VpnGateParser(private val networkUtils: NetworkUtils) {

  companion object {
    private val ipRegex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex()
  }

  // Suspend in a way to work like a callback. This should make bridge between `emit`ion & direct snapshot seamless.
  suspend fun parse(
    onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {},
    onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit
  ) {

    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    val vpnResponse = networkUtils.simpleGetRequest("https://www.vpngate.net/en").getOrNull()
    if (vpnResponse?.isSuccessful == true) {

      val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
      val expiredTime = DateUtils.format(offsetDateTime).toLong()

      val body = vpnResponse.getBodyAndClose()

      val doc = Jsoup.parse(body)

      val table = doc.getElementById("vpngate_inner_contents_td").children()
        .findLast { it.id() == "vg_hosts_table_id" }?.child(0)
        ?: run {
          onComplete.invoke(formatConfigurations(vpnConfigurations))
          return
        }
      vpnConfigurations.clear()

      // First iteration: Capture all data needed
      val intermediateList = arrayListOf<VpnConfiguration>()
      for (i in 1 until table.childrenSize()) {

        val tr = table.child(i)

        if (tr.getElementsByClass("vg_table_header").size == 0) {
          val imageUrl = tr.child(0).child(0).attr("src").replace("../", "https://www.vpngate.net/")
          val country = tr.child(0).text()

          if (country == "Reserved") continue

          // no more than 3 countries....
//          if (vpnConfigurations.count { it.country == formatCountry(country) } == 3) continue

          val ip = ipRegex.find(tr.child(1).html())?.value ?: ""

          val sessions = tr.child(2).child(0).child(0).text()
          val uptime = tr.child(2).child(2).text()

          val speed = tr.child(3).child(0).child(0).text()

          if (speed == "0.00 Mbps") continue

          val ovpnConfigElement = tr.child(6)
          if (ovpnConfigElement.childrenSize() == 0) continue

          val score = tr.child(tr.childrenSize() - 1).child(0).child(0).text()
            .replace(",", "").toLong()

          val configUrl = "https://www.vpngate.net/en/" + ovpnConfigElement.child(0).attr("href")

          val vpnConfig = VpnConfiguration(
            country = formatCountry(country),
            countryFlagUrl = imageUrl,
            ip = ip,
            sessions = sessions,
            upTime = uptime,
            speed = speed.replace("Mbps", "").trim(),
            configTCP = configUrl, // this will be considered
            configUDP = null,
//            configTCP = configTCP,
//            configUDP = configUDP,
            score = score,
            expireTime = expiredTime,
            username = "vpn",
            password = "vpn",
          )

          intermediateList.add(vpnConfig)
        }
      }

      // Second iteration: Format & capture the configs
      intermediateList.sortByDescending { it.speed }
      val premiumCountries = intermediateList.asSequence().groupBy { it.country }.filter { it.value.size > 1 }.map { it.key }.distinct().toMutableList()

      for(item in intermediateList) {
        // no more than 3 countries
        if (vpnConfigurations.count { it.country == formatCountry(item.country) } == 3) continue

        // fetch TCP & UDP configs
        val configResponse = networkUtils.simpleGetRequest(item.configTCP!!).getOrNull() // configTCP here serves as URL in previous iteration.
          if (configResponse?.isSuccessful == true) {
            val configBody = configResponse.getBodyAndClose()
            val hrefElements = Jsoup.parse(configBody).getElementsByAttribute("href")
            val ovpnConfigs = hrefElements.filter { it.attr("href").contains(".ovpn") }
              .map { "https://www.vpngate.net" + it.attr("href") }

            val configTCPUrl = ovpnConfigs.firstOrNull { it.contains("tcp=1") }
            val configUDPUrl = ovpnConfigs.firstOrNull { it.contains("udp=1") }

            val configTCP = safeFetchConfig(configTCPUrl)
            val configUDP = safeFetchConfig(configUDPUrl)

            if (configTCP == null && configUDP == null) continue

            val premium = if (premiumCountries.contains(item.country)) {
              premiumCountries.remove(item.country) // remove item after first use
              true
            } else false

            vpnConfigurations.add(
              item.copy(
                configTCP = configTCP,
                configUDP = configUDP,
                premium = premium
              )
            )
            onNewConfigurationAdded.invoke(formatConfigurations(vpnConfigurations))
          }
      }


//          val configResponse = networkUtils.simpleGetRequest(configUrl).getOrNull()
//          if (configResponse?.isSuccessful == true) {
//            val configBody = configResponse.getBodyAndClose()
//
//            val hrefElements = Jsoup.parse(configBody).getElementsByAttribute("href")
//            val ovpnConfigs = hrefElements.filter { it.attr("href").contains(".ovpn") }
//              .map { "https://www.vpngate.net" + it.attr("href") }
//
//            val configTCPUrl = ovpnConfigs.firstOrNull { it.contains("tcp=1") }
//            val configUDPUrl = ovpnConfigs.firstOrNull { it.contains("udp=1") }
//
//            val configTCP = safeFetchConfig(configTCPUrl)
//            val configUDP = safeFetchConfig(configUDPUrl)
//
//            if (configTCP == null && configUDP == null) continue
//
//            val vpnConfig = VpnConfiguration(
//              country = formatCountry(country),
//              countryFlagUrl = imageUrl,
//              ip = ip,
//              sessions = sessions,
//              upTime = uptime,
//              speed = speed.replace("Mbps", "").trim(),
//              configTCP = configTCP,
//              configUDP = configUDP,
//              score = score,
//              expireTime = expiredTime,
//              username = "vpn",
//              password = "vpn",
//            )
//
//            vpnConfigurations.add(vpnConfig)
//            onNewConfigurationAdded.invoke(formatConfigurations(vpnConfigurations))
//          } else {
//            continue
//          }
//
    }
    onComplete.invoke(formatConfigurations(vpnConfigurations))
  }

  private suspend fun safeFetchConfig(configUrl: String?): String? {
    configUrl?.let { url ->
      val response = networkUtils.simpleGetRequest(url).getOrNull()
      if (response?.isSuccessful == true) {
        return response.getBodyAndClose()
      }
    }
    return null
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

    return list.sortedBy { it.country }.sortedByDescending { it.premium }
//    return list.groupBy { it.country }.filter { it.value.size > 1 }.flatMap { it.value }
//      .sortedByDescending { it.speed }.distinctBy { it.country }.map { it.copy(premium = true) }
//      .union(list).distinctBy { it.ip }


    /*return list.sortedByDescending { it.speed.toFloat() }.distinctBy { it.country }
      .take(5).distinctBy { it.country }.map { it.copy(premium = true) }
      .union(list).distinctBy { it.ip }*/
  }

  private fun formatCountry(country: String): String {
    var ct = country
    list.forEach { ct = ct.replace(it, "") }
    return ct.trim()
  }

  private val list = listOf("Federation", "Republic of")
}