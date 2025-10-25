package com.devgarden.finper.navigation

import android.content.Context
import androidx.core.content.edit

object OnboardingPrefs {
    private const val PREFS_NAME = "fnper_prefs"
    private const val KEY_ONBOARDING_SEEN = "onboarding_seen"

    fun isOnboardingSeen(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
    }

    fun setOnboardingSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_ONBOARDING_SEEN, true) }
    }
}
