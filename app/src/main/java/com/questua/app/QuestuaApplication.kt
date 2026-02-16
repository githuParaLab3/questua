package com.questua.app

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuestuaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51SW2Ti2XZjipIAZilX1YoDuguVt3PzWbNmuvskh0SomNa3uPSOV3bZvY9AvxQNttTSYzzN86igiTZD3Bh8K9LTwZ00WIC6qIi7"
        )
    }
}