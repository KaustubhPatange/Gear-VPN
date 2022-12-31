package com.kpstv.vpn.ui.helpers

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.kpstv.vpn.R
import com.kpstv.vpn.data.models.Plan
import com.kpstv.vpn.logging.Logger
import com.kpstv.vpn.ui.viewmodels.PlanViewModel
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume

sealed interface SkuState {
  data class Sku(internal val skus: List<SkuDetails> = emptyList(), internal val plans: List<Plan>) : SkuState {

    val details: List<Data> = skus.map { sku ->
      val plan = plans.first { it.sku == sku.sku }
      return@map Data(
        id = sku.sku,
        billingName = plan.name,
        billingPeriodMonth = plan.billingCycleMonth,
        price = sku.price
      )
    }

    data class Data(
      val id: String,
      val billingName: String,
      val billingPeriodMonth: Int,
      val price: String,
    )
  }
  object Loading : SkuState
  object Error : SkuState
}

class BillingHelper(
  private val activity: ComponentActivity,
  private val planViewModel: PlanViewModel
) {

  private var currentIsPurchase: Boolean = false

  private var billingErrorMessage: String? = null

  val isPurchased: Flow<Boolean> get() = Settings.HasPurchased.get()
  private val purchaseCompleteStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val purchaseComplete: StateFlow<Boolean> = purchaseCompleteStateFlow.asStateFlow()

  val planState = MutableStateFlow<SkuState>(SkuState.Loading)

  private val availableSkus = arrayListOf<String>()
  private val skusDetails: List<SkuDetails>? get() = (planState.value as? SkuState.Sku)?.skus

  private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      purchases.forEach {
        activity.lifecycleScope.launch { handlePurchase(it) }
      }
    }
  }

  private var _billingClient: BillingClient? = null
  private val billingClient get() = _billingClient!!

  init {
    setupBillingClient()
  }

  private fun setupBillingClient() {
    _billingClient = BillingClient.newBuilder(activity)
      .setListener(purchasesUpdatedListener)
      .enablePendingPurchases()
      .build()
  }

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
    activity.lifecycleScope.launchWhenStarted { startConnection() }
    activity.lifecycleScope.launchWhenStarted {
      isPurchased.collect { currentIsPurchase = it }
    }
  }

  private suspend fun startConnection() {
    suspendCancellableCoroutine { continuation ->
      fun complete() {
        if (!continuation.isCompleted) continuation.resume(Unit)
      }
      billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
          if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            activity.lifecycleScope.launchWhenStarted {
              querySkuDetails()
              validatePurchase()
              complete()
            }
          } else {
            billingErrorMessage = billingResult.debugMessage
            Logger.d("Invalid Response code: ${billingResult.responseCode}, Message: ${billingResult.debugMessage}")
            complete()
          }
        }
        override fun onBillingServiceDisconnected() {
          Logger.d("Service disconnected")
        }
      })
    }
  }

  fun launch(sku: String) {
    activity.lifecycleScope.launchWhenStarted scope@{
      if (billingClient.isDisconnected()) {
        setupBillingClient()
        startConnection()
      }

      val skuDetail = skusDetails?.firstOrNull { it.sku == sku } ?: run {
        Toasty.error(activity, billingErrorMessage ?: activity.getString(R.string.purchase_err_client)).show()
        return@scope
      }

      val flowParams = BillingFlowParams.newBuilder()
        .setSkuDetails(skuDetail)
        .build()

      billingClient.launchBillingFlow(activity, flowParams).responseCode
    }
  }

  private suspend fun querySkuDetails() {
    val plans = planViewModel.fetchPlans().data
    availableSkus.clear() // method can called multiple times, better to clear it
    availableSkus.addAll(plans.map { it.sku })

    val params = SkuDetailsParams.newBuilder()
    params.setSkusList(availableSkus).setType(BillingClient.SkuType.SUBS)

    val skuDetailsResult: SkuDetailsResult = billingClient.querySkuDetails(params.build())

    val skuDetails = skuDetailsResult.skuDetailsList?.filter { availableSkus.contains(it.sku) }
      ?: emptyList()
    if (skuDetails.isEmpty()) {
      planState.emit(SkuState.Error)
    } else {
      planState.emit(SkuState.Sku(skuDetails, plans))
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
    purchaseResult.purchasesList.firstOrNull { purchase ->
      purchase.skus.intersect(availableSkus.toSet()).isNotEmpty()
    }?.let { purchase ->
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

      if (purchase.skus.intersect(availableSkus.toSet()).isNotEmpty()) {
        Toasty.info(activity, activity.getString(R.string.restart_maybe), Toasty.LENGTH_LONG).show()
        Settings.HasPurchased.set(true)
        purchaseCompleteStateFlow.emit(true)
      }
    }
  }

  private fun BillingClient.isDisconnected(): Boolean {
    return listOf(BillingClient.ConnectionState.DISCONNECTED, BillingClient.ConnectionState.CLOSED)
      .contains(connectionState)
  }
}