package com.strataguard.app.data.remote

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient

expect val serverBaseUrl: String
