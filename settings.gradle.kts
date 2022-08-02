pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { 
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net")
        }
        maven { 
            name = "Parchment"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Garden of Fancy"
            url = uri("https://maven.gofancy.wtf/releases")
        }
    }
}

plugins {
    id("wtf.gofancy.convention.buildcache") version "0.1.0"
}

rootProject.name = "repurposed-livings"
