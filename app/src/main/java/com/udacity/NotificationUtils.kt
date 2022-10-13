/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


// Notification ID.
private val NOTIFICATION_ID = 0
private const val CHANNEL_ID = "channelId"
private const val CHANNEL_NAME = "channelName"

fun sendNotification(
    selected: Selected,
    applicationContext: Context,
    pendingIntent: PendingIntent,
    manager: NotificationManager
) {

    val contentIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.download)
        .setContentTitle(selected.title)
        .setContentText(selected.desc)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(
            R.drawable.download,
            applicationContext.getString(R.string.notification_button),
            pendingIntent
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    manager.notify(NOTIFICATION_ID, builder.build())
}

fun Activity.createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply { setShowBadge(false) }
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.MAGENTA
        notificationChannel.enableVibration(true)
        notificationChannel.description = "Download complete"

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

