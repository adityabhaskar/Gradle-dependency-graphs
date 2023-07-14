plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:ui"))
    implementation(project(path = ":example:domain"))
}