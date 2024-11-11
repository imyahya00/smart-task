package com.tcp.smarttasks.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Utils {
    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        fun formatDate(date: LocalDate): String {
            return when (date) {
                LocalDate.now() -> "Today"
                LocalDate.now().plusDays(1) -> "Tomorrow"
                else -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            }
        }
    }
}