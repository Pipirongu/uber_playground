package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating backward compatibility of enhanced projectile system.
 * Verifies that all public method signatures remain unchanged after enhancements.
 * 
 * **Validates: Requirements 4.1, 4.2**
 */
public class BackwardCompatibilityTest {

    @Test
    @DisplayName("ProjectilePool public interface remains unchanged")
    public void testProjectilePoolPublicInterface() {
        // Verify all expected public methods exist with correct signatures
        Class<ProjectilePool> clazz = ProjectilePool.class;
        
        // Check constructor signature
        try {
            clazz.getConstructor(World.class, Stage.class);
        } catch (NoSuchMethodException e) {
            fail("ProjectilePool constructor(World, Stage) should exist");
        }
        
        // Check all expected public methods exist
        assertDoesNotThrow(() -> {
            Method obtain = clazz.getMethod("obtain", Vector2.class, Vector2.class, Vector2.class);
            assertEquals(Projectile.class, obtain.getReturnType(), 
                        "obtain method should return Projectile");
            
            Method free = clazz.getMethod("free", Projectile.class);
            assertEquals(void.class, free.getReturnType(), 
                        "free method should return void");
            
            Method updateAll = clazz.getMethod("updateAll", float.class);
            assertEquals(void.class, updateAll.getReturnType(), 
                        "updateAll method should return void");
            
            Method disposeAll = clazz.getMethod("disposeAll");
            assertEquals(void.class, disposeAll.getReturnType(), 
                        "disposeAll method should return void");
            
            Method clearAllActive = clazz.getMethod("clearAllActive");
            assertEquals(void.class, clearAllActive.getReturnType(), 
                        "clearAllActive method should return void");
            
            Method recreatePool = clazz.getMethod("recreatePool");
            assertEquals(void.class, recreatePool.getReturnType(), 
                        "recreatePool method should return void");
        }, "All expected ProjectilePool public methods should exist with correct signatures");
    }

    @Test
    @DisplayName("Projectile public interface remains unchanged")
    public void testProjectilePublicInterface() {
        // Verify all expected public methods exist with correct signatures
        Class<Projectile> clazz = Projectile.class;
        
        // Check constructor signature
        try {
            clazz.getConstructor(String.class, World.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            fail("Projectile constructor(String, World, int, int) should exist");
        }
        
        // Check all expected public methods exist
        assertDoesNotThrow(() -> {
            Method fire = clazz.getMethod("fire", Vector2.class, Vector2.class, Vector2.class);
            assertEquals(void.class, fire.getReturnType(), 
                        "fire method should return void");
            
            Method deactivate = clazz.getMethod("deactivate");
            assertEquals(void.class, deactivate.getReturnType(), 
                        "deactivate method should return void");
            
            Method markForDeactivation = clazz.getMethod("markForDeactivation");
            assertEquals(void.class, markForDeactivation.getReturnType(), 
                        "markForDeactivation method should return void");
            
            Method isMarkedForDeactivation = clazz.getMethod("isMarkedForDeactivation");
            assertEquals(boolean.class, isMarkedForDeactivation.getReturnType(), 
                        "isMarkedForDeactivation method should return boolean");
            
            Method isActive = clazz.getMethod("isActive");
            assertEquals(boolean.class, isActive.getReturnType(), 
                        "isActive method should return boolean");
            
            Method getTimeAlive = clazz.getMethod("getTimeAlive");
            assertEquals(float.class, getTimeAlive.getReturnType(), 
                        "getTimeAlive method should return float");
            
            Method act = clazz.getMethod("act", float.class);
            assertEquals(void.class, act.getReturnType(), 
                        "act method should return void");
        }, "All expected Projectile public methods should exist with correct signatures");
    }

    @Test
    @DisplayName("Projectile constants maintain expected values")
    public void testProjectileConstants() {
        // Verify that public constants exist and have expected values
        try {
            // Check PROJECTILE_SPEED constant exists and has enhanced value
            assertEquals(7500.0f, Projectile.PROJECTILE_SPEED, 0.001f,
                        "PROJECTILE_SPEED should be enhanced to 7500.0f");
            
            // Check PROJECTILE_LIFETIME constant exists and maintains original value
            assertEquals(15.0f, Projectile.PROJECTILE_LIFETIME, 0.001f,
                        "PROJECTILE_LIFETIME should remain 15.0f for backward compatibility");
            
        } catch (Exception e) {
            fail("Projectile constants should be accessible: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("No public methods were removed from ProjectilePool")
    public void testProjectilePoolMethodsNotRemoved() {
        Class<ProjectilePool> clazz = ProjectilePool.class;
        Method[] methods = clazz.getMethods();
        
        // Get all public methods declared in ProjectilePool (not inherited)
        Set<String> publicMethods = Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> Modifier.isPublic(m.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());
        
        // Verify expected methods are present
        assertTrue(publicMethods.contains("obtain"), 
                  "obtain method should be public");
        assertTrue(publicMethods.contains("free"), 
                  "free method should be public");
        assertTrue(publicMethods.contains("updateAll"), 
                  "updateAll method should be public");
        assertTrue(publicMethods.contains("disposeAll"), 
                  "disposeAll method should be public");
        assertTrue(publicMethods.contains("clearAllActive"), 
                  "clearAllActive method should be public");
        assertTrue(publicMethods.contains("recreatePool"), 
                  "recreatePool method should be public");
    }

    @Test
    @DisplayName("No public methods were removed from Projectile")
    public void testProjectileMethodsNotRemoved() {
        Class<Projectile> clazz = Projectile.class;
        
        // Get all public methods declared in Projectile (not inherited)
        Set<String> publicMethods = Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> Modifier.isPublic(m.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());
        
        // Verify expected methods are present
        assertTrue(publicMethods.contains("fire"), 
                  "fire method should be public");
        assertTrue(publicMethods.contains("deactivate"), 
                  "deactivate method should be public");
        assertTrue(publicMethods.contains("markForDeactivation"), 
                  "markForDeactivation method should be public");
        assertTrue(publicMethods.contains("isMarkedForDeactivation"), 
                  "isMarkedForDeactivation method should be public");
        assertTrue(publicMethods.contains("isActive"), 
                  "isActive method should be public");
        assertTrue(publicMethods.contains("getTimeAlive"), 
                  "getTimeAlive method should be public");
        assertTrue(publicMethods.contains("act"), 
                  "act method should be public");
    }

    @Test
    @DisplayName("ProjectilePool constructor parameters remain unchanged")
    public void testProjectilePoolConstructorCompatibility() {
        // Verify constructor signature matches expected interface
        Class<ProjectilePool> clazz = ProjectilePool.class;
        
        try {
            // Should accept World and Stage parameters
            clazz.getConstructor(World.class, Stage.class);
        } catch (NoSuchMethodException e) {
            fail("ProjectilePool should have constructor(World, Stage): " + e.getMessage());
        }
        
        // Verify no additional required constructor parameters were added
        assertEquals(1, clazz.getConstructors().length,
                    "ProjectilePool should have exactly one public constructor");
    }

    @Test
    @DisplayName("Projectile constructor parameters remain unchanged")
    public void testProjectileConstructorCompatibility() {
        // Verify constructor signature matches expected interface
        Class<Projectile> clazz = Projectile.class;
        
        try {
            // Should accept String, World, int, int parameters
            clazz.getConstructor(String.class, World.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            fail("Projectile should have constructor(String, World, int, int): " + e.getMessage());
        }
        
        // Verify no additional required constructor parameters were added
        assertEquals(1, clazz.getConstructors().length,
                    "Projectile should have exactly one public constructor");
    }
}