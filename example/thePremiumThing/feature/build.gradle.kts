plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:thePremiumThing:ui"))
    implementation(project(path = ":example:thePremiumThing:domain"))
}