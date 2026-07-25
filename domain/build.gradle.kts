plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.paging.common)
    api(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit)
}
