plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:theNewThing:data"))
    api(project(path = ":example:theNewThing:models"))
}