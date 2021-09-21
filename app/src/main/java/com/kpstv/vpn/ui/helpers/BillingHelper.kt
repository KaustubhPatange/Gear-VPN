package com.kpstv.vpn.ui.helpers

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.kpstv.vpn.R
import com.kpstv.vpn.logging.Logger
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class BillingSku(val sku: String) {
  companion object {
    fun createEmpty() = BillingSku(sku = "")
  }
}

class BillingHelper(private val activity: ComponentActivity) {

  private var currentIsPurchase: Boolean = false

  private var billingErrorMessage: String? = null

  val isPurchased: Flow<Boolean> get() = Settings.HasPurchased.get()
  private val purchaseCompleteStateFlow: MutableStateFlow<BillingSku> = MutableStateFlow(BillingSku.createEmpty())
  val purchaseComplete: StateFlow<BillingSku> = purchaseCompleteStateFlow.asStateFlow()

  companion object {
    const val purchase_sku = "gear_premium_sub"
  }

  private var sku: SkuDetails? = null

  private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      purchases.forEach {
        activity.lifecycleScope.launch { handlePurchase(it) }
      }
    }
  }

  private var billingClient = BillingClient.newBuilder(activity)
    .setListener(purchasesUpdatedListener)
    .enablePendingPurchases()
    .build()

  private val activityObserver = object: DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
      billingClient.endConnection()
    }
    override fun onDestroy(owner: LifecycleOwner) {
      activity.lifecycle.removeObserver(this)
    }
  }

  fun init() {
    activity.lifecycle.addObserver(activityObserver)
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
          activity.lifecycleScope.launchWhenStarted {
            querySkuDetails()
            validatePurchase()
          }
        } else {
          billingErrorMessage = billingResult.debugMessage
          Logger.d("Invalid Response code: ${billingResult.responseCode}, Message: ${billingResult.debugMessage}")
        }
      }
      override fun onBillingServiceDisconnected() {
        Logger.d("Service disconnected")
      }
    })

    activity.lifecycleScope.launchWhenStarted {
      isPurchased.collect { currentIsPurchase = it }
    }
  }

  fun launch() {
    val sku = sku ?: run {
      Toasty.error(activity, billingErrorMessage ?: activity.getString(R.string.purchase_err_client)).show()
      return
    }

    val flowParams = BillingFlowParams.newBuilder()
      .setSkuDetails(sku)
      .build()

   billingClient.launchBillingFlow(activity, flowParams).responseCode
  }

  private suspend fun querySkuDetails() {
    val params = SkuDetailsParams.newBuilder()
    params.setSkusList(listOf(purchase_sku)).setType(BillingClient.SkuType.SUBS)

    val skuDetailsResult: SkuDetailsResult = billingClient.querySkuDetails(params.build())

    skuDetailsResult.skuDetailsList?.let { list ->
      sku = list.find { it.sku == purchase_sku }
    }
  }

  private suspend fun validatePurchase() {

    fun removeSubscription() {
      if (currentIsPurchase) {
        Toasty.warning(activity, R.string.premium_expired, Toasty.LENGTH_LONG).show()
      }
      Settings.HasPurchased.set(false)
    }

    val purchaseResult = billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
    if (purchaseResult.purchasesList.isEmpty()) {
      removeSubscription()
      return
    }
    purchaseResult.purchasesList.firstOrNull { it.skus.contains(purchase_sku) }?.let { purchase ->
      if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
        removeSubscription()
        return
      }
    }

    Settings.HasPurchased.set(true)
  }

  private suspend fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
      if (!purchase.isAcknowledged) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
          .setPurchaseToken(purchase.purchaseToken)
        val ackPurchaseResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())

        if (ackPurchaseResult.responseCode != BillingClient.BillingResponseCode.OK) {
          Toasty.error(activity, activity.getString(R.string.purchase_ack)).show()
          return
        }
      }

      if (purchase.skus.contains(purchase_sku)) {
        Toasty.info(activity, activity.getString(R.string.restart_maybe), Toasty.LENGTH_LONG).show()
        Settings.HasPurchased.set(true)
        purchaseCompleteStateFlow.emit(BillingSku(purchase_sku))
      }
    }
  }
}