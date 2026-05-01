// Gradle 멀티모듈 설정 파일입니다.
// 현재는 Kotlin Spring Boot 백엔드 모듈만 포함하고, 프론트엔드는 npm 기반으로 별도 관리합니다.
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "trade-alarm-service"
include("backend")
