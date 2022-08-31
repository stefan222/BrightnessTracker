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

package org.digitalcure.brightnesstracker.prefs

import android.content.Context
import android.content.SharedPreferences
import org.digitalcure.brightnesstracker.logger.IBrightnessEventLogger
import java.lang.ref.WeakReference

/**
 * Preferences implementation using the Android shared preferences.
 */
class BrightnessTrackerPrefs(
    context: Context
) : IBrightnessTrackerPrefs {
    companion object {
        private const val PREFS_KEY_THRESHOLD = "threshold"
    }

    private val contextRef = WeakReference(context)

    override fun getThreshold(): Int? {
        val prefs = getPrefs() ?: return null
        val value = prefs.getInt(PREFS_KEY_THRESHOLD, IBrightnessEventLogger.INVALID_THRESHOLD)
        return if (value < 0) null else value
    }

    override fun setThreshold(value: Int) {
        val prefs = getPrefs() ?: return
        with (prefs.edit()) {
            putInt(PREFS_KEY_THRESHOLD, value)
            apply()
        }
    }

    private fun getPrefs(): SharedPreferences? {
        val context = contextRef.get() ?: return null
        return context.getSharedPreferences("BrightnessTracker", Context.MODE_PRIVATE)
    }
}
