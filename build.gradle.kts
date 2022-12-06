plugins {
    id("java")
}

group = "net.defade"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.defade.net/defade") {
        name = "defade"
        credentials(PasswordCredentials::class)
    }
}

dependencies {
    compileOnly("net.defade:Yokura:1.19.2-R1.1-SNAPSHOT")
}
