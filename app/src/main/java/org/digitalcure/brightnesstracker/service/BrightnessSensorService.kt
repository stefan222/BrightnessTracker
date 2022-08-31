/*
Copyright 2022 Stefan Diener Software-Entwicklung, digitalcure.org

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.digitalcure.brightnesstracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.digitalcure.brightnesstracker.R
import org.digitalcure.brightnesstracker.ui.MainActivity

/**
 * Brightness tracker service implementation using an Android service.
 * @see "https://developer.android.com/guide/topics/sensors/sensors_environment"
 */
class BrightnessSensorService : Service(), IBrightnessSensorService {
    companion object {
        private val TAG = BrightnessSensorService::class.java.name

        private const val NOTIFICATION_ID = 3876

        private const val NOTIFICATION_CHANNEL_ID = "ServiceNotification"

        private const val MAIN_ACTIVITY_REQUEST_CODE = 4711
    }

    private val binder = LocalBinder()

    override val rawBrightnessValue: LiveData<Float> =
        MutableLiveData(IBrightnessSensorService.INVALID_BRIGHTNESS_VALUE)

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val rawValue = event?.values?.get(0) ?: return
            (rawBrightnessValue as MutableLiveData).value = rawValue
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // ignore this event
        }
    }

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "BrightnessSensorService is starting...")

        // Get an instance of the sensor service, and use that to get an instance of a particular sensor.
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor == null) {
            Toast.makeText(applicationContext, R.string.error_no_light_sensor, Toast.LENGTH_LONG).show()
            return START_NOT_STICKY
        }

        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Create the NotificationChannel
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // Register the channel with the system
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // If the notification supports a direct reply action, use PendingIntent.FLAG_MUTABLE instead.
        val pendingIntent =
            Intent(this, MainActivity::class.java).let { notiIntent ->
                PendingIntent.getActivity(this, MAIN_ACTIVITY_REQUEST_CODE, notiIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "BrightnessSensorService is done.")
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder(), IBrightnessSensorService.ILocalBinder {
        override fun getService(): IBrightnessSensorService = this@BrightnessSensorService
    }
}
