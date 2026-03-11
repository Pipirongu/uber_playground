package com.plv.uberplayground.enemies;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Visual behavior label display for enemy PlayerActors.
 * Shows the current steering behavior name below the enemy, opposite to the health bar.
 * 
 * Features:
 * - Displays current behavior name (WANDER, SEEK, FLEE, etc.)
 * - Positioned below the enemy (opposite to health bar)
 * - Color-coded by behavior type for quick visual identification
 * - Efficient rendering with minimal performance impact
 * 
 * Requirements: Visual debugging and behavior identification
 */
public class EnemyBehaviorLabel {
    
    // Label dimensions and positioning
    private static final float LABEL_WIDTH = 1.5f; // Increased width for better text fit
    private static final float LABEL_HEIGHT = 0.4f; // Increased height for better text fit
    private static final float LABEL_OFFSET_Y = -35.0f; // Well below enemy (negative Y) - similar distance as health bar but below
    
    // Text properties
    private static final float TEXT_SCALE = 1.0f; // Increased for better readability
    
    // Behavior colors for visual identification
    private static final Color WANDER_COLOR = Color.YELLOW;
    private static final Color SEEK_COLOR = Color.GREEN;
    private static final Color FLEE_COLOR = Color.RED;
    private static final Color PATROL_COLOR = Color.BLUE;
    private static final Color ORBIT_COLOR = Color.CYAN;
    private static final Color INTERCEPT_COLOR = Color.MAGENTA;
    private static final Color FLOCK_COLOR = Color.ORANGE;
    private static final Color DEFAULT_COLOR = Color.WHITE;
    
    // Font for text rendering (static to share across all instances)
    private static BitmapFont font = null;
    private static SpriteBatch spriteBatch = null;
    
    /**
     * Initializes the static font and sprite batch for text rendering.
     * Should be called once during game initialization.
     */
    public static void initialize() {
        try {
            if (font == null && com.badlogic.gdx.Gdx.files != null) {
                font = new BitmapFont(); // Default LibGDX font
                font.getData().setScale(TEXT_SCALE);
            }
            if (spriteBatch == null) {
                spriteBatch = new SpriteBatch();
            }
        } catch (Exception e) {
            // Handle initialization errors gracefully (e.g., in headless environment)
            // This prevents crashes during testing or when LibGDX context is not available
        }
    }
    
    /**
     * Disposes of static resources.
     * Should be called during game cleanup.
     */
    public static void dispose() {
        try {
            if (font != null) {
                font.dispose();
                font = null;
            }
        } catch (Exception e) {
            // Handle disposal errors gracefully
            font = null;
        }
        
        try {
            if (spriteBatch != null) {
                spriteBatch.dispose();
                spriteBatch = null;
            }
        } catch (Exception e) {
            // Handle disposal errors gracefully
            spriteBatch = null;
        }
    }
    
    /**
     * Renders the behavior label for an enemy.
     * 
     * @param shapeRenderer The shape renderer for drawing the label background
     * @param camera The camera for coordinate transformation
     * @param enemyPosition The world position of the enemy
     * @param behaviorType The current behavior type to display
     * @param forceRender If true, always render; if false, use performance optimization
     */
    public void render(ShapeRenderer shapeRenderer, Camera camera, Vector2 enemyPosition, 
                      SteeringBehaviorType behaviorType, boolean forceRender) {
        
        if (behaviorType == null) {
            return;
        }
        
        // Performance optimization: skip rendering if not forced and behavior is common
        // Disabled for now to show all behaviors - can be re-enabled if performance is an issue
        /*
        if (!forceRender && (behaviorType == SteeringBehaviorType.WANDER || behaviorType == SteeringBehaviorType.SEEK)) {
            // Skip rendering for common behaviors to improve performance
            // This can be adjusted based on performance requirements
            return;
        }
        */
        
        // Calculate label position (below enemy)
        float labelX = enemyPosition.x - LABEL_WIDTH / 2f;
        float labelY = enemyPosition.y + LABEL_OFFSET_Y;
        
        // Get behavior color
        Color behaviorColor = getBehaviorColor(behaviorType);
        
        // Render label background
        renderLabelBackground(shapeRenderer, camera, labelX, labelY, behaviorColor);
        
        // Render behavior text
        renderBehaviorText(camera, enemyPosition, behaviorType, behaviorColor);
    }
    
    /**
     * Renders the label background rectangle.
     */
    private void renderLabelBackground(ShapeRenderer shapeRenderer, Camera camera, 
                                     float labelX, float labelY, Color color) {
        try {
            // Set color with transparency
            Color bgColor = new Color(color.r, color.g, color.b, 0.7f);
            shapeRenderer.setColor(bgColor);
            
            // Begin shape rendering
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(camera.combined);
            
            // Draw background rectangle
            shapeRenderer.rect(labelX, labelY, LABEL_WIDTH, LABEL_HEIGHT);
            
            // End shape rendering
            shapeRenderer.end();
            
            // Draw border
            shapeRenderer.setColor(color);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.rect(labelX, labelY, LABEL_WIDTH, LABEL_HEIGHT);
            shapeRenderer.end();
            
        } catch (Exception e) {
            // Graceful error handling - don't crash the game for rendering issues
            try {
                shapeRenderer.end();
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Renders the behavior text on the label.
     */
    private void renderBehaviorText(Camera camera, Vector2 enemyPosition, 
                                  SteeringBehaviorType behaviorType, Color color) {
        
        // Initialize font if needed
        if (font == null || spriteBatch == null) {
            initialize();
        }
        
        try {
            // Get behavior text
            String behaviorText = getBehaviorText(behaviorType);
            
            // Calculate text position (centered on label)
            float textX = enemyPosition.x;
            float textY = enemyPosition.y + LABEL_OFFSET_Y + LABEL_HEIGHT / 2f;
            
            // Begin sprite batch rendering
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            
            // Set text color
            font.setColor(Color.WHITE); // White text for good contrast
            
            // Get text bounds for centering
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, behaviorText);
            
            // Draw text centered
            font.draw(spriteBatch, behaviorText, 
                     textX - layout.width / 2f, 
                     textY + layout.height / 2f);
            
            // End sprite batch rendering
            spriteBatch.end();
            
        } catch (Exception e) {
            // Graceful error handling
            try {
                spriteBatch.end();
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Gets the display color for a behavior type.
     */
    private Color getBehaviorColor(SteeringBehaviorType behaviorType) {
        switch (behaviorType) {
            case WANDER:
                return WANDER_COLOR;
            case SEEK:
                return SEEK_COLOR;
            case FLEE:
                return FLEE_COLOR;
            case PATROL:
                return PATROL_COLOR;
            case ORBIT:
                return ORBIT_COLOR;
            case INTERCEPT:
                return INTERCEPT_COLOR;
            case FLOCK:
                return FLOCK_COLOR;
            default:
                return DEFAULT_COLOR;
        }
    }
    
    /**
     * Gets the display text for a behavior type.
     */
    private String getBehaviorText(SteeringBehaviorType behaviorType) {
        switch (behaviorType) {
            case WANDER:
                return "WANDER";
            case SEEK:
                return "SEEK";
            case FLEE:
                return "FLEE";
            case PATROL:
                return "PATROL";
            case ORBIT:
                return "ORBIT";
            case INTERCEPT:
                return "INTERCEPT";
            case FLOCK:
                return "FLOCK";
            default:
                return "UNKNOWN";
        }
    }
}