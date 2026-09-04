package com.example.appgasto.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    val state = billingManager.state

    fun launchProPurchase(activity: Activity) = billingManager.launchProPurchase(activity)

    fun restore() = billingManager.restore()

    fun requery() = billingManager.requery()
}
