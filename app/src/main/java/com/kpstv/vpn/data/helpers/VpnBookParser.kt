package com.kpstv.vpn.data.helpers

import androidx.annotation.WorkerThread
import com.kpstv.vpn.data.models.AppSettingsConverter
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils.Companion.getBodyAndClose
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume

class VpnBookParser(private val networkUtils: NetworkUtils) {

  suspend fun parse(
    onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {},
    onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit
  ) {
    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    var username = "vpnbook"
    var password = "2mxt8wz"

    val appSettingResponse = networkUtils.simpleGetRequest(SettingsUrl).getOrNull()
    if (appSettingResponse?.isSuccessful == true) {
      val content = appSettingResponse.getBodyAndClose()
      AppSettingsConverter.fromStringToAppSettings(content)?.let { converter ->
        username = converter.vpnbook.username
        password = converter.vpnbook.password
      }
    }

    val vpnBookResponse = networkUtils.simpleGetRequest("https://www.vpnbook.com/").getOrNull()
    if (vpnBookResponse?.isSuccessful == true) {

      val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
      val expiredTime = DateUtils.format(offsetDateTime).toLong()

      val body = vpnBookResponse.getBodyAndClose()

      val doc = Jsoup.parse(body)

      val root = doc.getElementById("openvpn")
      val elements = root.allElements.filter { it.hasAttr("href") }.filter { it.attr("href").endsWith(".zip") }//.children().filter { it.children().any { it.hasAttr("href") } }
      val buffer = ByteArray(2048)

      for(ele in elements) {
        val url = "https://www.vpnbook.com${ele.attr("href")}"
        val bytes = URL(url).openStream().readBytes()
        val zipStream = ZipInputStream(ByteArrayInputStream(bytes))

        var configUDP: String? = null
        var configTCP: String? = null
        var ip: String? = null

        var entry = zipStream.nextEntry
        while(entry != null) {
          if (configUDP == null && entry.name.contains("udp")) {
            configUDP = readZipFromStream(zipStream, buffer)
          }
          if (configTCP == null && entry.name.contains("tcp")) {
            configTCP = readZipFromStream(zipStream, buffer)
          }
          if (configTCP != null && configUDP != null) break
          entry = zipStream.nextEntry
        }

        if (ip == null && configTCP != null) {
          ip = ipRegex.find(configTCP)?.groups?.get(1)?.value
        }
        if (ip == null && configUDP != null) {
          ip = ipRegex.find(configUDP)?.groups?.get(1)?.value
        }

        zipStream.close()

        val name = nameRegex.find(url)?.groups?.get(1)?.value ?: "-1"

        val configuration = VpnConfiguration.createEmpty().copy(
          country = countryMap.getOrElse(name) { "Unknown" },
          configTCP = configTCP,
          configUDP = configUDP,
          expireTime = expiredTime,
          username = username,
          password = password,
          ip = ip ?: "Unknown"
        )

        vpnConfigurations.add(configuration)
        onNewConfigurationAdded.invoke(formatConfiguration(vpnConfigurations))
      }
    }

    onComplete.invoke(formatConfiguration(vpnConfigurations))
  }

  private fun readZipFromStream(zipStream: ZipInputStream, buffer: ByteArray): String? {
    return ByteArrayOutputStream().use { result ->
      var length = zipStream.read(buffer)
      while(length > 0) {
        result.write(buffer, 0, length)
        length = zipStream.read(buffer)
      }

      result.toString("UTF-8")
    }
  }

  private fun formatConfiguration(configs: List<VpnConfiguration>): List<VpnConfiguration> {
    return configs.groupBy { it.country }.filter { it.value.size > 1 }.flatMap { it.value }
      .distinctBy { it.country }.map { it.copy(premium = true) }
      .union(configs).distinctBy { it.ip }
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

  private companion object {
    private const val SettingsUrl = "https://pastebin.com/raw/Txd7v6y6" // TODO: Change this to with the one from github

    val ipRegex = "remote\\s?([\\d.]+)\\s?\\d+".toRegex()
    private val nameRegex = "VPNBook\\.com-OpenVPN-([\\w]{2})".toRegex()
    private val countryMap: Map<String, String> = mapOf(
      "PL" to "Poland",
      "DE" to "Germany",
      "US" to "United States",
      "CA" to "Canada",
      "FR" to "France",
    )
  }
}