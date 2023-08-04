pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    `gradle-enterprise`
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlwaysIf(System.getenv("GITHUB_ACTIONS") == "true")
        publishOnFailure()
    }
}

rootProject.name = "gradle-module-dependency-graph-plugin"

include(":example:app")
include(":example:ui")
include(":example:shared-ui")
include(":example:domain")
include(":example:data")
include(":example:models")
include(":example:system-test")
include(":example:theNewThing:feature")
include(":example:theNewThing:ui")
include(":example:theNewThing:domain")
include(":example:theNewThing:data")
include(":example:theNewThing:models")
include(":example:thePremiumThing:feature")
include(":example:thePremiumThing:ui")
include(":example:thePremiumThing:domain")
include(":example:thePremiumThing:data")
include(":example:thePremiumThing:models")
include(":example:system-test")
includeBuild("plugin-build")