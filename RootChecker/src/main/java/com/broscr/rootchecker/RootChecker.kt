package com.broscr.rootchecker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

class RootChecker {
    companion object {
        private const val TAG = "RootChecker"

        /** If the device is rooted, it returns true.
         *
         */
        fun isRooted(): Boolean {
            // Check for init.d directory and installation method
            val initDir = File("/system/etc/init.d")
            if (initDir.exists() && initDir.isDirectory) {
                // init.d directory exists, which is a common installation method for root apps
                return true
            }

            val potentialRootPaths = arrayListOf(
                "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/",
                "/data/local/"
            )

            // Check for common root files
            for (path in potentialRootPaths) {
                if (File(path + "su").exists() || File(path + "busybox").exists()) {
                    return true
                }
            }

            // Check for custom-built binaries
            val pathsSplit = System.getenv("PATH")?.split(":")
            if (pathsSplit != null) {
                for (path in pathsSplit) {
                    if (File("$path/su").exists() || File("$path/busybox").exists()) {
                        return true
                    }
                }
            }

            // Check for root permission
            try {
                val process = Runtime.getRuntime().exec("su")
                val outputStream = DataOutputStream(process.outputStream)
                outputStream.writeBytes("exit\n")
                outputStream.flush()
                process.waitFor()
                if (process.exitValue() == 0) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, e.message.toString())
            }

            // Check for system properties related to root
            val rootProperties = arrayOf(
                "ro.debuggable",
                "ro.secure"
            )
            for (property in rootProperties) {
                val propValue = getSystemProperty(property)
                if (propValue != null && propValue == "1") {
                    return true
                }
            }

            return false
        }

        private fun getSystemProperty(propName: String): String? {
            try {
                val process = Runtime.getRuntime().exec("getprop $propName")
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                return bufferedReader.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, e.message.toString())
                return null
            }
        }

        /** If the device is an emulator, it returns true.
         *
         */
        fun isEmulator(): Boolean {
            val emulatorIndicators = listOf(
                "generic",
                "unknown",
                "google_sdk",
                "Emulator",
                "Android SDK built for x86",
                "Genymotion",
                "sdk_gphone",
                "ranchu",
                "vbox86",
                "sdk",
                "Andy",
                "Droid4X",
                "ttVM_Hdragon",
                "vbox86p"
            )

            // Check for emulator
            if (emulatorIndicators.any { Build.FINGERPRINT.contains(it, true) } ||
                emulatorIndicators.any { Build.MODEL.contains(it, true) } ||
                emulatorIndicators.any { Build.MANUFACTURER.contains(it, true) } ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT ||
                Build.HARDWARE.contains("goldfish", true) ||
                Build.HARDWARE.contains("ranchu", true)
            ) {
                return true
            }
            return false
        }

        /** If the device using proxy, it returns true.
         *
         */
        fun isUsingProxy(): Boolean {
            // Check for proxy
            val proxyHost =
                System.getProperty("https.proxyHost") ?: System.getProperty("http.proxyHost")
            val proxyPort =
                System.getProperty("https.proxyPort") ?: System.getProperty("http.proxyPort")

            return !proxyHost.isNullOrBlank() && !proxyPort.isNullOrBlank()
        }

        /** If the application is debuggable, it returns true
         *
         * @param Context
         */
        fun isDebuggable(context: Context): Boolean {
            return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }
    }
}