plugins {
    alias(vcl.plugins.kotlin.jvm)
    alias(vcl.plugins.ksp)
}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    //upToDate设置为false, 每次都执行ksp任务
    outputs.upToDateWhen { false }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(vcl.kotlinx.coroutines.core)
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
    implementation(vcl.google.auto.service.anno)
    ksp(project(":auto-service"))
}