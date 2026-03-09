# Implementation Plan: libGDX and Gradle Update

## Overview

This plan implements a coordinated update of the UberPlayground project from libGDX 1.9.5 and Gradle 2.14.1 to modern versions (Gradle 8.5, libGDX 1.12.1). The migration follows a 7-phase approach to minimize build failures, starting with the Gradle wrapper, then updating build configurations, and finally updating each module in dependency order (core → desktop → android → html).

## Tasks

- [ ] 1. Phase 1: Update Gradle Wrapper
  - [x] 1.1 Update gradle-wrapper.properties to Gradle 8.5
    - Modify `gradle/wrapper/gradle-wrapper.properties`
    - Change `distributionUrl` to `https\://services.gradle.org/distributions/gradle-8.5-all.zip`
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [ ]* 1.2 Verify Gradle wrapper update
    - Run `./gradlew --version` and confirm Gradle 8.5 is being used
    - _Requirements: 1.3_

- [ ] 2. Phase 2: Update Root Build Configuration
  - [x] 2.1 Update root build.gradle buildscript and repositories
    - Update Android Gradle Plugin to 8.2.0 in buildscript dependencies
    - Add Gretty plugin 4.1.0 to buildscript dependencies
    - Remove jcenter() from all repositories blocks
    - Add google() repository where needed
    - _Requirements: 3.1, 5.1, 5.2, 5.3, 5.4_
  
  - [x] 2.2 Update version properties in root build.gradle
    - Set `gdxVersion = '1.12.1'`
    - Set `box2DLightsVersion = '1.5'`
    - Set `ashleyVersion = '1.7.4'`
    - Set `aiVersion = '1.8.2'`
    - _Requirements: 2.1, 2.6_
  
  - [x] 2.3 Update gradle.properties
    - Increase `org.gradle.jvmargs` to `-Xms256m -Xmx2048m`
    - Set `org.gradle.daemon=true`
    - Set `org.gradle.configureondemand=false`
    - Add `org.gradle.caching=true`
    - Add `org.gradle.parallel=true`
    - Add `android.useAndroidX=true`
    - Add `android.enableJetifier=false`
    - Add `android.nonTransitiveRClass=true`
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [ ] 3. Phase 3: Update Core Module
  - [x] 3.1 Update core/build.gradle
    - Set `sourceCompatibility = 11`
    - Set `targetCompatibility = 11`
    - Replace all `compile` dependencies with `api` (core exposes these to dependent modules)
    - _Requirements: 6.1, 6.2, 8.4, 8.5, 2.2_
  
  - [ ]* 3.2 Verify core module builds
    - Run `./gradlew core:build` and confirm success
    - _Requirements: 6.3, 9.1_

- [ ] 4. Phase 4: Update Desktop Module
  - [x] 4.1 Update desktop/build.gradle dependencies
    - Set `sourceCompatibility = 11`
    - Set `targetCompatibility = 11`
    - Replace `compile` with `implementation`
    - Change `com.badlogicgames.gdx:gdx-backend-lwjgl` to `com.badlogicgames.gdx:gdx-backend-lwjgl3`
    - Update all dependency references to use `$gdxVersion`
    - _Requirements: 6.1, 6.2, 8.1, 8.4, 2.3_
  
  - [x] 4.2 Update DesktopLauncher.java for LWJGL3
    - Change imports from `com.badlogic.gdx.backends.lwjgl` to `com.badlogic.gdx.backends.lwjgl3`
    - Replace `LwjglApplication` with `Lwjgl3Application`
    - Replace `LwjglApplicationConfiguration` with `Lwjgl3ApplicationConfiguration`
    - Replace `config.width` and `config.height` with `config.setWindowedMode(400, 240)`
    - Add `config.setTitle("Uber-Playground")`
    - _Requirements: 2.3_
  
  - [ ]* 4.3 Test desktop module execution
    - Run `./gradlew desktop:run` and verify application launches
    - _Requirements: 6.4, 7.1, 9.2_

- [ ] 5. Phase 5: Update Android Module
  - [x] 5.1 Update android/build.gradle android configuration block
    - Add `namespace 'com.plv.uberplayground'` at the top of android block
    - Change `compileSdkVersion 25` to `compileSdk 34`
    - Update defaultConfig: `minSdk 21`, `targetSdk 34`, add `applicationId "com.plv.uberplayground"`
    - Remove `buildToolsVersion` line (auto-selected by AGP 8.x)
    - Add `compileOptions` block with `sourceCompatibility JavaVersion.VERSION_11` and `targetCompatibility JavaVersion.VERSION_11`
    - Update `packagingOptions` with additional exclusions for META-INF files
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 6.1, 6.2_
  
  - [x] 5.2 Update android/build.gradle dependencies
    - Replace `compile` with `implementation`
    - Remove `natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi"`
    - Remove `natives "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi"`
    - Ensure natives for armeabi-v7a, arm64-v8a, x86, and x86_64 are present for both gdx-platform and gdx-box2d-platform
    - _Requirements: 3.5, 3.6, 8.2, 8.4_
  
  - [x] 5.3 Update copyAndroidNatives task
    - Remove `file("libs/armeabi/").mkdirs();` line
    - Remove the conditional block that handles "natives-armeabi.jar"
    - Ensure task creates directories for armeabi-v7a, arm64-v8a, x86_64, and x86
    - Add `tasks.whenTaskAdded` hook to ensure copyAndroidNatives runs before package tasks
    - _Requirements: 3.7, 7.4_
  
  - [ ]* 5.4 Test Android module build
    - Run `./gradlew android:assembleDebug` and verify APK is created
    - _Requirements: 9.3_

