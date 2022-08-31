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

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import org.digitalcure.brightnesstracker.logger.IBrightnessEventLogger
import org.digitalcure.brightnesstracker.prefs.IBrightnessTrackerPrefs

/**
 * Factory for the brightness tracker view model. Transfers the access to the logger and to the
 * preferences.
 */
class MainViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle,
    private val logger: IBrightnessEventLogger,
    private val prefs: IBrightnessTrackerPrefs
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return modelClass.getConstructor(
            IBrightnessEventLogger::class.java, IBrightnessTrackerPrefs::class.java, SavedStateHandle::class.java)
            .newInstance(logger, prefs, handle)
    }
}
