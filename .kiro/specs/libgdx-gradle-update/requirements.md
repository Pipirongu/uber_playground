# Requirements Document

## Introduction

This document specifies the requirements for updating the UberPlayground libGDX game project to use the latest stable versions of the libGDX framework and Gradle build system. The project currently uses libGDX 1.9.5 (released 2017) and Gradle 2.14.1 (released 2016), which are significantly outdated. The update will modernize the build tooling, dependency management, and framework capabilities while maintaining compatibility across Android, Desktop, and HTML modules.

## Glossary

- **Build_System**: The Gradle build automation system that compiles, packages, and manages dependencies for the project
- **libGDX_Framework**: The cross-platform game development framework (com.badlogicgames.gdx) used by the project
- **Android_Module**: The android/ subproject that builds the Android APK application
- **Desktop_Module**: The desktop/ subproject that builds the desktop JAR application using LWJGL backend
- **HTML_Module**: The html/ subproject that builds the web application using GWT backend
- **Core_Module**: The core/ subproject containing shared game logic used by all platform modules
- **Gradle_Wrapper**: The gradlew/gradlew.bat scripts and gradle/wrapper/ configuration that specify the Gradle version
- **Android_Gradle_Plugin**: The com.android.tools.build:gradle plugin that enables Android builds
- **GWT_Plugin**: The de.richsource.gradle.plugins:gwt-gradle-plugin that enables HTML/web builds
- **Native_Dependencies**: Platform-specific native libraries (e.g., .so files for Android, .dll/.dylib for desktop)
- **Dependency_Configuration**: The dependencies block in build.gradle files specifying library versions
- **Build_Configuration**: The complete set of Gradle configuration files including build.gradle, gradle.properties, and settings.gradle

## Requirements

### Requirement 1: Update Gradle Version

**User Story:** As a developer, I want to use a modern Gradle version, so that I can benefit from improved build performance, better dependency management, and current tooling support.

#### Acceptance Criteria

1. THE Build_System SHALL use Gradle version 8.0 or higher
2. THE Gradle_Wrapper SHALL be updated to reference the new Gradle distribution URL
3. WHEN the gradlew command is executed, THE Build_System SHALL download and use the updated Gradle version
4. THE Build_Configuration SHALL use current Gradle syntax compatible with the updated version
5. THE Build_System SHALL successfully resolve all project dependencies using the updated Gradle version

### Requirement 2: Update libGDX Framework Version

**User Story:** As a developer, I want to use the latest stable libGDX framework, so that I can access current features, bug fixes, and performance improvements.

#### Acceptance Criteria

1. THE Dependency_Configuration SHALL specify libGDX version 1.12.0 or higher
2. THE Core_Module SHALL use the updated libGDX core library
3. THE Desktop_Module SHALL use the updated libGDX LWJGL3 backend (replacing deprecated LWJGL2)
4. THE Android_Module SHALL use the updated libGDX Android backend
5. THE HTML_Module SHALL use the updated libGDX GWT backend
6. FOR ALL libGDX extension libraries (box2d, box2dlights, gdx-ai), THE Dependency_Configuration SHALL use versions compatible with the updated libGDX version

### Requirement 3: Update Android Build Configuration

**User Story:** As a developer, I want to use modern Android build tools, so that I can target current Android versions and use up-to-date SDK features.

#### Acceptance Criteria

1. THE Android_Gradle_Plugin SHALL use version 8.0 or higher
2. THE Android_Module SHALL specify compileSdkVersion 33 or higher
3. THE Android_Module SHALL specify targetSdkVersion 33 or higher
4. THE Android_Module SHALL specify minSdkVersion 21 or higher
5. THE Android_Module SHALL remove deprecated armeabi native architecture support
6. THE Android_Module SHALL retain support for armeabi-v7a, arm64-v8a, x86, and x86_64 architectures
7. THE Native_Dependencies SHALL be correctly extracted to the appropriate libs/ folders for each architecture

### Requirement 4: Update GWT Configuration

**User Story:** As a developer, I want to use a compatible GWT plugin and version, so that the HTML module builds successfully with the updated libGDX framework.

