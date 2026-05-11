plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.strataguard"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.jackson.module.kotlin)
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database
    runtimeOnly(libs.postgresql.driver)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.postgresql)

    // Firebase Admin (verify ID tokens from the app)
    implementation(libs.firebase.admin.sdk)

    // AWS S3 (evidence storage)
    implementation(libs.aws.s3.sdk)

    testImplementation(libs.spring.boot.starter.test)
}
