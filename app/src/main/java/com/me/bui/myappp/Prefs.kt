package com.me.bui.myappp

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

import androidx.preference.PreferenceManager


object Prefs {
    private var preferences: SharedPreferences? = null
    val INSTALLED_VERSION = "INSTALLED_VERSION"
    val NOTIFY_ON_UPGRADES = "NOTIFY_ON_UPGRADES"
    val SHOW_WHATS_NEW_ON_NEXT_LAUNCH = "SHOW_WHATS_NEW_ON_NEXT_LAUNCH"

    /**
     * @return A read-only instance of this app’s default
     * `SharedPreferences`.
     */
    operator fun get(context: Context?): SharedPreferences? {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return preferences
    }

    fun edit(context: Context?): Editor {
        return get(context)!!.edit()
    }
}