plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:ui"))
    implementation(project(path = ":example:domain"))
    implementation(project(path = ":example:theNewThing:feature"))
    implementation(project(path = ":example:thePremiumThing:feature"))
}