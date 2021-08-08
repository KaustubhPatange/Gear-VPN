package com.kpstv.vpn.data.helpers

import androidx.annotation.WorkerThread
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume

class VpnBookParser(private val networkUtils: NetworkUtils) {
// TODO: Refactor a bit & allow fetching username & password from url & add this to WorkManager
  suspend fun parse(
    onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {},
    onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit
  ) {
    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    val username = "vpnbook"
    val password = "2mxt8wz"

    val vpnBookResponse = networkUtils.simpleGetRequest("https://www.vpnbook.com/")
    if (vpnBookResponse.isSuccessful) {

      val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
      val expiredTime = DateUtils.format(offsetDateTime).toLong()

      val body = vpnBookResponse.body?.string()
      vpnBookResponse.close() // Always close stream

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
//          android.util.Log.e(entry.name, "Parsing")
          if (configUDP == null && entry.name.contains("udp")) {
            configUDP = readZipFromStream(zipStream, buffer)
//            android.util.Log.e(entry.name, "ConfigUDP Set")
          }
          if (configTCP == null && entry.name.contains("tcp")) {
            configTCP = readZipFromStream(zipStream, buffer)
//            android.util.Log.e(entry.name, "ConfigTCP Set")
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
//        android.util.Log.e(url,"Check For IP: ${ip}, Country: $name")
//        android.util.Log.e(url,"Check if same: ${configUDP == configTCP}")

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