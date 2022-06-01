package com.kpstv.vpn.data.helpers

import androidx.annotation.WorkerThread
import com.kpstv.vpn.data.models.AppSettings
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.logging.Logger
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils.Companion.getBodyAndClose
import com.kpstv.vpn.extensions.utils.NetworkUtils.Companion.getOrThrowIO
import kotlinx.coroutines.*
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.*
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream
import javax.net.ssl.SSLException

class VpnBookParser(private val networkUtils: NetworkUtils) {

  suspend fun parse(
    onNewConfigurationAdded: suspend (snapshot: List<VpnConfiguration>) -> Unit = {},
    onComplete: suspend (snapshot: List<VpnConfiguration>) -> Unit
  ) {
    val vpnConfigurations = arrayListOf<VpnConfiguration>()

    var username = "vpnbook"
    var password = "ct36a3k"

    Logger.d("Fetching credentials for vpnbook.com")
    val appSettingResult = networkUtils.simpleGetRequest(SettingsUrl)
    appSettingResult.fold(
      onSuccess = { response ->
        if (response.isSuccessful) {
          val content = response.getBodyAndClose()
          AppSettings.Converter.fromString(content)?.let { converter ->
            username = converter.vpnbook.username
            password = converter.vpnbook.password
          }
        }
      },
      onFailure = { exception ->
        Logger.w(exception, "Couldn't parse appsettings")
        onComplete(vpnConfigurations)
        return
      }
    )

    Logger.d("Fetching from network: vpnbook.com")
    val vpnBookResponse : Response = withTimeoutOrNull(CallTimeoutMillis) {
      val result = networkUtils.simpleGetRequest("https://www.vpnbook.com/")
      if (result.isFailure) {
        Logger.w(result.exceptionOrNull(), "Couldn't connect to vpnbook.com")
      }
      result.getOrThrowIO()
    } ?: run {
      Logger.w(Exception("Timeout error"), "Error: Vpnbook Timed out")
      onComplete.invoke(vpnConfigurations)
      return
    }

    if (vpnBookResponse.isSuccessful) {

      val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
      val expiredTime = DateUtils.format(offsetDateTime).toLong()

      val body = vpnBookResponse.getBodyAndClose() ?: run {
        Logger.d("Error: Body is null")
        onComplete.invoke(vpnConfigurations)
        return
      }

      val doc = Jsoup.parse(body)

      val root = doc.getElementById("openvpn") ?: run {  // platform types beware!!!
        onComplete(vpnConfigurations)
        return
      }
      val elements = root.allElements.filter { it.hasAttr("href") }.filter { it.attr("href").endsWith(".zip") }//.children().filter { it.children().any { it.hasAttr("href") } }
      val buffer = ByteArray(2048)

      for(ele in elements) {
        val url = "https://www.vpnbook.com${ele.attr("href")}"
        val name = nameRegex.find(url)?.groups?.get(1)?.value ?: "-1"

        Logger.d("Download configs for $name")

        /* Patch to fix any SSL or interrupting connection issues */
        val bytes = try {
          val urlConnection = URL(url).openConnection()
          urlConnection.connectTimeout = 15 * 1000
          urlConnection.readTimeout = 30 * 1000
          val stream = urlConnection.getInputStream()
          stream.run {
            val bytes = stream.readBytes()
            close()
            bytes
          }
        } catch (e : SSLException) {
          continue // skip
        } catch (e : ConnectException) {
          continue // skip
        } catch (e: SocketException) {
          continue // skip
        } catch (e : SocketTimeoutException) {
          continue // skip
        } catch (e: FileNotFoundException) {
          continue // skip, fixed: Issue where file does not exist for a temporary period.
        }

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


        val configuration = VpnConfiguration.createEmpty().copy(
          country = countryMap.getOrElse(name) { "Unknown" },
          configTCP = configTCP,
          configUDP = configUDP,
          expireTime = expiredTime,
          username = username,
          password = password,
          ip = ip ?: "Unknown"
        )

        Logger.d("Ip: ${configuration.ip}, Has TCP: ${configTCP != null}, Has UDP: ${configUDP != null}")

        vpnConfigurations.add(configuration)
        onNewConfigurationAdded.invoke(formatConfiguration(vpnConfigurations))
      }
    }

    Logger.d("Parsing completed with: ${vpnConfigurations.size} items")

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
  suspend fun parse(): List<VpnConfiguration> {
    var vpnConfigs : List<VpnConfiguration> = emptyList()
    parse(
      onComplete = { vpnConfigs = it }
    )
    return vpnConfigs
  }

  private companion object {
    private const val CallTimeoutMillis : Long = 1000L * 40
    private const val SettingsUrl = "https://raw.githubusercontent.com/KaustubhPatange/Gear-VPN/master/settings.json"

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