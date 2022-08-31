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

package org.digitalcure.brightnesstracker.ui

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.digitalcure.brightnesstracker.R
import org.digitalcure.brightnesstracker.logger.IBrightnessEventLogger
import org.digitalcure.brightnesstracker.prefs.IBrightnessTrackerPrefs
import org.digitalcure.brightnesstracker.service.IBrightnessSensorService

/**
 * View model of the brightness tracker app. Does all the communication between service and UI.
 */
class MainViewModel(
    private val logger: IBrightnessEventLogger,
    private val prefs: IBrightnessTrackerPrefs,
    private val state: SavedStateHandle
) : ViewModel() {
    companion object {
        private val TAG = MainViewModel::class.java.name

        private const val ROUNDING_FACTOR = 10.0f // 10 means 1 fraction digit

        private const val UNKNOWN_BRIGHTNESS_VALUE_TEXT = "---"

        private const val DEFAULT_THRESHOLD = 100

        private const val SAVED_STATE_KEY_THRESHOLD = "threshold"
    }

    private var serviceObserver: ServiceObserver? = null

    val brightnessText: State<String> = mutableStateOf(UNKNOWN_BRIGHTNESS_VALUE_TEXT)

    private val threshold = MutableLiveData<Int>()

    val thresholdText: State<String> = mutableStateOf("")

    init {
        threshold.observeForever { value ->
            logger.setThreshold(value)

            if (value < 0) {
                (thresholdText as MutableState).value = ""
                state[SAVED_STATE_KEY_THRESHOLD] = IBrightnessEventLogger.INVALID_THRESHOLD
                prefs.setThreshold(IBrightnessEventLogger.INVALID_THRESHOLD)
            } else {
                (thresholdText as MutableState).value = value.toString()
                state[SAVED_STATE_KEY_THRESHOLD] = value
                prefs.setThreshold(value)
            }
        }

        val oldThreshold = state.get<Int>(SAVED_STATE_KEY_THRESHOLD)
            ?: prefs.getThreshold()
            ?: DEFAULT_THRESHOLD
        if (oldThreshold != threshold.value) {
            threshold.value = oldThreshold
        }
    }

    fun bindToService(service: IBrightnessSensorService) {
        unbindFromService()

        serviceObserver = ServiceObserver(service)
        Log.d(TAG, "ViewModel is bound to service.")

        logger.onServiceStarted()
        logger.setThreshold(threshold.value!!)
    }

    fun unbindFromService() {
        serviceObserver?.let {
            it.unbind()
            serviceObserver = null
            (brightnessText as MutableState).value = UNKNOWN_BRIGHTNESS_VALUE_TEXT
            Log.d(TAG, "ViewModel is disconnected from service.")

            logger.onServiceStopped()
        }
    }

    fun setThresholdValue(value: String) {
        if (value.isBlank()) {
            threshold.value = IBrightnessEventLogger.INVALID_THRESHOLD
            return
        }

        try {
            val valueInt = value.toInt()
            if (valueInt >= 0) {
                threshold.value = valueInt
                Log.d(TAG, "New threshold: $valueInt")
            }
        } catch (nfe: NumberFormatException) {
            Log.e(TAG, "Ignoring invalid threshold value: $value")
        }
    }

    override fun onCleared() {
        unbindFromService()
    }

    private inner class ServiceObserver(
        private val service: IBrightnessSensorService
    ) : Observer<Float> {
        init {
            service.rawBrightnessValue.observeForever(this)
        }

        override fun onChanged(newValue: Float?) {
            val value = newValue ?: IBrightnessSensorService.INVALID_BRIGHTNESS_VALUE
            val roundedValue = (value * ROUNDING_FACTOR).toInt() / ROUNDING_FACTOR

            (brightnessText as MutableState).value = if (roundedValue >= 0.0f) {
                "$roundedValue ${service.getString(R.string.label_lx)}"
            } else {
                UNKNOWN_BRIGHTNESS_VALUE_TEXT
            }

            logger.onBrightnessChanged(roundedValue)
        }

        fun unbind() {
            service.rawBrightnessValue.removeObserver(this)
        }
    }
}
