plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:data"))
    implementation(project(path = ":example:ui"))
    api(project(path = ":example:models"))
}