package com.plv.uberplayground.scoring;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

/**
 * Score display component that renders the current score on screen during gameplay.
 * 
 * This class implements the ScoreListener interface to receive real-time score updates
 * from the ScoreTracker. It uses LibGDX's BitmapFont for text rendering and positions
 * the score display at the top middle of the screen.
 * 
 * Key features:
 * - Real-time score updates through ScoreListener interface
 * - Centered positioning at top of screen
 * - LibGDX BitmapFont rendering for consistent text display
 * - Thread-safe score value storage
 * 
 * Validates: Requirements 1.2, 1.3, 1.4
 */
public class ScoreDisplay implements ScoreListener {
    
    /** Font used for rendering the score text */
    private final BitmapFont font;
    
    /** Layout helper for text positioning calculations */
    private final GlyphLayout layout;
    
    /** Current score value to display */
    private volatile int currentScore;
    
    /** X position for score display (center of screen) */
    private float positionX;
    
    /** Y position for score display (top of screen with offset) */
    private float positionY;
    
    /** Screen width for centering calculations */
    private float screenWidth;
    
    /** Screen height for positioning calculations */
    private float screenHeight;
    
    /**
     * Creates a new ScoreDisplay with default font and screen dimensions.
     * Uses LibGDX's default font and standard game screen dimensions.
     */
    public ScoreDisplay() {
        this(new BitmapFont(), 1600f, 1200f); // Default game screen dimensions
    }
    
    /**
     * Creates a new ScoreDisplay with the specified font and screen dimensions.
     * 
     * @param font The BitmapFont to use for rendering the score
     * @param screenWidth The width of the game screen
     * @param screenHeight The height of the game screen
     * @throws IllegalArgumentException if font is null or dimensions are invalid
     */
    public ScoreDisplay(BitmapFont font, float screenWidth, float screenHeight) {
        if (font == null) {
            throw new IllegalArgumentException("Font cannot be null");
        }
        if (screenWidth <= 0 || screenHeight <= 0) {
            throw new IllegalArgumentException("Screen dimensions must be positive");
        }
        
        this.font = font;
        this.layout = new GlyphLayout();
        this.currentScore = ScoreConstants.INITIAL_SCORE;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Position at top middle of screen
        updatePosition();
    }
    
    /**
     * Renders the current score at the top middle of the screen.
     * This method should be called during the game's render loop.
     * 
     * @param batch The SpriteBatch to use for rendering
     * @throws IllegalArgumentException if batch is null
     * 
     * Validates: Requirements 1.2, 1.4 - Score display shows current score at top middle
     * and remains visible throughout entire game session
     */
    public void render(SpriteBatch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("SpriteBatch cannot be null");
        }
        
        String scoreText = "Score: " + currentScore;
        
        // Update layout for current score text to get accurate width
        layout.setText(font, scoreText);
        
        // Calculate centered X position based on text width
        float centeredX = (screenWidth - layout.width) / 2f;
        
        // Render the score text
        font.draw(batch, scoreText, centeredX, positionY);
    }
    
    /**
     * Called when the score changes. Updates the displayed score immediately.
     * This method is thread-safe and implements the ScoreListener interface.
     * 
     * @param newScore The new score value to display
     * 
     * Validates: Requirements 1.3 - Score display updates immediately when score changes
     */
    @Override
    public void onScoreChanged(int newScore) {
        this.currentScore = newScore;
    }
    
    /**
     * Sets the position of the score display on screen.
     * This method allows for custom positioning if needed.
     * 
     * @param x The X coordinate for the score display
     * @param y The Y coordinate for the score display
     */
    public void setPosition(float x, float y) {
        this.positionX = x;
        this.positionY = y;
    }
    
    /**
     * Updates the screen dimensions and recalculates position.
     * Should be called when the screen is resized.
     * 
     * @param screenWidth The new screen width
     * @param screenHeight The new screen height
     * @throws IllegalArgumentException if dimensions are invalid
     */
    public void updateScreenDimensions(float screenWidth, float screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            throw new IllegalArgumentException("Screen dimensions must be positive");
        }
        
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updatePosition();
    }
    
    /**
     * Gets the current score being displayed.
     * Useful for testing and debugging.
     * 
     * @return The current score value
     */
    public int getCurrentScore() {
        return currentScore;
    }
    
    /**
     * Gets the X position of the score display.
     * 
     * @return The X coordinate
     */
    public float getPositionX() {
        return positionX;
    }
    
    /**
     * Gets the Y position of the score display.
     * 
     * @return The Y coordinate
     */
    public float getPositionY() {
        return positionY;
    }
    
    /**
     * Updates the position to center the display at the top of the screen.
     * Uses the Y offset constant for consistent positioning.
     */
    private void updatePosition() {
        this.positionX = screenWidth / 2f; // Center horizontally (will be adjusted per text width in render)
        this.positionY = screenHeight - ScoreConstants.SCORE_DISPLAY_Y_OFFSET; // Top with offset
    }
}