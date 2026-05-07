import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.apollo)
}

android {
    namespace = "com.rangerscards.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":domain"))

    // Apollo Kotlin
    implementation(libs.apollo.runtime)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Paging runtime
    implementation(libs.androidx.paging.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase (auth / other firebase data-source) - uses BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Serialization for data models
    implementation(libs.kotlinx.serialization.json)

    // Hilt in data layer: runtime and compiler
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

apollo {
    service("service") {
        // The package name for the generated models
        packageName.set("com.rangerscards")
        schemaFiles.from("src/main/graphql/schema.graphqls", "src/main/graphql/extra.graphqls")
        addTypename.set("always")
        mapScalarToKotlinString("timestamptz")
        mapScalar("jsonb", "kotlinx.serialization.json.JsonElement", "com.rangerscards.data.objects.JsonElementAdapter")
    }
}