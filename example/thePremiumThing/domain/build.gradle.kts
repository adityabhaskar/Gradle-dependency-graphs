plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(path = ":example:thePremiumThing:data"))
    api(project(path = ":example:thePremiumThing:models"))
}