package com.strataguard.server.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    @Value("\${firebase.project-id}")
    private lateinit var projectId: String

    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isNotEmpty()) return

        // In local/dev: uses GOOGLE_APPLICATION_CREDENTIALS env var or ADC
        // In prod: uses service account attached to the compute instance
        val credentials = runCatching {
            GoogleCredentials.getApplicationDefault()
        }.getOrElse {
            // Fallback for environments without ADC — Firebase still works for token verification
            // using the project ID alone (public keys are fetched from Google)
            GoogleCredentials.newBuilder().build()
        }

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build()

        FirebaseApp.initializeApp(options)
    }
}
