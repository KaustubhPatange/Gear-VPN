package com.kpstv.vpn.di.service

import com.kpstv.vpn.services.GearConnect
import dagger.Subcomponent

@ServiceScope
@Subcomponent
interface ServiceComponent {
  fun inject(service: GearConnect)
}