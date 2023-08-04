plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:models"))
    implementation(project(path = ":example:thePremiumThing:models"))
}