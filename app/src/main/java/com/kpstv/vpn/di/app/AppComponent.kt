package com.kpstv.vpn.di.app

import android.app.Application
import com.kpstv.vpn.App
import com.kpstv.vpn.di.activity.ActivityComponent
import com.kpstv.vpn.di.service.ServiceComponent
import com.kpstv.vpn.di.service.worker.DaggerWorkerFactory
import com.kpstv.vpn.di.service.worker.WorkerModule
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [AppModule::class, WorkerModule::class])
interface AppComponent {

  fun newActivityComponentFactory(): ActivityComponent.Factory

  fun newServiceComponent() : ServiceComponent

  @Component.Factory
  interface Builder {
    fun create(@BindsInstance application: Application) : AppComponent
  }

  fun inject(app: App)

}