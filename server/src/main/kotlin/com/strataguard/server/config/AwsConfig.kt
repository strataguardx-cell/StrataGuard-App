package com.strataguard.server.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class AwsConfig {

    @Value("\${aws.s3.region}")
    private lateinit var region: String

    @Value("\${aws.s3.endpoint:}")
    private lateinit var endpoint: String

    @Bean
    fun s3Client(): S3Client {
        val builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
        if (endpoint.isNotBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }
        return builder.build()
    }

    @Bean
    fun s3Presigner(): S3Presigner {
        val builder = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
        if (endpoint.isNotBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }
        return builder.build()
    }
}
