package com.dmitrysukhov.expectactual

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform