plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":data"))
    api(project(path = ":models"))
}