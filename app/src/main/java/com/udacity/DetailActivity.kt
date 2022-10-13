package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.MainActivity.Companion.FILE_NAME
import com.udacity.MainActivity.Companion.FILE_STATUS
import com.udacity.MainActivity.Companion.SUCCESS
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import timber.log.Timber

class DetailActivity : AppCompatActivity() {
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)


        ok.setOnClickListener {
            Timber.i("ok.setOnClickListener")
            startActivity(Intent(this, MainActivity::class.java))
        }

        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        intent?.extras?.let {
            fileName.text = intent.getStringExtra(FILE_NAME)
            status.text = intent.getStringExtra(FILE_STATUS)
            notificationManager.cancelNotifications()
            fileName.setTextColor(getColor(R.color.colorPrimaryDark))
            if (intent.getStringExtra(FILE_STATUS).equals(SUCCESS)) {
                status.setTextColor(getColor(R.color.colorPrimaryDark))
                return
            }
            status.setTextColor(getColor(R.color.error))
        }


    }

}
