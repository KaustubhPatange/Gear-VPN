package com.kpstv.vpn.extensions.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import com.kpstv.vpn.logging.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory
import kotlin.reflect.KClass

suspend fun safeNetworkAccessor(vararg excludeExceptions: KClass<out Exception>, onConnectionRestored: suspend () -> Unit) {

  fun rethrow(e: IOException) {
    if (e::class in excludeExceptions) throw e
  }

  try {
    onConnectionRestored()
  } catch (e: IOException) {
    rethrow(e)
    delay(1000) // delay for network manager to update it's internal status.
    Logger.d("Crash: Network status: ${NetworkMonitor.connection.value}")
    if (!NetworkMonitor.connection.value) {
      NetworkMonitor.connection.collect { status ->
        if (status) {
          try {
            Logger.d("Restoring")
            onConnectionRestored()
          } catch (e: IOException) {
            rethrow(e)
            Logger.d("Failed again")
          }
        }
      }
    }
  }
}

object NetworkMonitor {

  private val networks: MutableSet<Network> = HashSet()
  private lateinit var connectivityManager: ConnectivityManager
  private var job = SupervisorJob()

  private val connectionStateFlow = MutableStateFlow(false)
  val connection = connectionStateFlow.asStateFlow()

  private var isInitialized: Boolean = false

  fun isConnected() : Boolean = if (!isInitialized) true else connection.value

  fun init(applicationContext: Context) {
    connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    try {
      connectivityManager.registerNetworkCallback(
        NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build(),
        networkCallback
      )
    } catch (e: SecurityException) {
      // java.lang.SecurityException: Package does not belong to xxx
      // https://issuetracker.google.com/issues/175055271

      // Fake network connection in such case
      connectionStateFlow.tryEmit(true)
    }
  }

  // forcefully fire network changes
  fun forceUpdate() {
    fireNetworkChanges()
  }

  private fun fireNetworkChanges() {
    connectionStateFlow.tryEmit(networks.size > 0)
    Logger.d("NetworkMonitor Status: ${connection.value}")
  }

  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
      val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
      if (hasInternetCapability == true) {
        job = SupervisorJob()
        CoroutineScope(Dispatchers.IO + job).launch {
          val hasInternet = execute(network.socketFactory)
          if(hasInternet){
            withContext(Dispatchers.Main) {
              networks.add(network)
              fireNetworkChanges()
            }
          }
          isInitialized = true
        }
      }
    }

    override fun onLost(network: Network) {
      networks.remove(network)
      fireNetworkChanges()
    }

    private fun execute(socketFactory: SocketFactory): Boolean {
      return try{
        val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
        socket.connect(InetSocketAddress("8.8.8.8", 53), 1500) // TODO: Migrate to other DNS than Google to make sure it works in countries like China
        socket.close()
        // gotcha hat
        true
      }catch (e: IOException){
        // no internet
        false
      }
    }
  }
}