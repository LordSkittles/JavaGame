plugins {
    java
    application
}

group = project.property("project_group") as String
version = project.property("project_version") as String
base.archivesName.set(project.property("project_name") as String)

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:${project.property("lwjgl_version")}"))
    implementation("com.moandjiezana.toml:toml4j:${project.property("toml_version")}")
    implementation("org.joml:joml:${property("joml_version")}")

    // Base LWJGL module (always included)
    implementation("org.lwjgl:lwjgl")
    runtimeOnly("org.lwjgl:lwjgl::${project.property("lwjgl_natives")}")

    // Add additional LWJGL modules dynamically from properties
    val lwjglModules = (project.property("lwjgl_modules") as String).split(",")
    lwjglModules.forEach { module ->
        val trimmedModule = module.trim()
        implementation("org.lwjgl:lwjgl-$trimmedModule")
        runtimeOnly("org.lwjgl:lwjgl-$trimmedModule::${project.property("lwjgl_natives")}")
    }
}

application {
    mainClass.set("${project.property("project_group")}.${project.property("main_class")}")
}

// Custom run configurations
tasks.register<JavaExec>("runDebug") {
    group = "application"
    description = "Run the application with debug settings"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("${project.property("project_group")}.${project.property("main_class")}")
    jvmArgs = listOf(
        "-Xmx2G",
        "-Xms1G",
        "-XX:+UseG1GC",
        "-Dorg.lwjgl.util.Debug=true",
        "-Dorg.lwjgl.util.DebugLoader=true"
    )
}

tasks.register<JavaExec>("runRelease") {
    group = "application"
    description = "Run the application with release settings"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("${project.property("project_group")}.${project.property("main_class")}")
    jvmArgs = listOf(
        "-Xmx4G",
        "-Xms2G",
        "-XX:+UseG1GC",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseZGC"
    )
}

tasks.register<JavaExec>("runDev") {
    group = "application"
    description = "Run the application with development settings"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("${project.property("project_group")}.${project.property("main_class")}")
    jvmArgs = listOf(
        "-Xmx1G",
        "-Dorg.lwjgl.util.Debug=true"
    )
    // Enable hot reload if available
    systemProperty("java.awt.headless", "false")
}

// Configure default run task
tasks.named<JavaExec>("run") {
    jvmArgs = listOf(
        "-Xmx2G",
        "-XX:+UseG1GC"
    )
}

// JAR configuration
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "${project.property("project_group")}.${project.property("main_class")}",
            "Implementation-Title" to project.property("project_name"),
            "Implementation-Version" to project.property("project_version")
        )
    }
}

// Fat JAR for distribution
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Create a fat JAR with all dependencies"
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes(
            "Main-Class" to "${project.property("project_group")}.${project.property("main_class")}",
            "Implementation-Title" to project.property("project_name"),
            "Implementation-Version" to project.property("project_version")
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}