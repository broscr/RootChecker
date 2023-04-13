package com.broscr.rootchecker

import android.os.Build
import android.util.Log
import java.io.DataOutputStream
import java.io.File

class RootChecker {
    companion object {
        private const val TAG = "RootChecker"

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

            return false
        }

        fun isEmulator(): Boolean {
            // Check for emulator
            if (Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            ) {
                return true
            }
            return false
        }
    }
}