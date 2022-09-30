package com.example.socketexample.Service

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.socketexample.Interface.Detector
import com.example.socketexample.Utillity.Utils

class LollipopDetector : Detector {
    @SuppressLint("WrongConstant")
    override fun getForegroundApp(context: Context?): String? {
        return if (!Utils.hasUsageStatsPermission(context)) {
            null
        } else {
            var foregroundApp: String? = null
            val mUsageStatsManager = context?.getSystemService("android.content.Context.USAGE_STATS_SERVICE") as UsageStatsManager
            val time = System.currentTimeMillis()
            val usageEvents = mUsageStatsManager.queryEvents(time - 3600000L, time)
            val event = UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == 1) {
                    foregroundApp = event.packageName
                }
            }
            foregroundApp
        }
    }

}
