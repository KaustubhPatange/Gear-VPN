package com.kpstv.composetest.extensions.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// https://github.com/KaustubhPatange/Moviesy/blob/master/app/src/main/java/com/kpstv/yts/extensions/utils/RetrofitUtils.kt
@Singleton
class NetworkUtils @Inject constructor() {

  fun getRetrofitBuilder(): Retrofit.Builder {
    return Retrofit.Builder().apply {
      addConverterFactory(MoshiConverterFactory.create())
      client(getHttpClient())
    }
  }

  fun getHttpBuilder(): OkHttpClient.Builder {
    return OkHttpClient.Builder()
//      .addInterceptor(interceptor)
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
  }

  /**
   * @param addLoggingInterceptor If true logcat will display all the Http request messages
   */
  fun getHttpClient(addLoggingInterceptor: Boolean = false): OkHttpClient {
    val client = getHttpBuilder()
    if (addLoggingInterceptor) {
      val loggingInterceptor = HttpLoggingInterceptor()
      loggingInterceptor.level =
        HttpLoggingInterceptor.Level.BODY
      client.addInterceptor(loggingInterceptor)
    }
    return client.build()
  }

  suspend fun simpleGetRequest(url: String) =
    getHttpClient().newCall(Request.Builder().url(url).build()).await()


  private suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
      enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          if (continuation.isCancelled) return
          continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
          continuation.resume(response)
        }
      })
      continuation.invokeOnCancellation {
        try {
          cancel()
        } catch (ex: Throwable) {
        }
      }
    }
  }
}