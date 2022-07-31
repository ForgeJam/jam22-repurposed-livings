import java.time.LocalDateTime

plugins {
    id("net.minecraftforge.gradle") version "5.1.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

group = "wtf.gofancy.mc"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("parchment", "1.18.2-2022.07.03-1.19.1")
    
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
    
    runs {
        create("client") {
            workingDirectory = file("run").canonicalPath

            properties(mapOf(
                "forge.logging.markers" to "REGISTRIES",
                "forge.logging.console.level" to "debug",
                "forge.enabledGameTestNamespaces" to "repurposed-livings"
            ))
        }
        
        create("server") {
            workingDirectory = file("run").canonicalPath
            
            properties(mapOf(
                "forge.logging.markers" to "REGISTRIES",
                "forge.logging.console.level" to "debug",
                "forge.enabledGameTestNamespaces" to "repurposed-livings"
            ))
        }
        
        create("gameTestServer") {
            workingDirectory = file("run").canonicalPath
                    
            properties(mapOf(
                "forge.logging.markers" to "REGISTRIES",
                "forge.logging.console.level" to "debug",
                "forge.enabledGameTestNamespaces" to "repurposed-livings"
            ))
        }

        create("data") {
            workingDirectory = file("run").canonicalPath
            
            properties(mapOf(
                "forge.logging.markers" to "REGISTRIES",
                "forge.logging.console.level" to "debug"
            ))
            
            args("--mod", "repurposedlivings", "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))
        }
    }
}

sourceSets.main.configure { resources.srcDir("src/generated/resources") }

repositories {
    maven {
        name = "Cursemaven"
        url = uri("https://cursemaven.com")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.19.1-42.0.0")
    
    compileOnly(fg.deobf("curse.maven:the-one-probe-245211:3871444"))
}

// Example for how to get properties into the manifest for reading at runtime.
tasks {
    jar {
        finalizedBy("reobfJar")
        manifest.attributes(
            "Specification-Title" to project.name,
            "Specification-Vendor" to "Su5ed, Nikx, Silk",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to archiveVersion.get(),
            "Implementation-Vendor" to "Su5ed, Nikx, Silk",
            "Implementation-Timestamp" to LocalDateTime.now()
        )
    }
    
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
