package com.kpstv.vpn.data.helpers

import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.NetworkUtils
import org.jsoup.Jsoup

class VpnBookParser(private val networkUtils: NetworkUtils) {

  suspend fun parse(
    onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {},
    onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit
  ) {
    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    val username = "vpnbook"
    val password = "2mxt8wz"

    val vpnBookResponse = networkUtils.simpleGetRequest("https://www.vpnbook.com/")
    if (vpnBookResponse.isSuccessful) {
      val body = vpnBookResponse.body?.string()
      vpnBookResponse.close() // Always close stream

      val doc = Jsoup.parse(body)

      val root = doc.getElementById("openvpn")
      val elements = root.child(0).children().filter { it.children().hasAttr("href") }

      for(ele in elements) {
        val url = "https://www.vpnbook.com${ele.child(1).attr("href")}"


      }
    }

    onComplete.invoke(vpnConfigurations)
  }

  private companion object {
    private val countryMap: Map<String, String> = mapOf(
      "PL" to "Poland",
      "DE" to "Germany",
      "US" to "United States",
      "CA" to "Canada",
      "FR" to "France"
    )
  }
}