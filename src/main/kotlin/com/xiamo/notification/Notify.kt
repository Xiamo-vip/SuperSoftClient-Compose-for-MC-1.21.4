package com.xiamo.notification

import androidx.compose.runtime.Composable

class Notify(val titile: String, val message: String,val time:Long,val composeContent: @Composable () -> Unit?) {
    val startTime = System.currentTimeMillis()

    val isExpired
        get() = System.currentTimeMillis() - startTime > this.time

    val expiredTime
         get() = System.currentTimeMillis() - startTime



}