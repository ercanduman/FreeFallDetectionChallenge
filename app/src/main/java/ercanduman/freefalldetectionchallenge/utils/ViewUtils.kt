package ercanduman.freefalldetectionchallenge.utils

import android.util.Log
import ercanduman.freefalldetectionchallenge.BuildConfig

fun Any.logd(message: String) {
    if (BuildConfig.DEBUG) Log.d(this.javaClass.simpleName, message)
}