package com.kpstv.vpn.di.service.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Reusable
import javax.inject.Inject
import javax.inject.Provider

@Reusable
class InjectDaggerWorkerFactory @Inject constructor(
  private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<DaggerWorkerFactory<out ListenableWorker>>>
) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    val foundEntry = workerFactories.entries
      .find { Class.forName(workerClassName).isAssignableFrom(it.key) }
      ?: return null
    return foundEntry.value.get().create(appContext, workerParameters)
  }
}
