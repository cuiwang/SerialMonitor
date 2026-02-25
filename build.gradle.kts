plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.serialmonitor"
version = "1.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Serial communication library - 使用jSerialComm（更可靠）
    implementation("com.fazecast:jSerialComm:[2.8.0,2.9.0)")

    // 图表库 - 用于绘制实时数据
    implementation("org.knowm.xchart:xchart:3.8.7")

    // 注意: Kotlin stdlib 和 Kotlin Coroutines 已由 IntelliJ Platform 提供，无需显式添加

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

// Configure IntelliJ Platform Plugin
intellij {
    version.set("2024.1")
    type.set("IC") // IntelliJ IDEA Community (更容易获取)
    // 或者也可以工作在CLion中，因为都是基于IntelliJ平台

    pluginName.set("Serial Monitor")
    downloadSources.set(false)
    instrumentCode.set(true)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    // Patch plugin.xml
    patchPluginXml {
        version.set("1.1.0")
        sinceBuild.set("241")
        untilBuild.set("261.*") // 支持到最新的CLion 261版本
    }

    // Run IDE task
    runIde {
        enabled = true
    }
}











