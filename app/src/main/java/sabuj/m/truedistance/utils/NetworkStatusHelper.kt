package sabuj.m.truedistance.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * §11.1 — Utility helper checking active Internet network connectivity.
 */
object NetworkStatusHelper {
    /**
     * Verifies if the device has an active network with NET_CAPABILITY_INTERNET capability.
     */
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
