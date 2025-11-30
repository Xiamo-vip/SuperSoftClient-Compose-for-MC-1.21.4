package com.xiamo.notification

import androidx.compose.runtime.mutableStateListOf
import java.util.concurrent.CopyOnWriteArrayList

object NotificationManager {
    var notifies = mutableStateListOf<Notify>()



    fun add(notify: Notify) {
        notifies.add(notify)
    }



}