- [ ] 6. Phase 6: Update HTML Module
  - [x] 6.1 Update html/build.gradle plugin application
    - Ensure `apply plugin: "java"`, `apply plugin: "war"`, and `apply plugin: "org.gretty"` are present
    - _Requirements: 4.1_
  
  - [x] 6.2 Add GWT configuration block to html/build.gradle
    - Add `gwt` configuration block with `gwtVersion = '2.10.0'`
    - Set `maxHeapSize = '2G'` and `minHeapSize = '1G'`
    - Configure `src`, `modules`, and `devModules` properties
    - Add `compiler` block with strict mode and closure compiler settings
    - _Requirements: 4.2_
  
  - [x] 6.3 Add Gretty configuration block to html/build.gradle
    - Add `gretty` configuration block with `httpPort = 8080`
    - Set `contextPath = "/"` and `servletContainer = "jetty11"`
    - _Requirements: 4.1_
  
  - [x] 6.4 Update html/build.gradle dependencies
    - Replace `compile` with `implementation`
    - Ensure all runtime dependencies have corresponding `:sources` classifier dependencies
    - Update all dependency versions to use `$gdxVersion` and other version variables
    - _Requirements: 4.4, 8.3, 8.4, 2.5_
  
  - [x] 6.5 Update HTML module tasks
    - Update or create `dist` task that depends on clean and compileGwt
    - Update or create `superDev` task for GWT super dev mode
    - _Requirements: 7.5, 7.6_
  
  - [ ]* 6.6 Test HTML module build
    - Run `./gradlew html:dist` and verify dist folder is created
    - _Requirements: 4.3, 9.4_

- [ ] 7. Phase 7: Final Verification and Testing
  - [x] 7.1 Run complete clean build
    - Execute `./gradlew clean build` and verify all modules build successfully
    - Check build output for any deprecation warnings
    - _Requirements: 1.4, 1.5, 9.1_
  
  - [ ]* 7.2 Write property test for dependency resolution
    - **Property 2: Dependency Resolution Success**
    - **Validates: Requirements 1.5, 2.6, 8.5**
    - For all modules, verify dependency resolution completes without errors
  
  - [ ]* 7.3 Write property test for native library architecture mapping
    - **Property 3: Native Library Architecture Mapping**
    - **Validates: Requirements 3.7**
    - Verify .so files are extracted to correct architecture folders
  
  - [ ]* 7.4 Write property test for GWT source dependency completeness
    - **Property 4: GWT Source Dependency Completeness**
    - **Validates: Requirements 4.4**
    - Verify all runtime dependencies have corresponding sources dependencies
  
  - [ ]* 7.5 Write property test for Java version consistency
    - **Property 5: Java Version Consistency**
    - **Validates: Requirements 6.1, 6.2, 6.3**
    - Verify all modules use Java 11 for source and target compatibility
  
  - [ ]* 7.6 Write property test for build output completeness
    - **Property 7: Build Output Completeness**
    - **Validates: Requirements 9.5**
    - Verify build outputs contain all required dependencies and assets
  
  - [ ]* 7.7 Write property test for Gradle properties application
    - **Property 8: Gradle Properties Application**
    - **Validates: Requirements 10.4**
    - Verify Gradle respects configured properties during build
  
  - [ ]* 7.8 Run example tests for version verification
    - Verify gradle-wrapper.properties contains Gradle 8.5 URL
    - Verify root build.gradle ext.gdxVersion is 1.12.1
    - Verify Android SDK configuration (compileSdk 34, minSdk 21, targetSdk 34)
    - Verify no build.gradle contains "jcenter()"
    - Verify all modules contain "mavenCentral()"
    - _Requirements: 1.1, 2.1, 3.2, 3.3, 3.4, 5.1, 5.2_
  
  - [ ]* 7.9 Run manual verification tests
    - Test desktop:run launches application window with correct dimensions
    - Test android:assembleDebug creates valid APK
    - Test html:dist creates complete web application
    - Verify no console errors or deprecation warnings
    - _Requirements: 6.4, 7.1, 7.2, 7.3, 9.2, 9.3, 9.4_

- [ ] 8. Checkpoint - Migration Complete
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional verification tasks and can be skipped for faster completion
- Each phase builds on the previous phase - follow the order strictly to avoid build failures
- The migration updates build configuration files (Gradle) and one Java source file (DesktopLauncher.java)
- Core game logic in the core module requires no changes
- Property tests validate universal correctness properties across the build system
- Manual tests verify end-to-end functionality of each platform module
