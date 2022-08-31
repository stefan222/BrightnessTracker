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

/**
 * Generic logger interface. Logs events, such as the start of the service, the stop of the service,
 * a new threshold, and the crossing of the threshold.
 */
interface IBrightnessEventLogger {
    companion object {
        const val INVALID_THRESHOLD = -1
    }

    fun onServiceStarted()

    fun onServiceStopped()

    fun setThreshold(threshold: Int)

    fun onBrightnessChanged(value: Float)
}
