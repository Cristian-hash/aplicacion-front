package org.frontapp.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// expect fun multiplicacion(): Int