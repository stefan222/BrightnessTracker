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

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

/**
 * Logs all brightness tracker events into a file AND the logcat. Please note that the log messages
 * are not internationalized.
 */
class BrightnessEventFileLogger(
    private val file: File
) : BrightnessEventLogcatLogger() {
    companion object {
        private val SYNC = Any()
    }

    override fun writeln(text: String) {
        super.writeln(text)

        synchronized(SYNC) {
            FileWriter(file, true).use { fileWriter ->
                PrintWriter(fileWriter).use { printWriter ->
                    printWriter.println(text)
                }
            }
        }
    }

    fun clear() {
        synchronized(SYNC) {
            FileWriter(file, false).use { fileWriter ->
                PrintWriter(fileWriter).use { printWriter ->
                    printWriter.print("")
                }
            }
        }
    }
}
