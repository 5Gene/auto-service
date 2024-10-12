plugins {
    alias(vcl.plugins.kotlin.jvm)
    alias(vcl.plugins.ksp)
}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(vcl.kotlinx.coroutines.core)
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
    implementation(vcl.google.auto.service.anno)
    ksp(project(":auto-service"))
}