plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:theNewThing:ui"))
    implementation(project(path = ":example:theNewThing:domain"))
}