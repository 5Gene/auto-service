plugins {
    //https://github.com/google/ksp/releases
    //libs.plugins.android.application必须在最上面
    alias(libs.plugins.android.application)
    alias(wings.plugins.compose)
    alias(libs.plugins.ksp)
}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}
android {
    namespace = "com.example.auto.service"
}

dependencies {
    ksp(project(":auto-service"))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.bundles.androidx.benchmark)
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
}
