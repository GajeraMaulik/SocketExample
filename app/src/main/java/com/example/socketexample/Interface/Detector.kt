package com.example.socketexample.Interface

import android.content.Context

interface Detector {
    fun getForegroundApp(var1: Context?): String?
}