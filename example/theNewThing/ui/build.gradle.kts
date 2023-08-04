plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:models"))
    implementation(project(path = ":example:theNewThing:models"))
    implementation(project(path = ":example:shared-ui"))
}