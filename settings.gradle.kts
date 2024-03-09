pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
// 호출
//project(":externalLibrary").projectDir =
//    File(getExternalModuleDir())
//
//// 메서드 선언
//fun getExternalModuleDir(): String {
//    val properties = java.util.Properties()
//    properties.load(File(rootDir.absolutePath + "/local.properties").inputStream())
//    val externalLibraryPath = properties["youtubeDataApiKey"]
//    return externalLibraryPath.toString()
//}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io" ) }
    }
}

rootProject.name = "Clicker"
include(":app")
 