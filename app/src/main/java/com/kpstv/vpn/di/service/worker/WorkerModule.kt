package com.kpstv.vpn.di.service.worker

import androidx.work.ListenableWorker
import com.kpstv.vpn.services.VpnWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerModule {
  @Binds
  @IntoMap
  @WorkerKey(VpnWorker::class)
  abstract fun bindVpnWorker(f: VpnWorker.Factory) : DaggerWorkerFactory<out ListenableWorker>
}