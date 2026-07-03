package com.example.appgasto.data.local

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.appgasto.R

@Composable
fun Category.localizedName(): String {
    val resId = when (stringKey) {
        "cat_food" -> R.string.cat_food
        "cat_transport" -> R.string.cat_transport
        "cat_leisure" -> R.string.cat_leisure
        "cat_home" -> R.string.cat_home
        "cat_health" -> R.string.cat_health
        "cat_clothing" -> R.string.cat_clothing
        "cat_other" -> R.string.cat_other
        else -> 0
    }
    return if (resId != 0) stringResource(resId) else name
}