#### Acceptance Criteria

1. THE GWT_Plugin SHALL use a version compatible with Gradle 8.0 or higher
2. THE HTML_Module SHALL specify a GWT version compatible with the updated libGDX framework
3. WHEN the HTML module is built, THE Build_System SHALL successfully compile the GWT application
4. THE HTML_Module SHALL include all required source dependencies for GWT compilation

### Requirement 5: Update Repository Configuration

**User Story:** As a developer, I want to use current Maven repositories, so that dependency resolution works reliably without deprecated repository warnings.

#### Acceptance Criteria

1. THE Build_Configuration SHALL remove the deprecated jcenter() repository
2. THE Build_Configuration SHALL use mavenCentral() as the primary repository
3. WHERE snapshot dependencies are needed, THE Build_Configuration SHALL include the Sonatype snapshots repository
4. WHEN dependencies are resolved, THE Build_System SHALL not produce deprecation warnings about repositories

### Requirement 6: Update Java Compatibility

**User Story:** As a developer, I want to use a modern Java version, so that I can leverage current language features and maintain compatibility with updated tools.

#### Acceptance Criteria

1. THE Build_Configuration SHALL specify sourceCompatibility of Java 11 or higher
2. THE Build_Configuration SHALL specify targetCompatibility of Java 11 or higher
3. THE Build_System SHALL successfully compile all modules with the specified Java version
4. THE Desktop_Module SHALL run successfully with Java 11 or higher

### Requirement 7: Preserve Build Tasks

**User Story:** As a developer, I want to retain all existing build tasks, so that my development workflow remains consistent after the update.

#### Acceptance Criteria

1. THE Desktop_Module SHALL provide a 'run' task that executes the desktop application
2. THE Desktop_Module SHALL provide a 'dist' task that creates a distributable JAR
3. THE Android_Module SHALL provide a 'run' task that launches the application on a connected device
4. THE Android_Module SHALL provide a 'copyAndroidNatives' task that extracts native libraries
5. THE HTML_Module SHALL provide a 'dist' task that creates the web application output
6. THE HTML_Module SHALL provide a 'superDev' task for GWT super dev mode
7. WHEN any build task is executed, THE Build_System SHALL complete successfully or provide clear error messages

### Requirement 8: Maintain Module Dependencies

**User Story:** As a developer, I want all module dependencies to remain correctly configured, so that the project structure and inter-module relationships are preserved.

#### Acceptance Criteria

1. THE Desktop_Module SHALL depend on the Core_Module
2. THE Android_Module SHALL depend on the Core_Module
3. THE HTML_Module SHALL depend on the Core_Module
4. THE Build_Configuration SHALL use current Gradle dependency syntax (implementation/api instead of deprecated compile)
5. FOR ALL modules, THE Dependency_Configuration SHALL correctly specify transitive dependencies

### Requirement 9: Validate Build Success

**User Story:** As a developer, I want to verify that the updated build configuration works correctly, so that I can confidently use the updated project.

#### Acceptance Criteria

1. WHEN './gradlew clean build' is executed, THE Build_System SHALL complete without errors
2. WHEN './gradlew desktop:run' is executed, THE Desktop_Module SHALL launch the application
3. WHEN './gradlew android:assembleDebug' is executed, THE Android_Module SHALL create a valid APK
4. WHEN './gradlew html:dist' is executed, THE HTML_Module SHALL create the web application output
5. FOR ALL build outputs, THE Build_System SHALL include all required dependencies and assets

### Requirement 10: Update Gradle Properties

**User Story:** As a developer, I want optimized Gradle build properties, so that builds execute efficiently with the updated Gradle version.

#### Acceptance Criteria

1. THE gradle.properties file SHALL specify appropriate JVM heap size settings for the updated Gradle version
2. THE gradle.properties file SHALL enable the Gradle daemon for improved build performance
3. WHERE applicable, THE gradle.properties file SHALL include Android-specific properties (e.g., AndroidX migration flags)
4. THE Build_System SHALL respect all configured properties during build execution
