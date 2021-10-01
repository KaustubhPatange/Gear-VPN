package com.kpstv.vpn.data.helpers

import androidx.annotation.WorkerThread
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.clearAndAdd
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils.Companion.getBodyAndClose
import com.kpstv.vpn.logging.Logger
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.resume

class VpnGateParser(private val networkUtils: NetworkUtils) {

  // Suspend in a way to work like a callback. This should make bridge between `emit`ion & direct snapshot seamless.
  suspend fun parse(
    onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {},
    onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit
  ): Unit = coroutineScope scope@{

    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    Logger.d("Fetching from network: vpngate.net")
    val vpnResponse = networkUtils.simpleGetRequest("https://www.vpngate.net/")
    if (vpnResponse.isSuccessful) {

      val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
      val expiredTime = DateUtils.format(offsetDateTime).toLong()

      val body = vpnResponse.getBodyAndClose()

      val doc = Jsoup.parse(body)

      val table = doc.getElementById("vpngate_inner_contents_td").children()
        .findLast { it.id() == "vg_hosts_table_id" }?.child(0)
        ?: run {
          onComplete.invoke(formatConfigurations(vpnConfigurations))
          return@scope
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
            configTCP = configUrl, // this will be used as configUrl for next iteration.
            configUDP = null,
            score = score,
            expireTime = expiredTime,
            username = "vpn",
            password = "vpn",
          )

          intermediateList.add(vpnConfig)
        }
      }

      // Second iteration: Format & capture the configs

      val list = intermediateList.groupBy { it.country }
        .flatMap { c ->
          c.value.take(3) // no more than 3 countries (keep the original order since vpn gate ranking is better)
            .sortedByDescending { it.speed.toFloatOrNull() ?: 0f }
        }
      intermediateList.clearAndAdd(list)
      val premiumCountries = intermediateList.asSequence().groupBy { it.country }.filter { it.value.size > 1 }.map { it.key }.distinct().toMutableList()

      for(item in intermediateList) {
        // fetch TCP & UDP configs
        Logger.d("Fetching configs for ${item.country} - ${item.ip}")
        val configResponse = networkUtils.simpleGetRequest(item.configTCP!!) // configTCP here serves as URL in previous iteration.
          if (configResponse.isSuccessful) {
            val configBody = configResponse.getBodyAndClose()
            val hrefElements = Jsoup.parse(configBody).getElementsByAttribute("href")
            val ovpnConfigs = hrefElements.filter { it.attr("href").contains(".ovpn") }
              .map { "https://www.vpngate.net" + it.attr("href") }

            val configTCPUrl = ovpnConfigs.firstOrNull { it.contains("tcp=1") }
            val configUDPUrl = ovpnConfigs.firstOrNull { it.contains("udp=1") }

            val configTCPAsync = async { safeFetchConfig(configTCPUrl) }
            val configUDPAsync = async { safeFetchConfig(configUDPUrl) }

            val configs = awaitAll(configTCPAsync, configUDPAsync)

            val configTCP = configs[0]
            val configUDP = configs[1]

            if (configTCP == null && configUDP == null) continue

            Logger.d("Has TCP: ${configTCP != null}, Has UDP: ${configUDP != null}")

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
    }

    Logger.d("Parsing completed with: ${vpnConfigurations.size} items")

    onComplete.invoke(formatConfigurations(vpnConfigurations))
  }

  private suspend fun safeFetchConfig(configUrl: String?): String? {
    configUrl?.let { url ->
      val response = networkUtils.simpleGetRequest(url)
      if (response.isSuccessful) {
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
  }

  private fun formatCountry(country: String): String {
    var ct = country
    list.forEach { ct = ct.replace(it, "") }
    return ct.trim()
  }

  private val list = listOf("Federation", "Republic of")

  companion object {
    private val ipRegex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex()
    private val numberRegex = "\\d+".toRegex()

    private fun String.parseNumber(): Int {
      return numberRegex.find(this)?.value?.toInt() ?: 0
    }
  }
}