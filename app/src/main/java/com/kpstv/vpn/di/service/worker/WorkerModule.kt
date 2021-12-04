package com.kpstv.vpn.di.service.worker

import androidx.work.ListenableWorker
import com.kpstv.vpn.services.FlagWorker
import com.kpstv.vpn.services.VpnWorker
import com.kpstv.vpn.services.VpnBookWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerModule {
  @Binds
  @IntoMap
  @WorkerKey(VpnWorker::class)
  abstract fun bindVpnWorker(f: VpnWorker.Factory) : DaggerWorkerFactory<out ListenableWorker>

  @Binds
  @IntoMap
  @WorkerKey(VpnBookWorker::class)
  abstract fun bindVpnBookPasswordWorker(f: VpnBookWorker.Factory) : DaggerWorkerFactory<out ListenableWorker>

  @Binds
  @IntoMap
  @WorkerKey(FlagWorker::class)
  abstract fun bindFlagWorker(f: FlagWorker.Factory) : DaggerWorkerFactory<out ListenableWorker>
}