package com.dabada.downloader

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil {
    fun isPermissionGranted(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun requestPermissions(activity: Activity, requestCode: Int, vararg permissions: String) {
        val permissionList = java.util.ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission)
            }
        }
        if (permissionList.size > 0) {
            ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), requestCode)
        }
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }
        return false
    }
}