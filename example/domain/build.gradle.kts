plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:data"))
    api(project(path = ":example:models"))
}