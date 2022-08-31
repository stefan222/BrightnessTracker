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

import android.os.IBinder
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData

/**
 * Generic brightness tracker service. Tracks the light sensor and updates the raw brightness value.
 */
interface IBrightnessSensorService {
    companion object {
        const val INVALID_BRIGHTNESS_VALUE = -1.0f
    }

    val rawBrightnessValue: LiveData<Float>

    /**
     * Returns a localized string from the application's package's default string table.
     * @param resId resource id for the string
     * @return the string data associated with the resource, stripped of styled text information
     */
    fun getString(@StringRes resId: Int): String

    interface ILocalBinder : IBinder {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): IBrightnessSensorService
    }
}
