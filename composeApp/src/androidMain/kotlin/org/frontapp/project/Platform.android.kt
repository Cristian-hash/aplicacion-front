package org.frontapp.project

import android.R
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()


/*class implMulti{
    fun multi() {
        val a: Int = 1;
        val b: Int = 1;
        return a * b;
    }
}


actual fun multiplicacion(): Int = implMulti().multi()*/