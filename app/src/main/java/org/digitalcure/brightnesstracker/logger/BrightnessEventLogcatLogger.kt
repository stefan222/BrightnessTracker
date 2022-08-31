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

package org.digitalcure.brightnesstracker.logger

import android.util.Log
import org.digitalcure.brightnesstracker.service.IBrightnessSensorService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Logs all brightness tracker events to logcat. Please note that the log messages are not
 * internationalized.
 */
open class BrightnessEventLogcatLogger : IBrightnessEventLogger {
    companion object {
        private val TAG = BrightnessEventLogcatLogger::class.java.name

        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    private var threshold = IBrightnessEventLogger.INVALID_THRESHOLD

    private var lastValue = IBrightnessSensorService.INVALID_BRIGHTNESS_VALUE

    override fun onServiceStarted() {
        val timestamp = DATE_TIME_FORMATTER.format(LocalDateTime.now())
        writeln("$timestamp Service started")
    }

    override fun onServiceStopped() {
        val timestamp = DATE_TIME_FORMATTER.format(LocalDateTime.now())
        writeln("$timestamp Service stopped")
    }

    override fun setThreshold(threshold: Int) {
        this.threshold = threshold

        val timestamp = DATE_TIME_FORMATTER.format(LocalDateTime.now())
        writeln("$timestamp Threshold changed to $threshold")
    }

    override fun onBrightnessChanged(value: Float) {
        if (threshold < 0) {
            // no threshold set
            return
        }

        if (value < 0.0f) {
            // invalid value, will not be logged
            lastValue = IBrightnessSensorService.INVALID_BRIGHTNESS_VALUE
            return
        }

        if (lastValue < 0.0f) {
            val timestamp = DATE_TIME_FORMATTER.format(LocalDateTime.now())
            writeln("$timestamp Initial value: $value lx")
            lastValue = value
            return
        }

        if (((lastValue < threshold) && (value >= threshold)) || // last value below threshold, but new value above
            ((lastValue >= threshold) && (value < threshold))) { // last value above threshold, but new value below
            val timestamp = DATE_TIME_FORMATTER.format(LocalDateTime.now())
            writeln("$timestamp Threshold crossed: $lastValue -> $value lx")
            lastValue = value
        }
    }

    open fun writeln(text: String) {
        Log.d(TAG, text)
    }
}
