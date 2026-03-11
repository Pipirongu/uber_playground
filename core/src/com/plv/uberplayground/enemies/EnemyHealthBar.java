package com.plv.uberplayground.enemies;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * Visual health bar component for enemies.
 * Renders a health bar above enemies with efficient updates only when health changes.
 * Provides proper positioning relative to enemy location and integrates with LibGDX rendering system.
 */
public class EnemyHealthBar {
    
    // Health bar dimensions from configuration
    private final float width;
    private final float height;
    private final float offsetY;
    
    // Colors for health bar rendering
    private final Color backgroundColor;
    private final Color foregroundColor;
    private final Color borderColor;
    
    // Optimization: track last rendered health to avoid unnecessary updates
    private float lastRenderedHealth = -1.0f;
    
    // Health bar positioning
    private final Vector2 barPosition = new Vector2();
    
    /**
     * Creates a new EnemyHealthBar with default configuration.
     */
    public EnemyHealthBar() {
        this(SpawnConfiguration.HEALTH_BAR_WIDTH, 
             SpawnConfiguration.HEALTH_BAR_HEIGHT, 
             SpawnConfiguration.HEALTH_BAR_OFFSET);
    }
    
    /**
     * Creates a new EnemyHealthBar with custom dimensions.
     * @param width Width of the health bar in pixels
     * @param height Height of the health bar in pixels
     * @param offsetY Vertical offset above enemy position in pixels
     */
    public EnemyHealthBar(float width, float height, float offsetY) {
        this.width = width;
        this.height = height;
        this.offsetY = offsetY;
        
        // Initialize colors
        this.backgroundColor = new Color(0.2f, 0.2f, 0.2f, 0.8f); // Dark gray background
        this.foregroundColor = new Color(0.0f, 0.8f, 0.0f, 1.0f); // Green health
        this.borderColor = new Color(0.0f, 0.0f, 0.0f, 1.0f); // Black border
    }
    
    /**
     * Renders the health bar above the specified enemy position.
     * Only renders if health percentage has changed since last render for efficiency.
     * 
     * @param renderer The ShapeRenderer to use for drawing
     * @param camera The camera for projection matrix setup
     * @param enemyPosition The world position of the enemy
     * @param healthPercentage Health percentage (0.0 to 1.0)
     * @param forceRender If true, renders regardless of health change optimization
     */
    public void render(ShapeRenderer renderer, Camera camera, Vector2 enemyPosition, 
                      float healthPercentage, boolean forceRender) {
        
        // Early return for pooled enemies at (0,0) - prevents visual artifacts
        boolean isPooledPosition = (enemyPosition.x == 0 && enemyPosition.y == 0);
        if (isPooledPosition) {
            return; // Skip rendering for pooled enemies
        }
        
        // Enhanced optimization: only render if health changed or forced
        if (!forceRender && Math.abs(healthPercentage - lastRenderedHealth) < 0.001f) {
            return; // Skip rendering - no significant change
        }
        
        // Clamp health percentage to valid range
        healthPercentage = Math.max(0.0f, Math.min(1.0f, healthPercentage));
        
        // Calculate health bar position (centered above enemy)
        barPosition.set(enemyPosition.x - width / 2.0f, enemyPosition.y + offsetY);
        
        // Set up projection matrix
        Matrix4 projectionMatrix = camera.combined.cpy();
        renderer.setProjectionMatrix(projectionMatrix);
        
        // Render all shapes in one begin/end block to avoid conflicts
        renderer.begin(ShapeType.Filled);
        
        // Render background (full bar)
        renderer.setColor(backgroundColor);
        renderer.rect(barPosition.x, barPosition.y, width, height);
        
        // Render health (foreground)
        if (healthPercentage > 0.0f) {
            // Calculate health color based on percentage
            Color healthColor = calculateHealthColor(healthPercentage);
            renderer.setColor(healthColor);
            renderer.rect(barPosition.x, barPosition.y, width * healthPercentage, height);
        }
        
        renderer.end();
        
        // Render border separately
        renderer.setColor(borderColor);
        renderer.begin(ShapeType.Line);
        renderer.rect(barPosition.x, barPosition.y, width, height);
        renderer.end();
        
        // Update last rendered health for optimization
        lastRenderedHealth = healthPercentage;
    }
    
    /**
     * Convenience method that renders with automatic health change detection.
     * 
     * @param renderer The ShapeRenderer to use for drawing
     * @param camera The camera for projection matrix setup
     * @param enemyPosition The world position of the enemy
     * @param healthPercentage Health percentage (0.0 to 1.0)
     */
    public void render(ShapeRenderer renderer, Camera camera, Vector2 enemyPosition, float healthPercentage) {
        render(renderer, camera, enemyPosition, healthPercentage, false);
    }
    
    /**
     * Calculates the health bar color based on health percentage.
     * Green at full health, yellow at medium health, red at low health.
     * 
     * @param healthPercentage Health percentage (0.0 to 1.0)
     * @return Color for the health bar
     */
    private Color calculateHealthColor(float healthPercentage) {
        if (healthPercentage > 0.6f) {
            // Green to yellow transition (100% to 60%)
            float factor = (healthPercentage - 0.6f) / 0.4f; // 0.0 to 1.0
            return new Color(1.0f - factor, 0.8f, 0.0f, 1.0f);
        } else if (healthPercentage > 0.3f) {
            // Yellow to orange transition (60% to 30%)
            float factor = (healthPercentage - 0.3f) / 0.3f; // 0.0 to 1.0
            return new Color(1.0f, 0.8f * factor, 0.0f, 1.0f);
        } else {
            // Orange to red transition (30% to 0%)
            float factor = healthPercentage / 0.3f; // 0.0 to 1.0
            return new Color(1.0f, 0.2f * factor, 0.0f, 1.0f);
        }
    }
    
    /**
     * Sets custom colors for the health bar.
     * 
     * @param background Background color
     * @param foreground Foreground (health) color
     * @param border Border color
     */
    public void setColors(Color background, Color foreground, Color border) {
        this.backgroundColor.set(background);
        this.foregroundColor.set(foreground);
        this.borderColor.set(border);
    }
    
    /**
     * Gets the width of the health bar.
     * @return Health bar width in pixels
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the health bar.
     * @return Health bar height in pixels
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * Gets the vertical offset above enemy position.
     * @return Vertical offset in pixels
     */
    public float getOffsetY() {
        return offsetY;
    }
    
    /**
     * Forces the next render to occur regardless of health change optimization.
     * Useful when the enemy position changes or other visual updates are needed.
     */
    public void forceNextRender() {
        lastRenderedHealth = -1.0f;
    }
    
    /**
     * Resets the health bar state for reuse.
     * Clears the optimization cache to ensure proper rendering.
     */
    public void reset() {
        lastRenderedHealth = -1.0f;
    }
}