package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

data class Selected(val url: String, val title: String, val desc: String)
class MainActivity : AppCompatActivity() {
    private var downloadID: Long = 0
    private lateinit var selected: Selected
    private var status = FAILED
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        createNotificationChannel()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selected = when (checkedId) {
                R.id.glid -> Selected(GLIDE_URL, GLIDE_TITLE, GLIDE_TEXT)
                R.id.udacity -> Selected(UDACITY_URL, UDACITY_TITLE, UDACITY_TEXT)
                R.id.retro -> Selected(RETROFIT_URL, RETROFIT_TITLE, RETROFIT_TEXT)
                else -> Selected("", "", "")
            }
        }
        custom_button.setOnClickListener {
            val id: Int = radioGroup.checkedRadioButtonId
            if (id != -1) {
                val radio: RadioButton = findViewById(id)
                Toast.makeText(applicationContext, " ${radio.text}", Toast.LENGTH_SHORT).show()
                custom_button.changeState(ButtonState.Loading)
                download()
            } else {
                Toast.makeText(
                    applicationContext, "Please select the file to download", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                status = SUCCESS
                custom_button.changeState(ButtonState.Completed)
                notificationManager = ContextCompat.getSystemService(
                    applicationContext,
                    NotificationManager::class.java
                ) as NotificationManager
                val detailIntent = Intent(applicationContext, DetailActivity::class.java)
                detailIntent.putExtra(FILE_NAME, selected.title)
                detailIntent.putExtra(FILE_STATUS, status)
                pendingIntent = TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(detailIntent)
                    getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT)
                } as PendingIntent
                sendNotification(selected, applicationContext, pendingIntent, notificationManager)
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selected.url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "file.zip")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if (cursor.moveToFirst()) {
            when (cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) {
                DownloadManager.STATUS_FAILED -> {
                    status = FAILED
                    custom_button.changeState(ButtonState.Completed)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    status = SUCCESS
                }
            }
        }
    }


    companion object {
        const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        const val GLIDE_TITLE = "Glide: Image Loading Library By BumpTech"
        const val GLIDE_TEXT = "Glide repository is downloaded"

        const val UDACITY_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        const val UDACITY_TITLE = "Udacity: Android Kotlin Nanodegree"
        const val UDACITY_TEXT = "The Project From Udacity is downloaded"

        const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
        const val RETROFIT_TITLE = "Retrofit: Type-safe HTTP client by Square, Inc"
        const val RETROFIT_TEXT = "Retrofit repository is downloaded"
        private val REQUEST_CODE = 0

        const val FILE_NAME = "fileName"
        const val FILE_STATUS = "status"
        const val FAILED = "Failed"
        const val SUCCESS = "Success"

    }
}
