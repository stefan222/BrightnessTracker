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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import org.digitalcure.brightnesstracker.R
import org.digitalcure.brightnesstracker.logger.BrightnessEventFileLogger
import org.digitalcure.brightnesstracker.logger.BrightnessEventLogcatLogger
import org.digitalcure.brightnesstracker.prefs.BrightnessTrackerPrefs
import org.digitalcure.brightnesstracker.service.BrightnessSensorService
import org.digitalcure.brightnesstracker.service.IBrightnessSensorService
import org.digitalcure.brightnesstracker.ui.theme.BrightnessTrackerTheme
import java.io.File

/**
 * The main activity of the brightness tracker application. It defines the Compose UI, handles the
 * foreground service, and connects all components.
 * @see "https://developer.android.com/jetpack/compose/state"
 */
class MainActivity : ComponentActivity() {
    companion object {
        private val TAG = MainActivity::class.java.name

        private const val LOG_FILE_NAME = "brightness_log.txt"
    }

    private lateinit var viewModel: MainViewModel

    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service is bound.")

            // we've bound to the service, cast the IBinder and get service instance
            isServiceBound = true
            val service = (binder as IBrightnessSensorService.ILocalBinder).getService()
            viewModel.bindToService(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, "Service is disconnected.")
            viewModel.unbindFromService()
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // determine log file and start logging
        val extAppFolder = getExternalAppFolder()
        val logger = if (extAppFolder == null) {
            Log.e(TAG, "No external app folder found! Switching to logcat.")
            Toast.makeText(this, R.string.error_no_external_app_folder, Toast.LENGTH_LONG).show()
            BrightnessEventLogcatLogger()
        } else {
            val loggerFile = File(extAppFolder, LOG_FILE_NAME)
            BrightnessEventFileLogger(loggerFile)
        }

        // create / get view model instance
        val prefs = BrightnessTrackerPrefs(this)
        val vmFactory = MainViewModelFactory(this, Bundle(), logger, prefs)
        viewModel = ViewModelProvider(this, vmFactory)[MainViewModel::class.java]

        // Compose UI
        setContent {
            val vm: MainViewModel = viewModel()
            val brightnessText = vm.brightnessText
            val thresholdText = vm.thresholdText

            BrightnessTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = brightnessText.value, fontSize = 48.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(shape = MaterialTheme.shapes.medium, onClick = { pressedStartServiceButton() }) {
                                Text(text = stringResource(R.string.button_start_service))
                            }
                            Box(modifier = Modifier.width(16.dp))
                            Button(shape = MaterialTheme.shapes.medium, onClick = { pressedStopServiceButton() }) {
                                Text(text = stringResource(R.string.button_stop_service))
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                        ) {
                            Text(text= stringResource(R.string.label_threshold))
                            Box(modifier = Modifier.width(12.dp))
                            TextField(
                                value = thresholdText.value,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                onValueChange = viewModel::setThresholdValue,
                                modifier = Modifier.width(100.dp))
                            Box(modifier = Modifier.width(12.dp))
                            Text(text= stringResource(R.string.label_lx))
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    private fun bindService() {
        // Build the intent for the service
        val intent = Intent(this, BrightnessSensorService::class.java)
        applicationContext.startForegroundService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        if (isServiceBound) {
            isServiceBound = false
            viewModel.unbindFromService()
            unbindService(serviceConnection)
        }
    }

    private fun stopService() {
        unbindService()

        val intent = Intent(this, BrightnessSensorService::class.java)
        applicationContext.stopService(intent)
    }

    private fun pressedStartServiceButton() {
        if (!isServiceBound) {
            bindService()
        }
    }

    private fun pressedStopServiceButton() {
        if (isServiceBound) {
            stopService()
        }
    }

    private fun getExternalAppFolder(): File? {
        val paths = ContextCompat.getExternalFilesDirs(this, null)
        return if (paths.isEmpty()) {
            null
        } else if (paths.size > 1) {
            paths[1] // for compatibility reasons
        } else {
            paths[0]
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    BrightnessTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "123.4 lx", fontSize = 48.sp)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(shape = MaterialTheme.shapes.medium, onClick = {}) {
                        Text(text = stringResource(R.string.button_start_service))
                    }
                    Box(modifier = Modifier.width(16.dp))
                    Button(shape = MaterialTheme.shapes.medium, onClick = {}) {
                        Text(text = stringResource(R.string.button_stop_service))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                ) {
                    Text(text= stringResource(R.string.label_threshold))
                    Box(modifier = Modifier.width(12.dp))
                    TextField(value = "100", onValueChange = {}, modifier = Modifier.width(100.dp))
                    Box(modifier = Modifier.width(12.dp))
                    Text(text= stringResource(R.string.label_lx))
                }
            }
        }
    }
}
